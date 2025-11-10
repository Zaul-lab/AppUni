package it.universita.model;

import java.time.LocalDate;

public class Professore extends Persona {

    private String idProfessore;

    public Professore(String nome, String cognome, LocalDate dataDiNascita, String idProfessore) {
        super(nome, cognome, dataDiNascita);
        this.idProfessore = idProfessore;
    }

    public String getidProfessore() {
        return this.idProfessore;
    }

    @Override
    public String toString() {
        return "Nome: " + super.getNome() +" Cognome: " + super.getCognome() +" Data di nascita: " + super.getDataDiNascita() +
                " matricola: " + this.idProfessore + ".";
    }
}
