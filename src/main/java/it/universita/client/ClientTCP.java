package it.universita.client;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.universita.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.List;

public class ClientTCP {
    private String serverAdress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    // ===== CONNETTIVITÀ =====
    public ClientTCP(String serverAdress, int serverPort) throws IOException{
        this.serverAdress = serverAdress;
        this.serverPort = serverPort;
        this.gson = new Gson();
        this.socket = new Socket(serverAdress, serverPort);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    private JsonObject sendRequest(JsonObject request) throws IOException{
        String json = gson.toJson(request);
        out.println(json);
        String response = this.in.readLine();
        return gson.fromJson(response, JsonObject.class);
    }

    public void close() throws IOException{
        this.socket.close();
    }

    // ===== LOGIN / REGISTRAZIONE =====
    public Utente login(String username, String password) throws IOException{
        JsonObject req= new JsonObject();
        req.addProperty("action", "login");
        req.addProperty("username", username);
        req.addProperty("password", password);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if(!success){
            //leggiamo messaggio di errore e lo gestiamo
            if(res.has("messaggio")){
                String messaggio= res.get("messaggio").getAsString();
                System.out.println("Accesso fallito: " + messaggio);
            }else{
                System.out.println("Accesso fallito(errore sconosciuto)");
            }
            return null;
        }
        JsonObject utente = res.getAsJsonObject("utente");
        if(utente == null){
            //se ci sono errori lato server:
            throw new IOException("Risposta server non valida");
        }
        Utente u = gson.fromJson(utente, Utente.class);
        return u;
    }

    public Utente registrazione(String nome, String cognome, String data_nascita, String username, String password) throws IOException{
        JsonObject req = new JsonObject();
        req.addProperty("action", "registrazione");
        req.addProperty("nome", nome);
        req.addProperty("cognome", cognome);
        req.addProperty("data_nascita", data_nascita);
        req.addProperty("username", username);
        req.addProperty("password", password);
        //mandiamo la richiesta al server
        JsonObject res = sendRequest(req);
        //controllo se l'operazione è andata a buon fine
        boolean success = res.get("success").getAsBoolean();
        if(!success){
            //leggiamo messaggio di errore e lo gestiamo
            if(res.has("messaggio")){
                String messaggio= res.get("messaggio").getAsString();
                System.out.println("Registrazione fallita: " + messaggio);
            }else{
                System.out.println("Registrazione fallita(errore sconosciuto)");
            }
            return null;
        }
        //se tutto non ci sono errori, prendo l'oggetto utente dalla risposta:
        JsonObject utente = res.getAsJsonObject("utente");
        if(utente == null){
            //se ci sono errori lato server:
            throw new IOException("Risposta server non valida");
        }
        Utente u = gson.fromJson(utente, Utente.class);
        return u;
    }

    // ===== APPELLI (LETTURA) =====
    public List<Appello> listaAppelliAperti() throws IOException{

    }
    public List<Appello> listaAppelliDocente(long docenteId) throws IOException{

    }
    public List<Appello> listaAppelliStudente(long studenteId) throws IOException{

    }

    // ===== PRENOTAZIONI STUDENTE =====
    public boolean prenotazioneAppello(long appelloId, long studenteId) throws IOException{

    }
    public boolean cancellazionePrenotazioneAppello(long appelloId, long studenteId) throws IOException{

    }
    public List<Studente> listaIscrittiAppello(long appelloId) throws IOException{

    }

    // ===== LIBRETTO =====
    public Libretto mostraLibretto(long studenteId) throws IOException{

    }

    // ===== FUNZIONI DOCENTE =====
    public boolean creaAppello(long materiaId, long docenteId, LocalDateTime dataEsame, String aula) throws IOException{

    }
    public boolean chiudiAppello(long appelloId) throws IOException{

    }
    public boolean inserisciVoto(long appelloId, long studenteId, int voto, boolean lode) throws IOException{

    }

    // ===== MATERIE =====
    public List<Materia> listaMaterie() throws IOException{

    }
}
