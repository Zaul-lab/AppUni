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

    public Persona login(String nomeUtente, String password) throws SQLException {
        return null;
    }

    private String hashPassword(String password) {
        return BCrypt.hashpw(password, BCrypt.gensalt(12));
    }


    public Utente registrazione(String nome, String cognome, String dataDiNascita, String username, String password) throws SQLException {
        long utenteId = 0;
        final Ruolo ruolo = username.endsWith(".docente") ? Ruolo.PROFESSORE : Ruolo.STUDENTE;
        LocalDate dataNascita = Persona.parseData(dataDiNascita);
        //Verifica che username contenga .docente
        String sqlUtente = "INSERT  INTO utente (username,password_hash,ruolo) VALUES (?,?,?)";
        String sqlStudente = "INSERT INTO studente(id,nome, cognome, data_nascita) VALUES (?,?,?,?)";
        String sqlProfessore = "INSERT INTO professore(id,nome, cognome, data_nascita) VALUES (?,?,?,?)";
        try (Connection cn = GestoreDB.getConnection()) {
            cn.setAutoCommit(false);
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
                        if (psProf.executeUpdate()!= 1) throw new SQLException("Inserimento professore non riuscito");
                    }
                } else {
                    try (PreparedStatement psStudente = cn.prepareStatement(sqlStudente)) {
                        psStudente.setLong(1, utenteId);
                        psStudente.setString(2, nome);
                        psStudente.setString(3, cognome);
                        psStudente.setDate(4, Date.valueOf(dataNascita));
                        if (psStudente.executeUpdate() != 1) throw new SQLException("Inserimento studente non riuscito");
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
            }finally {
                cn.setAutoCommit(true);
            }
        }
            return new Utente(utenteId, username, ruolo.name());
    }

        // Appelli in lettura
        public List<Appello> listaAppelliAperti () throws SQLException {
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

        public List<Appello> listAppelliPerStudente ( long studenteId) throws SQLException {
            List<Appello> appelliStudente;
            String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, posti_max AS postiMax, stato\n" +
                    "FROM appello\n" +
                    "WHERE stato = 'APERTO' AND data_esame >= NOW() AND id_professore = ? \n" +
                    "ORDER BY dataAppello ASC\n";
            try (Connection cn = GestoreDB.getConnection();
                 Statement st = cn.createStatement();
                 ResultSet rs = st.executeQuery(sql)) {
            } catch (SQLException e) {

            }

            return null;
        }

        public List<Appello> listAppelliPerDocenti ( long docenteId) throws SQLException {
            List<Appello> appelliDocenti = new ArrayList<>();
            String sql = "SELECT id, id_materia AS materiaId, id_professore AS docenteId, data_esame AS dataAppello, aula, posti_max AS postiMax, stato\n" +
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
                                rs.getString("aula"), rs.getInt("postiMax"), rs.getString("stato"));
                        appelliDocenti.add(app);
                    }
                }
            } catch (SQLException e) {
            }
            return appelliDocenti;
        }

        public List<Studente> listIscrittiAppello ( long appelloId){
            List<Studente> studentiIscritti = new ArrayList<>();

            String sql = "Select studente.nome, studente.cognome, studente.matricola, studente.id, studente.data_nascita " +
                    "From prenotazione Inner Join studente ON prenotazione.id_studente = studente.id " +
                    "Where id_appello= ? AND prenotazione.stato = 'PRENOTATO' " +
                    "Order by prenotazione.prenotato_il";

            try (Connection cn = GestoreDB.getConnection();
                 PreparedStatement ps = cn.prepareStatement(sql)
            ) {
                ps.setLong(1, appelloId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        Studente s = new Studente(rs.getString("nome"), rs.getString("cognome"), rs.getDate("data_nascita").toLocalDate(), rs.getString("matricola"));
                        studentiIscritti.add(s);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return studentiIscritti;
        }

        // Prenotazioni (transazioni)
        public void prenotazioneAppello ( long appelloId, long studenteId) throws SQLException {
        }

        public void cancellazionePrenotazioneAppello ( long appelloId, long studenteId) throws SQLException {
        }

        //Libretto-esiti
        public List<Esame> mostraLibretto ( long studenteId) throws SQLException {
            return null;
        }

        //Docente
        public boolean inserisciVoto ( long appelloId, long studenteId, int Voto, boolean lode) throws SQLException {
            return false;
        }

        public int verbalizzaAppello ( long appelloId) throws SQLException {
            return 0;
        }

        public long creaAppello (long corsoId, LocalDateTime dataEsame, String aula, int postiMax) throws SQLException {
            return 0;
        }

        public boolean chiudiAppello ( long appelloId) throws SQLException {
            return false;
        }

        public static void main (String[]args){
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
        }

    }
