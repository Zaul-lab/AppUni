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


    public Utente login(String nomeUtente, String password) throws SQLException {
        return null;
    }

    public Utente registrazione(String username, String password, String matricola, String nome, String cognome, LocalDate dataNascita) throws SQLException {
        return null;
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
