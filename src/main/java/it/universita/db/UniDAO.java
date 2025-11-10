package it.universita.db;

import it.universita.model.Appello;
import it.universita.model.Esame;
import it.universita.model.Utente;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class UniDAO {


    public Utente login(String nomeUtente, String password) throws SQLException{}
    public Utente registrazione(String username, String password, String matricola, String nome, String cognome, LocalDate dataNascita) throws SQLException {}


    // Appelli in lettura
    public List<Appello> listaAppelliAperti() throws SQLException {}
    public List<Appello> listAppelliPerStudente(long studenteId) throws SQLException{}
    public List<Appello> listAppelliPerDocenti(long docenteId) throws SQLException {}
    public List<Utente> listIscrittiAppello(long appelloId) throws SQLException {}

    // Prenotazioni (transazioni)
    public void prenotazioneAppello(long appelloId, long studenteId) throws SQLException{}
    public void cancellazionePrenotazioneAppello(long appelloId, long studenteId) throws SQLException{}

    //Libretto-esiti
    public List<Esame> mostraLibretto(long studenteId) throws SQLException{}

    //Docente
    public boolean inserisciVoto(long appelloId, long studenteId,int Voto,boolean lode) throws SQLException{}
    public int verbalizzaAppello(long appelloId) throws SQLException{}
    public long creaAppello(long corsoId, LocalDateTime dataEsame, String aula, int postiMax) throws SQLException{}
    public boolean chiudiAppello(long appelloId) throws SQLException{}


}
