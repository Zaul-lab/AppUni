package it.universita.client.modello;

import java.time.LocalDate;

public class Professore extends Persona {

    private int matricola;

    public Professore(String nome, String cognome, LocalDate dataDiNascita, int matricola) {
        super(nome, cognome, dataDiNascita);
        this.matricola = matricola;
    }

    public int getMatricola() {
        return matricola;
    }

    @Override
    public String toString() {
        return "{" + super.toString() +
                "matricola=" + matricola +
                '}';
    }
}
