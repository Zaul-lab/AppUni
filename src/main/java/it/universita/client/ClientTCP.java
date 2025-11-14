package it.universita.client;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import it.universita.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

public class ClientTCP {
    private String serverAdress;
    private int serverPort;
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private Gson gson;

    // ===== CONNETTIVITÀ =====
    public ClientTCP(String serverAdress, int serverPort) throws IOException {
        this.serverAdress = serverAdress;
        this.serverPort = serverPort;
        this.gson = new Gson();
        this.socket = new Socket(serverAdress, serverPort);
        this.out = new PrintWriter(this.socket.getOutputStream(), true);
        this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
    }

    private JsonObject sendRequest(JsonObject request) throws IOException {
        String json = gson.toJson(request);
        out.println(json);
        String response = this.in.readLine();
        if (response == null) {
            throw new IOException("Connessione chiusa dal server");
        }
        return gson.fromJson(response, JsonObject.class);
    }

    public void close() throws IOException {
        this.socket.close();
    }

    // ===== LOGIN / REGISTRAZIONE =====
    public Utente login(String username, String password) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "login");
        req.addProperty("username", username);
        req.addProperty("password", password);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Accesso fallito: " + messaggio);
            } else {
                System.out.println("Accesso fallito(errore sconosciuto)");
            }
            return null;
        }
        JsonObject utente = res.getAsJsonObject("utente");
        if (utente == null) {
            //se ci sono errori lato server:
            throw new IOException("Risposta server non valida");
        }
        Utente u = gson.fromJson(utente, Utente.class);
        return u;
    }

    public Utente registrazione(String nome, String cognome, String dataDiNascita, String username, String password) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "registrazione");
        req.addProperty("nome", nome);
        req.addProperty("cognome", cognome);
        req.addProperty("data_nascita", dataDiNascita);
        req.addProperty("username", username);
        req.addProperty("password", password);
        //mandiamo la richiesta al server
        JsonObject res = sendRequest(req);
        //controllo se l'operazione è andata a buon fine
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Registrazione fallita: " + messaggio);
            } else {
                System.out.println("Registrazione fallita(errore sconosciuto)");
            }
            return null;
        }
        //se tutto non ci sono errori, prendo l'oggetto utente dalla risposta:
        JsonObject utente = res.getAsJsonObject("utente");
        if (utente == null) {
            //se ci sono errori lato server:
            throw new IOException("Risposta server non valida");
        }
        Utente u = gson.fromJson(utente, Utente.class);
        return u;
    }

    // ===== APPELLI (LETTURA) =====
    public List<Appello> listaAppelliAperti() throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "listaAppelliAperti");
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile visualizzare gli Appelli: " + messaggio);
            } else {
                System.out.println("Impossibile visualizzare gli Appelli(errore sconosciuto)");
            }
            return Collections.emptyList(); //restituisce una lista vuota
        }
        JsonArray appelli = res.getAsJsonArray("appelli");
        if (appelli == null) {
            throw new IOException("Risposta server non valida");
        }
        //metodo per convertire una JsonArray in una List<Appello>
        Type listType = new TypeToken<List<Appello>>(){}.getType();
        List<Appello> listaAppelli = gson.fromJson(appelli, listType);
        return listaAppelli;
    }

    public List<Appello> listaAppelliPerDocenti(long docenteId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "listaAppelliPerDocenti");
        req.addProperty("docenteId", docenteId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile visualizzare gli Appelli: " + messaggio);
            } else {
                System.out.println("Impossibile visualizzare gli Appelli(errore sconosciuto)");
            }
            return Collections.emptyList(); //restituisce una lista vuota
        }
        JsonArray appelliDocente = res.getAsJsonArray("appelliDocente");
        if (appelliDocente == null) {
            throw new IOException("Risposta server non valida");
        }
        Type listType = new TypeToken<List<Appello>>(){}.getType();
        List<Appello> listaAppelliPerDocenti = gson.fromJson(appelliDocente, listType);
        return listaAppelliPerDocenti;
    }

    public List<Appello> listAppelliPrenotatiDaStudente(long studenteId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "listAppelliPrenotatiDaStudente");
        req.addProperty("studenteId", studenteId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile visualizzare gli Appelli Prenotati: " + messaggio);
            } else {
                System.out.println("Impossibile visualizzare gli Appelli Prenotati(errore sconosciuto)");
            }
            return Collections.emptyList(); //restituisce una lista vuota
        }
        JsonArray appelliPrenotatiDaStudente = res.getAsJsonArray("appelliPrenotatiDaStudente");
        if (appelliPrenotatiDaStudente == null) {
            throw new IOException("Risposta server non valida");
        }
        Type listType = new TypeToken<List<Appello>>(){}.getType();
        List<Appello> listaAppelliPrenotatiDaStudente = gson.fromJson(appelliPrenotatiDaStudente, listType);
        return  listaAppelliPrenotatiDaStudente;
    }

    public List<Studente> listIscrittiAppello(long appelloId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "listIscrittiAppello");
        req.addProperty("appelloId", appelloId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile visualizzare gli Studenti Prenotati: " + messaggio);
            } else {
                System.out.println("Impossibile visualizzare gli Studenti Prenotati(errore sconosciuto)");
            }
            return Collections.emptyList(); //restituisce una lista vuota
        }
        JsonArray listaIscritti  = res.getAsJsonArray("iscrittiAppello");
        if (listaIscritti == null) {
            throw new IOException("Risposta server non valida");
        }
        Type listType = new TypeToken<List<Studente>>(){}.getType();
        List<Studente> listaIscrittiAppello  = gson.fromJson(listaIscritti, listType);
        return listaIscrittiAppello;
    }

    // ===== PRENOTAZIONI STUDENTE =====
    public boolean prenotazioneAppello(long appelloId, long studenteId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "prenotazioneAppello");
        req.addProperty("appelloId", appelloId);
        req.addProperty("studenteId", studenteId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile effettuare la prenotazione: " + messaggio);
            } else {
                System.out.println("Impossibile effettuare la prenotazione(errore sconosciuto)");
            }
            return false;
        }
        return true;
    }

    public boolean cancellazionePrenotazioneAppello(long appelloId, long studenteId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "cancelPrenotazioneAppello");
        req.addProperty("appelloId", appelloId);
        req.addProperty("studenteId", studenteId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile cancellare la prenotazione: " + messaggio);
            } else {
                System.out.println("Impossibile cancellare la prenotazione(errore sconosciuto)");
            }
            return false;
        }
        return true;
    }


    // ===== LIBRETTO =====
    public Libretto mostraLibretto(long studenteId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "mostraLibretto");
        req.addProperty("studenteId", studenteId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile visualizzare il libretto: " + messaggio);
            } else {
                System.out.println("Impossibile visualizzare il libretto(errore sconosciuto)");
            }
            return null;
        }
        JsonObject libretto = res.getAsJsonObject("libretto");
        if (libretto == null) {
            //se ci sono errori lato server:
            throw new IOException("Risposta server non valida");
        }
        Libretto l = gson.fromJson(libretto, Libretto.class);
        return l;
    }

    // ===== FUNZIONI DOCENTE =====
    public boolean creaAppello(long materiaId, long professoreId, LocalDateTime dataEsame, String aula) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "creaAppello");
        req.addProperty("materiaId", materiaId);
        req.addProperty("professoreId", professoreId);
        req.addProperty("dataEsame", dataEsame.toString());
        req.addProperty("aula", aula);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile creare l'appello: " + messaggio);
            } else {
                System.out.println("Impossibile creare l'appello(errore sconosciuto)");
            }
            return false;
        }
        return true;

    }

    public boolean chiudiAppello(long appelloId) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "chiudiAppello");
        req.addProperty("appelloId", appelloId);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile chiudere l'appello: " + messaggio);
            } else {
                System.out.println("Impossibile chiudere l'appello(errore sconosciuto)");
            }
            return false;
        }
        return true;
    }

    public boolean inserisciVoto(long prenotazioneId,long professoreId, long studenteId, int voto, boolean lode) throws IOException {
        JsonObject req = new JsonObject();
        req.addProperty("action", "inserisciVoto");
        req.addProperty("prenotazioneId", prenotazioneId);
        req.addProperty("professoreId", professoreId);
        req.addProperty("studenteId", studenteId);
        req.addProperty("voto", voto);
        req.addProperty("lode", lode);
        JsonObject res = sendRequest(req);
        boolean success = res.get("success").getAsBoolean();
        if (!success) {
            //leggiamo messaggio di errore e lo gestiamo
            if (res.has("messaggio")) {
                String messaggio = res.get("messaggio").getAsString();
                System.out.println("Impossibile inserire il voto: " + messaggio);
            } else {
                System.out.println("Impossibile inserire il voto(errore sconosciuto)");
            }
            return false;
        }
        return true;
    }
}
