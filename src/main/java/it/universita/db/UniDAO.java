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

    private String generaNuovoUsername(Connection cn, String base) throws SQLException {
        String nuovo = base;
        int i = 1;
        String sqlCheck = "SELECT COUNT(*) FROM utente WHERE username = ?";
        try (PreparedStatement ps = cn.prepareStatement(sqlCheck)) {
            do {
                nuovo = base + i;
                ps.setString(1, nuovo);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) break; // trovato username libero
                }
                i++;
            } while (true);
        }
        return nuovo;
    }

    //fatto
    public Utente login(String nomeUtente, String password) throws SQLException {
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
            e.printStackTrace();
            return null;
        }
    }

    //fatto
    public Utente registrazione(String nome, String cognome, String dataDiNascita, String username, String password) throws SQLException {
        long utenteId = 0;
        final Ruolo ruolo = username.endsWith(".docente") ? Ruolo.PROFESSORE : Ruolo.STUDENTE;
        LocalDate dataNascita = Persona.parseData(dataDiNascita);
        //Verifica che username contenga .docente
        String sqlUtente = "INSERT  INTO utente (username,password_hash,ruolo) VALUES (?,?,?)";
        String sqlStudente = "INSERT INTO studente(id,nome, cognome, data_nascita) VALUES (?,?,?,?)";
        String sqlProfessore = "INSERT INTO professore(id,nome, cognome, data_nascita) VALUES (?,?,?,?)";
        String sqlCheck = "SELECT COUNT(*) FROM utente WHERE username = ?";
        try (Connection cn = GestoreDB.getConnection(); PreparedStatement pscheck = cn.prepareStatement(sqlCheck)) {
            cn.setAutoCommit(false);
            pscheck.setString(1, username);
            try (ResultSet rs = pscheck.executeQuery()) {
                if (rs.next() && rs.getInt(1) > 0) {
                    username = generaNuovoUsername(cn, username);
                    System.out.println("username già esistente ho generato uno in automatico: " + username);
                }
            }

            try (PreparedStatement ps = cn.prepareStatement(sqlUtente, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, username);
                ps.setString(2, hashPassword(password));
                ps.setString(3, ruolo.name());
                if (ps.executeUpdate() != 1) throw new SQLException("Inserimento utente non riuscito");

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (!rs.next()) throw new SQLException("Manca generated key utente");
                    utenteId = rs.getLong(1);
                }
                if (ruolo == Ruolo.PROFESSORE) {
                    try (PreparedStatement psProf = cn.prepareStatement(sqlProfessore)) {
                        psProf.setLong(1, utenteId);
                        psProf.setString(2, nome);
                        psProf.setString(3, cognome);
                        psProf.setDate(4, Date.valueOf(dataNascita));
                        if (psProf.executeUpdate() != 1) throw new SQLException("Inserimento professore non riuscito");
                    }
                } else {
                    try (PreparedStatement psStudente = cn.prepareStatement(sqlStudente)) {
                        psStudente.setLong(1, utenteId);
                        psStudente.setString(2, nome);
                        psStudente.setString(3, cognome);
                        psStudente.setDate(4, Date.valueOf(dataNascita));
                        if (psStudente.executeUpdate() != 1)
                            throw new SQLException("Inserimento studente non riuscito");
                    }
                }
                cn.commit();
            } catch (SQLException e) {
                try {
                    cn.rollback();
                } catch (SQLException sup) {
                    e.addSuppressed(sup);
                }
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
        return new Utente(utenteId, username, ruolo.name());
    }

    // Appelli in lettura
    //fatto
    public List<Appello> listaAppelliAperti() throws SQLException {
        List<Appello> listaAppelli = new ArrayList<>();
        String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, posti_max AS postiMax, stato\n" + "FROM appello\n" + "WHERE stato = 'APERTO' AND data_esame >= NOW()\n" + "ORDER BY dataAppello ASC\n"; //ordino gli appelli per data
        try (Connection cn = GestoreDB.getConnection(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                //creo un oggetto di tipo TimeStamp perchè nel Db abbiamo DATETIME come valore e con LocalData
                //andiamo a troncare l'orario altrimenti, quindi ci serve un oggetto di tipo LocalDateTime
                Timestamp ts = rs.getTimestamp("dataAppello");
                Appello app = new Appello(rs.getLong("id"), rs.getLong("materiaId"), rs.getLong("docenteId"), ts.toLocalDateTime(), rs.getString("aula"), rs.getInt("postiMax"), rs.getString("stato"));
                listaAppelli.add(app);
            }

        } catch (SQLException e) {
            System.out.println("Problema di tipo SQLException");
            e.printStackTrace();
        }
        return listaAppelli;
    }

    public List<Appello> listAppelliPerStudente(long studenteId) throws SQLException {
        List<Appello> appelliStudente;
        String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, posti_max AS postiMax, stato\n" + "FROM appello\n" + "WHERE stato = 'APERTO' AND data_esame >= NOW() AND id_professore = ? \n" + "ORDER BY dataAppello ASC\n";
        try (Connection cn = GestoreDB.getConnection(); Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //Mostra gli appelli relativi ad un docente FATTO
    public List<Appello> listAppelliPerDocenti(long docenteId) throws SQLException {
        List<Appello> appelliDocenti = new ArrayList<>();
        String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, posti_max AS postiMax, stato\n" + "FROM appello\n" + "WHERE stato = 'APERTO' AND data_esame >= NOW() AND id_professore = ? \n" + "ORDER BY dataAppello ASC\n";
        try (Connection cn = GestoreDB.getConnection(); PreparedStatement ps = cn.prepareStatement(sql)) {
            ps.setLong(1, docenteId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    //creo un oggetto di tipo TimeStamp perchè nel Db abbiamo DATETIME come valore e con LocalData
                    //andiamo a troncare l'orario altrimenti, quindi ci serve un oggetto di tipo LocalDateTime
                    Timestamp ts = rs.getTimestamp("dataAppello");
                    Appello app = new Appello(rs.getLong("id"), rs.getLong("materiaId"), rs.getLong("docenteId"), ts.toLocalDateTime(), rs.getString("aula"), rs.getInt("postiMax"), rs.getString("stato"));
                    appelliDocenti.add(app);
                }
            }
        } catch (SQLException e) {
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


        //String sql = "Select studente.nome, studente.cognome, studente.matricola, studente.id, studente.data_nascita " + "From prenotazione Inner Join studente ON prenotazione.id_studente = studente.id " + "Where id_appello= ? AND prenotazione.stato = 'PRENOTATO' " + "Order by prenotazione.prenotato_il";

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
    public boolean cancellazionePrenotazioneAppello(long appelloId, long studenteId) throws SQLException {
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
            e.printStackTrace();
            return false;
        }
    }

    //Libretto-esiti
    public List<Esame> mostraLibretto(long studenteId) throws SQLException {
        return null;
    }

    //Docente
    public boolean inserisciVoto(long appelloId, long studenteId, int Voto, boolean lode) throws SQLException {
        return false;
    }

    public long creaAppello(long corsoId, LocalDateTime dataEsame, String aula) throws SQLException {
        return 0;
    }

    // Utilizzato dal Professore, serve a settare lo stato chiuso ad un appello
    public boolean chiudiAppello(long appelloId) throws SQLException {
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
            }else {
                System.out.println("Chiusura non effettuata");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void main(String[] args) {

        String nome = "Raul";
        String cognome = "Petruzza";
        String dataDiNascita = "2000/09/22";
        String username = "raulpet";
        String password = "pass1234";
        Utente u = new Utente();

        UniDAO ud = new UniDAO();
        try {
            u = ud.registrazione(nome, cognome, dataDiNascita, username, password);
            System.out.println("Registrazione effettuata con successo, benvenuto " + u.getUsername());
        } catch (SQLException e) {
            System.out.println("Non ci sono riuscito");
            e.printStackTrace();
        }

        /*
            UniDAO ud = new UniDAO();
            List<Appello> ap;
            List<Studente> lS;
            long appelloId = 4;

            try {
                ap = ud.listaAppelliAperti();
                for (Appello a : ap) {
                    System.out.println(a);
                }

                System.out.println("mostra gli iscritti ad un appello: ");
                lS = ud.listIscrittiAppello(appelloId);
                for (Studente st : lS) {
                    System.out.println(st);
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }*/

    }
}