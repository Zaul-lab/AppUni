package it.universita.model;

import java.time.LocalDate;

public class Utente {
    public enum Ruolo { STUDENTE, PROFESSORE };
    private Long id; //chiave primaria per l'account
    private String username;
    private String password;
    private Ruolo ruolo;
    private Long personaId; //collegamento a persona

    public Utente(String username, String password, Ruolo ruolo,  Long personaId, Long id) {
        this.username = username;
        this.password = password;
        this.ruolo = ruolo;
        this.personaId = personaId;
        this.id = id;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Ruolo getRuolo() {
        return ruolo;
    }
    public void setRuolo(Ruolo ruolo) {
        this.ruolo = ruolo;
    }
    public Long getPersonaId() {
        return personaId;
    }
    public void setPersonaId(Long personaId) {
        this.personaId = personaId;
    }
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Utente{" +
                "username" + username + "password" + password + "ruolo" + ruolo + "personaId"+ personaId + "id" + id + "}";
    }
}
