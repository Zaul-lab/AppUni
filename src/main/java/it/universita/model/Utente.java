package it.universita.model;


public class Utente extends Persona {
    private long id;
    private String username;
    private String ruolo;


    public Utente(long id, String username, String ruolo){
        this.id = id;
        this.username = username;
        this.ruolo = ruolo;
    }

    public Studente creaStudente(){
        Studente u = new Studente(this.nome,this.cognome,this.dataDiNascita,null,this.id);
        return u;
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
