package it.universita.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public abstract class Persona {
    String nome;
    String cognome;
    LocalDate dataDiNascita;

    public Persona(){}

    public Persona(String nome, String cognome, LocalDate dataDiNascita) {
        if (nome == null) throw new IllegalArgumentException("nome vuoto");
        if (cognome == null) throw new IllegalArgumentException("cognome vuoto");
        if (dataDiNascita == null) throw new IllegalArgumentException("data nulla");
        this.nome = nome;
        this.cognome = cognome;
        this.dataDiNascita = dataDiNascita;
    }

     public String getNome(){
        return this.nome;
    }

     public String getCognome() {
        return this.cognome;
    }

     public LocalDate getDataDiNascita() {
        return this.dataDiNascita;
    }

    @Override
     public String toString() {
        return "Persona{" +
                "nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", dataDiNascita=" + dataDiNascita +
                '}';
    }
//Metodo per convertire l'età da String a LocalDate
    public static LocalDate parseData(String s) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy/MM/dd"); //Creiamo un oggetto come modello di comparazione con l'input dell'utente
        try {
            return LocalDate.parse(s.trim(), fmt);
        } catch (DateTimeParseException e) {
            System.out.println("Formato data non valido. Usa il formato yyyy/MM/dd.");
            return null;
        }
    }
}
