package it.universita.model;

import java.time.LocalDate;

public class Studente extends Persona {

    private String matricola;
    private long id;

    public Studente(String nome, String cognome, LocalDate dataDiNascita, String matricola, long id) {
        super(nome, cognome, dataDiNascita);
        this.matricola = matricola;
        this.id = id;
    }

    public String getMatricola() {
        return this.matricola;
    }

    @Override
    public String toString() {
        return "Nome: " + super.getNome() +" Cognome: " + super.getCognome() +" Data di nascita: " + super.getDataDiNascita() +
                " matricola: " + this.matricola + "." + " idUtente: " + this.id;
    }
}
