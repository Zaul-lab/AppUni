package it.universita.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import it.universita.config.LocalDateAdapter;
import it.universita.config.LocalDateTimeAdapter;
import it.universita.db.UniDAO;
import it.universita.model.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Gson gson;
    private BufferedReader in;
    private PrintWriter out;
    private final UniDAO uniDAO;
    private Utente utenteloggato; //null finchè non fa il login(un utente per ogni connessione)

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.gson = new GsonBuilder().registerTypeAdapter(LocalDate.class, new LocalDateAdapter()).registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter()).create();
        this.uniDAO = new UniDAO();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String line;
            while ((line = in.readLine()) != null) {
                JsonObject request = gson.fromJson(line, JsonObject.class);
                if (request == null) {
                    //richiesta non valida
                    JsonObject error = new JsonObject();
                    error.addProperty("success", false);
                    error.addProperty("messaggio", "Richiesta non valida");
                    out.println(gson.toJson(error));
                    continue;
                }
                //menu richieste, passiamo la richiesta che ci arriva dal client al metodo handleRequest che smista la richiesta al metodo giusto
                //e ci torna la risposta in formato json
                JsonObject response = handleRequest(request);
                out.println(gson.toJson(response));
            }
        } catch (SocketException e) {
            System.out.println("Client disconnesso zio" + socket.getRemoteSocketAddress());
        } catch (Exception e) {
            System.err.println("Errore nella gestione del cliente: " + socket.getRemoteSocketAddress() + ":" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private JsonObject handleRequest(JsonObject req) {
        JsonObject res = new JsonObject();
        try {
            String action = req.get("action").getAsString();

            switch (action) {
                case "registrazione":
                    return handleRegistrazione(req);
                case "login":
                    return handleLogin(req);
                case "listaAppelliAperti":
                    return handleListaAppelliAperti(req);
                case "listaAppelliPerDocenti":
                    return handleListaAppelliPerDocenti(req);
                case "listAppelliPrenotatiDaStudente":
                    return handleListAppelliPrenotatiDaStudente(req);
                case "listIscrittiAppello":
                    return handleListIscrittiAppello(req);
                case "prenotazioneAppello":
                    return handlePrenotazioneAppello(req);
                case "cancelPrenotazioneAppello":
                    return handleCancellazionePrenotazioneAppello(req);
                case "mostraLibretto":
                    return handleMostraLibretto(req);
                case "creaAppello":
                    return handleCreaAppello(req);
                case "chiudiAppello":
                    return handleChiudiAppello(req);
                case "inserisciVoto":
                    return handleInserisciVoto(req);
                case "mostraMaterieInsegnate":
                    return handleMostraMaterieInsegnate(req);
                default:
                    res.addProperty("success", false);
                    res.addProperty("messaggio", "Azione sconosciuta: " + action);
                    return res;
            }

        } catch (SQLException e) {
            // Errore di database
            e.printStackTrace(); // ok tenerlo qui
            res.addProperty("success", false);
            res.addProperty("messaggio", "Errore di database. Riprova più tardi.");
            return res;

        } catch (IllegalArgumentException e) {
            // Parametri mancanti / sbagliati
            res.addProperty("success", false);
            res.addProperty("messaggio", "Richiesta non valida: " + e.getMessage());
            return res;

        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("success", false);
            res.addProperty("messaggio", "Errore server: " + e.getMessage());
            return res;
        }
    }

    private JsonObject handleRegistrazione(JsonObject req) throws SQLException {
        String nome = req.get("nome").getAsString();
        String cognome = req.get("cognome").getAsString();
        String dataDiNascita = req.get("data_nascita").getAsString();
        String username = req.get("username").getAsString();
        String password = req.get("password").getAsString();
        JsonObject res = new JsonObject();

        Utente u = uniDAO.registrazione(nome, cognome, dataDiNascita, username, password);
        this.utenteloggato = u;
        System.out.println("SERVER - utente restituito da DAO: " + u);//log per debug

        res.addProperty("success", true);
        res.addProperty("messaggio", "Registrazione con successo");
        res.add("utente", gson.toJsonTree(u));
        return res;
    }

    private JsonObject handleLogin(JsonObject req) throws SQLException {
        String username = req.get("username").getAsString();
        String password = req.get("password").getAsString();
        JsonObject res = new JsonObject();

        Utente u = uniDAO.login(username, password);

        if (u == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Credenziali errate : Utente o password errati");
            return res;
        }

        this.utenteloggato = u;
        res.addProperty("success", true);
        res.add("utente", gson.toJsonTree(u));
        return res;
    }

    private JsonObject handleListaAppelliAperti(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        List<Appello> appelli = uniDAO.listaAppelliAperti(this.utenteloggato.getId());

        //for (Appello a : appelli) System.out.println(a);

        res.addProperty("success", true);
        res.add("appelli", gson.toJsonTree(appelli));
        return res;
    }

    private JsonObject handleListAppelliPrenotatiDaStudente(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"STUDENTE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo gli studenti possono visualizzare i propri appelli prenotati");
            return res;
        }

        List<Appello> appelli = uniDAO.listAppelliPrenotatiDaStudente(this.utenteloggato.getId());
        res.addProperty("success", true);
        res.add("appelli", gson.toJsonTree(appelli));
        return res;
    }

    private JsonObject handleListaAppelliPerDocenti(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"PROFESSORE".equals(utenteloggato.getRuolo())) { // assumo ruolo.name()
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo i docenti possono visualizzare i propri appelli");
            return res;
        }

        List<Appello> appelli = uniDAO.listAppelliPerDocenti(this.utenteloggato.getId());
        res.addProperty("success", true);
        res.add("appelli", gson.toJsonTree(appelli));
        return res;
    }

    private JsonObject handleListIscrittiAppello(JsonObject req) throws SQLException {
       JsonObject res = new JsonObject();
        if (this.utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"PROFESSORE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo i docenti possono visualizzare gli iscritti");
            return res;
        }
        long appelloId = req.get("appelloId").getAsLong();


        List<StudenteIscrittoAppello> studentiIscritti = uniDAO.listIscrittiAppello(appelloId);
        res.addProperty("success", true);
        res.add("iscrittiAppello", gson.toJsonTree(studentiIscritti));
        return res;
    }

    private JsonObject handlePrenotazioneAppello(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();
        // controllo login
        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }
        // controllo ruolo
        if (!"STUDENTE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo gli studenti possono prenotare un appello");
            return res;
        }
        // leggo SOLO l'id dell'appello dal client
        long appelloId = req.get("appelloId").getAsLong();

        // lo studenteId lo prendo dall'utente loggato, non dal JSON
        long studenteId = utenteloggato.getId();

        // chiamo il DAO
        boolean ok = uniDAO.prenotazioneAppello(appelloId, studenteId);

        res.addProperty("success", ok);
        if (!ok) {
            res.addProperty("messaggio", "Prenotazione non effettuata");
        }
        return res;
    }

    private JsonObject handleCancellazionePrenotazioneAppello(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"STUDENTE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo gli studenti possono cancellare una prenotazione");
            return res;
        }

        long appelloId = req.get("appelloId").getAsLong();
        long studenteId = utenteloggato.getId();

        boolean ok = uniDAO.cancellazionePrenotazioneAppello(appelloId, studenteId);
        res.addProperty("success", ok);
        if (!ok) {
            res.addProperty("messaggio", "Cancellazione non effettuata");
        }
        return res;
    }

    private JsonObject handleMostraLibretto(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        // 1) controllo login
        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        // 2) controllo ruolo
        if (!"STUDENTE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo gli studenti possono visualizzare il libretto");
            return res;
        }

        // 3) prendo l'id dello studente dalla sessione, NON dal client
        long studenteId = utenteloggato.getId();

        // 4) chiamo il DAO
        Libretto libretto = uniDAO.mostraLibretto(studenteId);

        if (libretto == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Libretto non trovato");
            return res;
        }

        res.addProperty("success", true);
        res.add("libretto", gson.toJsonTree(libretto));
        return res;
    }

    private JsonObject handleCreaAppello(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"PROFESSORE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo i docenti possono creare appelli");
            return res;
        }

        long materiaId = req.get("materiaId").getAsLong();
        String dataEsame = req.get("dataEsame").getAsString();
        String aula = req.get("aula").getAsString();

        LocalDateTime dataEsameLocalDateTime = LocalDateTime.parse(dataEsame);

        boolean ok = uniDAO.creaAppello(materiaId, dataEsameLocalDateTime, aula);
        res.addProperty("success", ok);
        if (!ok) {
            res.addProperty("messaggio", "Creazione appello non riuscita");
        }
        return res;
    }

    private JsonObject handleChiudiAppello(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"PROFESSORE".equals(utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo i docenti possono chiudere un appello");
            return res;
        }

        long appelloId = req.get("appelloId").getAsLong();

        boolean ok = uniDAO.chiudiAppello(appelloId);
        res.addProperty("success", ok);
        if (!ok) {
            res.addProperty("messaggio", "Chiusura appello non riuscita");
        }
        return res;
    }

    private JsonObject handleInserisciVoto(JsonObject req) throws SQLException {

        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"PROFESSORE".equals(this.utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo i docenti possono inserire voti");
            return res;
        }

        long prenotazioneId = req.get("prenotazioneId").getAsLong();
        long studenteId = req.get("studenteId").getAsLong();
        int voto = req.get("voto").getAsInt();
        boolean lode = req.get("lode").getAsBoolean();

        boolean ok = uniDAO.inserisciVoto(prenotazioneId, this.utenteloggato.getId(), studenteId, voto, lode);

        res.addProperty("success", ok);
        if (!ok) {
            res.addProperty("messaggio", "Inserimento voto non riuscito");
        }
        return res;
    }

    private JsonObject handleMostraMaterieInsegnate(JsonObject req) throws SQLException {
        JsonObject res = new JsonObject();

        if (utenteloggato == null) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Devi effettuare il login");
            return res;
        }

        if (!"PROFESSORE".equals(this.utenteloggato.getRuolo())) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Solo i docenti possono vedere le materie insegnate");
            return res;
        }

        List<Materia> materie = uniDAO.mostraMaterieInsegnate(this.utenteloggato.getId());

        if (materie.isEmpty()) {
            res.addProperty("success", false);
            res.addProperty("messaggio", "Materie non trovate");
            return res;
        }

        res.addProperty("success", true);
        res.add("materie", gson.toJsonTree(materie));
        return res;
    }

}
