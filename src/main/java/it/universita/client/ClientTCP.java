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
    private String serverAdress = "localhost";
    private int serverPort = 5555;
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

    }
    public Utente registrazione(String nome, String cognome, String dataNascita, String candidatoUsername, String password) throws IOException{

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
