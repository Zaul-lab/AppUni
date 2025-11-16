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
                        Utente u = new Utente(id, nomeUtente, rs.getString("ruolo"), "Raul", "cognome", Persona.parseData("2000/09/09"));//Persona.parseData(rs.getTimestamp("data_registrazione").toString())
                        return u;
                    } else {
                        System.out.println("Login non riuscito");
                        return null;
                    }
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("errore nel login", e);
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
        return new Utente(utenteId, userEffettivo, ruolo.name(), nome, cognome, dataNascita);
    }

    public List<Appello> listaAppelliAperti() {
        List<Appello> listaAppelli = new ArrayList<>();

        String sql = """
                                SELECT
                                a.id AS appello_id,
                                a.data_esame AS dataAppello,
                                a.aula,
                                a.stato,
                                m.id AS materia_id,
                                m.nome AS nome_materia,
                                p.id AS docente_id,
                                p.nome AS nome_docente
                                FROM appello a
                                JOIN materia m ON a.id_materia = m.id
                                JOIN professore p ON m.id_professore = p.id
                                WHERE a.stato = 'APERTO'
                                AND a.data_esame >= NOW()
                                ORDER BY dataAppello ASC
                """;

        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Timestamp ts = rs.getTimestamp("dataAppello");

                Appello app = new Appello(
                        rs.getLong("appello_id"),
                        rs.getLong("materia_id"),   // id materia
                        ts.toLocalDateTime(),
                        rs.getString("aula"),
                        rs.getString("stato"),
                        rs.getString("nome_materia"),
                        rs.getString("nome_docente")
                );

                System.out.println("Appello trovato nel DAO: " + app);
                listaAppelli.add(app);
            }

        } catch (SQLException e) {
            System.out.println("Problema di tipo SQLException");
            e.printStackTrace();
        }
        return listaAppelli;
    }

    public List<Appello> listAppelliPrenotatiDaStudente(long studenteId) {
        List<Appello> appelliStudente = new ArrayList<>();

        String sql = """
                SELECT 
                    a.id            AS appello_id,
                    a.id_materia    AS id_materia,
                    a.data_esame    AS data_esame,
                    a.aula          AS aula,
                    a.stato         AS stato,
                    m.nome          AS nome_materia,
                    p.nome          AS nome_docente
                FROM appello a JOIN prenotazione pr ON a.id = pr.id_appello
                JOIN materia m ON a.id_materia = m.id
                JOIN professore p ON m.id_professore = p.id
                WHERE pr.id_studente = ? 
                AND pr.stato = 'PRENOTATO'
                ORDER BY a.data_esame ASC
                """;
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, studenteId);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Timestamp ts = rs.getTimestamp("data_esame");
                    Appello appello = new Appello(rs.getLong("appello_id"), rs.getLong("id_materia"), ts.toLocalDateTime(), rs.getString("aula"), rs.getString("stato"), rs.getString("nome_materia"), rs.getString("nome_docente"));
                    appelliStudente.add(appello);
                }
                System.out.println("log nel daoListaAppelliPrenotatiDaStudenti");
                for (Appello a : appelliStudente) System.out.println(a);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appelliStudente;
    }

    public List<Appello> listAppelliPerDocenti(long docenteId) {
        List<Appello> appelliDocenti = new ArrayList<>();
        String sql = """
                                SELECT
                                a.id AS appello_id,
                                a.data_esame AS dataAppello,
                                a.aula,
                                a.stato,
                                m.id AS materia_id,
                                m.nome AS nome_materia,
                                p.id AS docente_id,
                                p.nome AS nome_docente
                                FROM appello a
                                JOIN materia m ON a.id_materia = m.id
                                JOIN professore p ON m.id_professore = p.id
                                WHERE a.stato = 'APERTO'
                                AND a.data_esame >= NOW()
                                AND p.id = ?
                                ORDER BY dataAppello ASC
                """;


        /*String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, stato\n" +
                "FROM appello\n" +
                "WHERE stato = 'APERTO' AND data_esame >= NOW() AND docenteId = ? \n" +
                "ORDER BY dataAppello ASC\n";*/
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    //creo un oggetto di tipo TimeStamp perchè nel Db abbiamo DATETIME come valore e con LocalData
                    //andiamo a troncare l'orario altrimenti, quindi ci serve un oggetto di tipo LocalDateTime
                    Timestamp ts = rs.getTimestamp("dataAppello");
                    Appello app = new Appello(rs.getLong("appello_id"), rs.getLong("materia_id"),
                            ts.toLocalDateTime(),
                            rs.getString("aula"), rs.getString("stato"), rs.getString("nome_materia"), rs.getString("nome_docente"));
                    appelliDocenti.add(app);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appelliDocenti;
    }

    public List<Materia> mostraMaterieInsegnate(long docenteId) {
        ArrayList<Materia> materie = new ArrayList<>();

        String sql = """
                SELECT id,id_corso,nome,cfu,anno,semestre,id_professore FROM materia WHERE id_professore = ?
                """;
        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1,docenteId);
                ResultSet rs = ps.executeQuery();

                    while (rs.next()){
                        Materia m = new Materia(rs.getLong("id"),rs.getLong("id_corso"),rs.getString("nome"),rs.getInt("cfu"),rs.getInt("anno"),rs.getString("semestre"),rs.getLong("id_professore"));
                        materie.add(m);
                    }

                return materie;
        } catch (SQLException e) {
            e.printStackTrace();
            return materie;
        }

    }

    //fatto
    public List<StudenteIscrittoAppello> listIscrittiAppello(long appelloId) {
        List<StudenteIscrittoAppello> studentiIscritti = new ArrayList<>();
        String sql = """
                Select prenotazione.id AS prenotazione_id, prenotazione.prenotato_il,studente.nome, studente.cognome, studente.matricola, studente.id, studente.data_nascita 
                From prenotazione Inner JOIN studente ON prenotazione.id_studente = studente.id
                Where id_appello= ? AND prenotazione.stato = 'PRENOTATO' Order by prenotazione.prenotato_il                
                """;
        try (Connection cn = GestoreDB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, appelloId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Studente s = new Studente(rs.getString("nome"), rs.getString("cognome"), rs.getDate("data_nascita").toLocalDate(), rs.getString("matricola"), rs.getLong("id"));

                    long prenotazioneId = rs.getLong("prenotazione_id");
                    LocalDateTime prenotatoIl = rs.getTimestamp("prenotato_il").toLocalDateTime();

                    StudenteIscrittoAppello studIscr = new StudenteIscrittoAppello(prenotazioneId,s,prenotatoIl);

                    studentiIscritti.add(studIscr);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return studentiIscritti;
    }

    // Prenotazioni (transazioni)
    //usato dallo studente per prenotarsi all'appello FATTO
    public boolean prenotazioneAppello(long appelloId, long studenteId) {
        try (Connection cn = GestoreDB.getConnection()) {
            cn.setAutoCommit(false);

            // 1) provo ad aggiornare una prenotazione cancellata
            String sqlUpdate = """
                    UPDATE prenotazione p
                    JOIN appello a ON a.id = p.id_appello
                    SET p.stato = 'PRENOTATO',
                        p.prenotato_il = NOW()
                    WHERE p.id_appello = ?
                      AND p.id_studente = ?
                      AND p.stato = 'CANCELLATO'
                      AND a.stato = 'APERTO'
                    """;

            try (PreparedStatement psUp = cn.prepareStatement(sqlUpdate)) {
                psUp.setLong(1, appelloId);
                psUp.setLong(2, studenteId);
                int updated = psUp.executeUpdate();

                if (updated == 0) {
                    // 2) nessuna riga cancellata → provo ad inserire
                    String sqlInsert = """
                            INSERT INTO prenotazione (id_appello, id_studente, stato, prenotato_il)
                            SELECT ?, ?, 'PRENOTATO', NOW()
                            FROM appello
                            WHERE appello.id = ? AND stato = 'APERTO'
                            """;

                    try (PreparedStatement psIns = cn.prepareStatement(sqlInsert)) {
                        psIns.setLong(1, appelloId);
                        psIns.setLong(2, studenteId);
                        psIns.setLong(3, appelloId);

                        int inserted = psIns.executeUpdate();
                        if (inserted == 0) {
                            cn.rollback();
                            return false; // appello non aperto / non esistente
                        }
                    }
                }

                cn.commit();
                return true;
            } catch (SQLException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
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
            throw new RuntimeException("Errore nella cancellazione della prenotazione", e);
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
    public boolean inserisciVoto(long prenotazioneId, long professoreId, long studenteId, int voto, boolean lode) throws SQLException {

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

    public boolean creaAppello(long materiaId, LocalDateTime dataEsame, String aula) throws SQLException {
        if (dataEsame == null || aula == null || aula.isBlank()) {
            throw new IllegalArgumentException("Parametri non validi.");
        }
        if (dataEsame.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("La data dell'esame non può essere nel passato.");
        }
        final String sql = "INSERT INTO appello (id_materia, data_esame, aula) VALUES (?,?,?)";

        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(sql)) {

            ps.setLong(1, materiaId);
            ps.setTimestamp(2, Timestamp.valueOf(dataEsame));
            ps.setString(3, aula.trim());

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
    public boolean chiudiAppello(long appelloId) {
        String sql = """
                UPDATE appello
                SET stato = 'CHIUSO'
                WHERE id = ?
                AND stato = 'APERTO'
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
            throw new RuntimeException("Errore nella chiusura dell'appello", e);
        }
    }

    public static void main(String[] args) {
    }

}
