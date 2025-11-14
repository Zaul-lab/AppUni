package it.universita.model;


import java.time.LocalDate;

public class Utente extends Persona {
    private long id;
    private String username;
    private String ruolo;


    public Utente(long id, String username, String ruolo, String nome, String cognome, LocalDate dataNascita){
        super(nome,cognome,dataNascita);
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
