package it.universita.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.universita.db.UniDAO;
import it.universita.model.Utente;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Gson gson;
    private BufferedReader in;
    private PrintWriter out;
    private final UniDAO uniDAO;
    private Utente utenteloggato; //null finchè non fa il login

    public ClientHandler(Socket socket) throws IOException {
        this.socket = socket;
        this.gson = new Gson();
        this.uniDAO = new UniDAO();
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    @Override
    public void run() {
        try {
            String line;
            while((line = in.readLine()) != null) {
                JsonObject request = gson.fromJson(line, JsonObject.class);
                if(request == null){
                    //richiesta non valida
                    JsonObject error = new JsonObject();
                    error.addProperty("success", false);
                    error.addProperty("message", "Richiesta non valida");
                    out.println(gson.toJson(error));
                    continue;
                }
                //menu richieste, passiamo la richiesta che ci arriva dal client al metodo handleRequest che smista la richiesta al metodo giusto
                //e ci torna la risposta in formato json
                JsonObject response = handleRequest(request);
                out.println(gson.toJson(response));
            }
        } catch (Exception e) {
            System.err.println("Errore nella gestione del cliente: " + socket.getRemoteSocketAddress() + ":" + e.getMessage());
        }finally {
            try{
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
                    return handleCancelPrenotazioneAppello(req);
                case "mostraLibretto":
                    return handleMostraLibretto(req);
                case "creaAppello":
                    return handleCreaAppello(req);
                case "chiudiAppello":
                    return handleChiudiAppello(req);
                case "inserisciVoto":
                    return handleInserisciVoto(req);
                default:
                    res.addProperty("success", false);
                    res.addProperty("messaggio", "Azione sconosciuta: " + action);
                    return res;
            }

        } catch (Exception e) {
            e.printStackTrace();
            res.addProperty("success", false);
            res.addProperty("messaggio", "Errore server: " + e.getMessage());
            return res;
        }
    }

    private JsonObject handlePrenotazioneAppello(JsonObject req) {

    }

    private JsonObject handleRegistrazione(JsonObject req) throws SQLException {
        String nome = req.get("nome").getAsString();
        String cognome = req.get("cognome").getAsString();
        String dataDiNascita = req.get("data_nascita").getAsString();
        String username = req.get("username").getAsString();
        String password = req.get("password").getAsString();

        JsonObject res = new JsonObject();



    }

    private JsonObject handleLogin(JsonObject req) throws SQLException {
        String username = req.get("username").getAsString();
        String password = req.get("password").getAsString();
        JsonObject res = new JsonObject();

        Utente u = uniDAO.login(username, password);

        if(u == null){
            res.addProperty("success", false);
            res.addProperty("messaggio", "Impossibile accedere come: " + username);
            return res;
        }

        this.utenteloggato = u;
        res.addProperty("success", true);
        res.add("utente", gson.toJsonTree(u));
        return res;
    }
}
