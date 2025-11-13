package it.universita.db;

import it.universita.model.*;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UniDAO {
    private enum Ruolo {PROFESSORE, STUDENTE}

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }

    //fatto
    public Utente login(String nomeUtente, String password) {
        long id;
        String sql = "SELECT id, username,password_hash,ruolo FROM utente WHERE username = ?";
        try (Connection cn = GestoreDB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setString(1, nomeUtente);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    String passHash = rs.getString("password_hash");
                    if (BCrypt.checkpw(password, passHash)) {
                        System.out.println("login effettuato");
                        id = rs.getLong("id");
                        Utente u = new Utente(id, nomeUtente, rs.getString("ruolo"));
                        return u;
                    } else {
                        System.out.println("Login non riuscito");
                        return null;
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("errore nel login",e);
        }
    }

    //fatto
    public Utente registrazione(String nome, String cognome, String dataDiNascita, String candidato, String password) throws SQLException {
        final String sqlUtente = "INSERT INTO utente (username, password_hash, ruolo) VALUES (?,?,?)";
        final String sqlStudente = "INSERT INTO studente (id, nome, cognome, data_nascita) VALUES (?,?,?,?)";
        final String sqlProf = "INSERT INTO professore (id, nome, cognome, data_nascita) VALUES (?,?,?,?)";

        //base è il primo nome da provare per inserire l'utente
        final String base = candidato == null ? "" : candidato.trim(); // (+ opzionale: .toLowerCase(Locale.ROOT))
        final Ruolo ruolo = base.endsWith(".docente") ? Ruolo.PROFESSORE : Ruolo.STUDENTE;
        final LocalDate dataNascita = Persona.parseData(dataDiNascita);

        long utenteId = 0L;
        String userEffettivo = null;


        try (Connection cn = GestoreDB.getConnection()) {
            cn.setAutoCommit(false);
            try {
                final int MAX = 10;
                boolean inserito = false;
                //facciamo il for per gestire la race condition, se un altro thread nel frattempo voleva inserire lo stesso nome prima di lui
                for (int i = 0; i <= MAX; i++) {
                    //Se è la prima volta che siamo nel ciclo, username sarà base, quindi il primo nome da provare
                    //ossia quello fornito dall'utente, al secondo ciclo andiamo a sommargli un numero e riproviamo l'inserimento
                    String username = (i == 0) ? base : base + i;
                    try (PreparedStatement ps = cn.prepareStatement(sqlUtente, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setString(1, username);
                        ps.setString(2, hashPassword(password));
                        ps.setString(3, ruolo.name());
                        ps.executeUpdate();

                        try (ResultSet rs = ps.getGeneratedKeys()) {
                            if (!rs.next()) throw new SQLException("Manca generated key utente");
                            utenteId = rs.getLong(1);
                        }
                        userEffettivo = username;
                        inserito = true;
                        break; //Inserimento utente riuscito usciamo dal ciclo
                    } catch (SQLIntegrityConstraintViolationException dup) {
                        System.out.println("Username esistente provo il prossimo ");
                        // riprova col prossimo suffisso
                        continue;
                    }
                }
                if (!inserito) throw new SQLException("Nessuno username disponibile vicino a " + base);
                if (ruolo == Ruolo.PROFESSORE) {
                    try (PreparedStatement ps = cn.prepareStatement(sqlProf)) {
                        ps.setLong(1, utenteId);
                        ps.setString(2, nome);
                        ps.setString(3, cognome);
                        ps.setDate(4, Date.valueOf(dataNascita));
                        if (ps.executeUpdate() != 1) throw new SQLException("Inserimento professore non riuscito");
                    }
                } else {
                    try (PreparedStatement ps = cn.prepareStatement(sqlStudente)) {
                        ps.setLong(1, utenteId);
                        ps.setString(2, nome);
                        ps.setString(3, cognome);
                        ps.setDate(4, Date.valueOf(dataNascita));
                        if (ps.executeUpdate() != 1) throw new SQLException("Inserimento studente non riuscito");
                    }
                }
                cn.commit();
            } catch (Exception e) {
                try {
                    cn.rollback();
                } catch (SQLException sup) {
                    e.addSuppressed(sup);
                }
                throw e;
            } finally {
                try {
                    cn.setAutoCommit(true);
                } catch (SQLException ignore) {
                }
            }
        }
        return new Utente(utenteId, userEffettivo, ruolo.name());
    }

    // Appelli in lettura
    public List<Appello> listaAppelliAperti() {
        List<Appello> listaAppelli = new ArrayList<>();
        String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, stato\n" +
                "FROM appello\n" +
                "WHERE stato = 'APERTO' AND data_esame >= NOW()\n" +
                "ORDER BY dataAppello ASC\n"; //ordino gli appelli per data
        try (Connection cn = GestoreDB.getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                //creo un oggetto di tipo TimeStamp perchè nel Db abbiamo DATETIME come valore e con LocalData
                //andiamo a troncare l'orario altrimenti, quindi ci serve un oggetto di tipo LocalDateTime
                Timestamp ts = rs.getTimestamp("dataAppello");
                Appello app = new Appello(rs.getLong("id"), rs.getLong("materiaId"),
                        rs.getLong("docenteId"), ts.toLocalDateTime(),
                        rs.getString("aula"), rs.getString("stato"));
                listaAppelli.add(app);
            }

        } catch (SQLException e) {
            System.out.println("Problema di tipo SQLException");
            e.printStackTrace();
        }
        return listaAppelli;
    }

    public List<Appello> listAppelliPrenotatiDaStudente(long studenteId) {
        List<Appello> appelliStudente  = new ArrayList<>();
        String sql = """
                SELECT appello.id,appello.id_materia, appello.id_professore, appello.data_esame, appello.aula, appello.stato 
                FROM appello JOIN prenotazione  
                ON appello.id = prenotazione.id_appello WHERE prenotazione.id_studente = ? AND prenotazione.stato = 'PRENOTATO'
                ORDER BY appello.data_esame ASC
                """;
        try(Connection cn = GestoreDB.getConnection();
            PreparedStatement ps = cn.prepareStatement(sql)){
            ps.setLong(1,studenteId);
            try(ResultSet rs = ps.executeQuery()){

                while(rs.next()){
                    Timestamp ts = rs.getTimestamp("data_esame");
                    Appello appello = new Appello(rs.getLong("id"),rs.getLong("id_materia"),rs.getLong("id_professore"),ts.toLocalDateTime(),rs.getString("aula"), rs.getString("stato"));
                    appelliStudente.add(appello);
                }
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        return appelliStudente;
    }

    public List<Appello> listAppelliPerDocenti(long docenteId) {
        List<Appello> appelliDocenti = new ArrayList<>();
        String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, stato\n" +
                "FROM appello\n" +
                "WHERE stato = 'APERTO' AND data_esame >= NOW() AND id_professore = ? \n" +
                "ORDER BY dataAppello ASC\n";
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    //creo un oggetto di tipo TimeStamp perchè nel Db abbiamo DATETIME come valore e con LocalData
                    //andiamo a troncare l'orario altrimenti, quindi ci serve un oggetto di tipo LocalDateTime
                    Timestamp ts = rs.getTimestamp("dataAppello");
                    Appello app = new Appello(rs.getLong("id"), rs.getLong("materiaId"),
                            rs.getLong("docenteId"), ts.toLocalDateTime(),
                            rs.getString("aula"), rs.getString("stato"));
                    appelliDocenti.add(app);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appelliDocenti;
    }

    //fatto
    public List<Studente> listIscrittiAppello(long appelloId) {
        List<Studente> studentiIscritti = new ArrayList<>();
        String sql = """
                Select studente.nome, studente.cognome, studente.matricola, studente.id, studente.data_nascita 
                From prenotazione Inner Join studente ON prenotazione.id_studente = studente.id
                Where id_appello= ? AND prenotazione.stato = 'PRENOTATO' Order by prenotazione.prenotato_il                
                """;
        try (Connection cn = GestoreDB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, appelloId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Studente s = new Studente(rs.getString("nome"), rs.getString("cognome"), rs.getDate("data_nascita").toLocalDate(), rs.getString("matricola"), rs.getLong("id"));
                    studentiIscritti.add(s);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentiIscritti;
    }

    // Prenotazioni (transazioni)
    //
    //usato dallo studente per prenotarsi all'appello FATTO
    public boolean prenotazioneAppello(long appelloId, long studenteId) {
        String sql = """
                INSERT INTO prenotazione(id_appello,id_studente,stato) 
                SELECT ?,?,'PRENOTATO' FROM appello WHERE appello.id = ? AND stato = 'APERTO'
                """;
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, appelloId);
            ps.setLong(2, studenteId);
            ps.setLong(3,appelloId);
            int i = ps.executeUpdate();
            if (i == 1) {
                System.out.println("Prenotazione avvenuta con successo");
                return true;
            } else {
                System.out.println("Appello non trovato o non aperto");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    //Utilizzato da studente FATTO
    public boolean cancellazionePrenotazioneAppello(long appelloId, long studenteId) {
        String sql = """
                UPDATE prenotazione 
                SET stato = 'CANCELLATO'
                WHERE id_appello = ? AND id_studente = ? AND stato = 'PRENOTATO'
                """;
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, appelloId);
            ps.setLong(2, studenteId);
            if (ps.executeUpdate() == 1) {
                System.out.println("Cancellazione all'appello effettuata");
                return true;
            } else {
                System.out.println("Cancellazione non riuscita");
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella cancellazione della prenotazione",e);
        }
    }

    //Libretto-esiti
    public Libretto mostraLibretto(long idStudente) {
        String sql =
                "SELECT e.id AS id_esame, e.id_prenotazione, e.id_professore, " +
                        "CONCAT(pf.nome,' ',pf.cognome) AS docente, m.nome AS materia, " +
                        "e.voto, e.lode, e.esito, e.data_registrazione " +
                        "FROM esame e " +
                        "JOIN prenotazione pr ON pr.id = e.id_prenotazione " +
                        "JOIN appello a ON a.id = pr.id_appello " +
                        "JOIN materia m ON m.id = a.id_materia " +
                        "JOIN professore pf ON pf.id = e.id_professore " +
                        "WHERE pr.id_studente = ? AND e.esito = 'SUPERATO' " +
                        "ORDER BY e.data_registrazione DESC";

        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, idStudente);
            try (ResultSet rs = ps.executeQuery()) {
                Libretto libretto = new Libretto();
                while (rs.next()) {
                    Esame e = new Esame();
                    e.setIdEsame(rs.getLong("id_esame"));
                    e.setIdPrenotazione(rs.getLong("id_prenotazione"));
                    e.setIdProfessore(rs.getLong("id_professore"));
                    e.setDocente(rs.getString("docente"));
                    e.setMateria(rs.getString("materia"));
                    e.setVoto(rs.getInt("voto"));
                    e.setLode(rs.getBoolean("lode"));
                    e.setEsito(rs.getString("esito"));
                    e.setDataRegistrazione(rs.getTimestamp("data_registrazione").toLocalDateTime());
                    libretto.aggiungiEsameAlLibretto(e);
                }
                return libretto;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore mostraLibretto", e);
        }
    }

    //Docente FATTO
    public boolean inserisciVoto(long prenotazioneId,long professoreId, long studenteId, int voto, boolean lode) throws SQLException {

        if (voto < 18 || voto > 30) {
            throw new IllegalArgumentException("Voto non valido (deve essere tra 18 e 30).");
        }
        if (voto < 30) {
            lode = false; // niente lode se non è 30
        }

        String inserisciVoto = """
                INSERT INTO esame(id_prenotazione, id_professore, voto, lode, esito)
                SELECT ?, ?, ?, ?, 'SUPERATO'
                FROM prenotazione
                WHERE id_studente = ?
                  AND id = ?
                  AND stato = 'PRENOTATO'
                """;

        String aggiornaPrenotazione = """
                UPDATE prenotazione
                SET stato = 'VERBALIZZATO'
                WHERE id = ?
                """;

        Connection cn = null;
        try {
            cn = GestoreDB.getConnection();
            cn.setAutoCommit(false);
            // PREPARED STATEMENTS
            try (PreparedStatement ps = cn.prepareStatement(inserisciVoto);
                 PreparedStatement ps1 = cn.prepareStatement(aggiornaPrenotazione)) {
                // INSERT ESAME
                ps.setLong(1, prenotazioneId);
                ps.setLong(2, professoreId);
                ps.setInt(3, voto);
                ps.setBoolean(4, lode);
                ps.setLong(5, studenteId);
                ps.setLong(6, prenotazioneId);
                int i = ps.executeUpdate();
                if (i == 0) {
                    // Nessuna prenotazione trovata / non PRENOTATO / non di quello studente
                    cn.rollback();
                    return false;
                }
                // UPDATE PRENOTAZIONE
                ps1.setLong(1, prenotazioneId);
                int j = ps1.executeUpdate();
                if (j != 1) {
                    cn.rollback();
                    throw new SQLException("Aggiornamento prenotazione fallito (righe aggiornate: " + j + ").");
                }
                cn.commit();
                System.out.println("Inserimento completato e prenotazione verbalizzata.");
                return true;
            }
        } catch (SQLException e) {
            if (cn != null) {
                try {
                    cn.rollback();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (cn != null) {
                try {
                    cn.setAutoCommit(true);
                    cn.close();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    public boolean creaAppello(long materiaId, long professoreId, LocalDateTime dataEsame, String aula) throws SQLException {
        if (dataEsame == null || aula == null || aula.isBlank()) {
            throw new IllegalArgumentException("Parametri non validi.");
        }
        if (dataEsame.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La data dell'esame non può essere nel passato.");
        }
        final String sql = "INSERT INTO appello (id_materia, id_professore, data_esame, aula) VALUES (?,?,?,?)";

        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, materiaId);
            ps.setLong(2, professoreId);
            ps.setTimestamp(3, Timestamp.valueOf(dataEsame));
            ps.setString(4, aula.trim());

            int rows = ps.executeUpdate();
            if (rows != 1) {
                throw new SQLException("Creazione appello fallita (righe toccate: " + rows + ").");
            }
            //se arrivi qua, l'appello è stato creato
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new SQLException("Creazione appello fallita: duplicato o riferimenti non validi.", e);
        }
        return true;
    }

    // Utilizzato dal Professore, serve a settare lo stato chiuso ad un appello
    public boolean chiudiAppello(long appelloId){
        String sql = """
                UPDATE appello
                SET stato = 'CHIUSO'
                WHERE id = ?
                """;
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, appelloId);
            if (ps.executeUpdate() == 1) {
                System.out.println("Appello chiuso");
                return true;
            } else {
                System.out.println("Chiusura non effettuata");
                return false;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore nella chiusura dell'appello",e);
        }
    }

    public static void main(String[] args) {
    }

}
