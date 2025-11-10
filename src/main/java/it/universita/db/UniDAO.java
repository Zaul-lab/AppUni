package it.universita.db;

import it.universita.model.Appello;
import it.universita.model.Esame;
import it.universita.model.Utente;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class UniDAO {

    public Utente registrazione(String username, String password, Utente.Ruolo ruolo, String nome, String cognome, LocalDate dataNascita) {
        String qPersona = "INSERT INTO persone (nome, cognome, data_nascita) VALUES (?, ?, ?)";
        String qUtente  = "INSERT INTO utenti (username, password, ruolo, persona_id) VALUES (?, ?, ?, ?)";

        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement p1 = cn.prepareStatement(qPersona, Statement.RETURN_GENERATED_KEYS)) { //Statement.RETURN_GENERATED_KEYS serve per generare un ID in automatico

            // 1)Inserimento in 'persone'
            p1.setString(1, nome);
            p1.setString(2, cognome);
            p1.setDate(3, Date.valueOf(dataNascita));

            int r1 = p1.executeUpdate(); //esegue l'inserimento
            if (r1 == 0) { //se non si inserisce nessuna riga
                System.out.println("Registrazione: insert in 'persone' fallito.");
                return null; //esco
            }
            //recupero l'ID autogenerato per la persona
            long personaId;
            try (ResultSet k1 = p1.getGeneratedKeys()) { //contiene le chiavi generate nell'INSERT
                if (!k1.next()) { //non c'è la riga e quindi niente ID
                    System.out.println("Registrazione: nessun id generato per 'persone'.");
                    return null;
                }
                personaId = k1.getLong(1); //prima colonna =ID generato
            }
            // 2) Inserimento 'utenti' usando "persona_id"
            try (PreparedStatement p2 = cn.prepareStatement(qUtente, Statement.RETURN_GENERATED_KEYS)) {
                p2.setString(1, username);
                p2.setString(2, password);
                p2.setString(3, ruolo.name());     // 'STUDENTE' / 'PROFESSORE'
                p2.setLong(4, personaId);

                int r2 = p2.executeUpdate(); //esegue l'INSERT
                if (r2 == 0) {
                    System.out.println("Registrazione: insert in 'utenti' fallito.");
                    return null;
                }
                //recupero l'ID autogenerato per l'utente
                long utenteId = -1L;
                try (ResultSet k2 = p2.getGeneratedKeys()) {
                    if (k2.next()) utenteId = k2.getLong(1);
                }
                //costruisco l'utente
                Utente u = new Utente(username, password, ruolo, personaId, utenteId);
                u.setUsername(username);
                u.setRuolo(ruolo);
                u.setPersonaId(personaId);
                System.out.println("Registrazione completata.");
                return u;
            }

        } catch (SQLException e) {
            System.out.println("Errore registrazione: " + e.getMessage());
            return null;
        }
    }

    // LOGIN: username + password (ritorna Utente o null)
    public Utente login(String username, String password) {
        String q = "SELECT id, username, ruolo, persona_id FROM utenti WHERE username = ? AND password = ?";

        try (Connection cn = GestoreDB.getConnection();
             PreparedStatement ps = cn.prepareStatement(q)) {

            ps.setString(1, username);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) { //esegue la select
                if (!rs.next()) { //se non c'è nessuna riga le credenziali sono errate
                    System.out.println("Credenziali non valide.");
                    return null;
                }

                Utente u = new Utente(
                        rs.getString("username"), rs.getString("password"),
                        Utente.Ruolo.valueOf(rs.getString("ruolo")),
                        rs.getLong("persona_id"),
                        rs.getLong("id")
                );
                System.out.println("Accesso effettuato con successo.");
                return u;
            }
        } catch (SQLException e) {
            System.out.println("Errore login: " + e.getMessage());
            return null;
        }
    }


    // Appelli in lettura
    public List<Appello> listaAppelliAperti() throws SQLException {
        List<Appello> listaAppelli = new ArrayList<>();
        String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, posti_max AS postiMax, stato\n" +
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
                        rs.getString("aula"), rs.getInt("postiMax"), rs.getString("stato"));
                listaAppelli.add(app);
            }

        } catch (SQLException e) {
            System.out.println("Problema di tipo SQLException");
            e.printStackTrace();
        }
        return listaAppelli;
    }

    public List<Appello> listAppelliPerStudente(long studenteId) throws SQLException {
    return null;
    }

    public List<Appello> listAppelliPerDocenti(long docenteId) throws SQLException {
        return null;
    }

    public List<Utente> listIscrittiAppello(long appelloId) throws SQLException {
        return null;
    }

    // Prenotazioni (transazioni)
    public void prenotazioneAppello(long appelloId, long studenteId) throws SQLException {
    }

    public void cancellazionePrenotazioneAppello(long appelloId, long studenteId) throws SQLException {
    }

    //Libretto-esiti
    public List<Esame> mostraLibretto(long studenteId) throws SQLException {
        return null;
    }

    //Docente
    public boolean inserisciVoto(long appelloId, long studenteId, int Voto, boolean lode) throws SQLException {
        return false;
    }

    public int verbalizzaAppello(long appelloId) throws SQLException {
        return 0;
    }

    public long creaAppello(long corsoId, LocalDateTime dataEsame, String aula, int postiMax) throws SQLException {
        return 0;
    }

    public boolean chiudiAppello(long appelloId) throws SQLException {
        return false;
    }

    public static void main(String[] args) {
        UniDAO ud = new UniDAO();
        List<Appello> ap;
        try{
            ap = ud.listaAppelliAperti();

            for(Appello a : ap){
                System.out.println(a);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
