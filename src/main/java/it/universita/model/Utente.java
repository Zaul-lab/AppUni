package it.universita.model;

import com.sun.jna.platform.win32.Netapi32Util;

public class Utente extends Persona {
    private long id;
    private String username;
    private String ruolo;


    public Utente(long id, String username, String ruolo){
        this.id = id;
        this.username = username;
        this.ruolo = ruolo;
    }
    public Utente(){}

    public long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getRuolo() {
        return ruolo;
    }
}
