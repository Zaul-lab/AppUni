package it.universita.client;

import it.universita.model.Persona;

import java.time.LocalDate;
import java.util.Scanner;

public class ConsoleUI {

    public static void main(String[] args) {
        System.out.println("Benvenuti in AppUni!");
        Scanner tastiera = new Scanner(System.in);/*
        while (true) {
            System.out.println("Digita 1 per effettuare il  login");
            System.out.println("Digita 2 per registrarsi");
            String scelta = tastiera.nextLine().trim();
            switch (scelta) {
                case "1" -> {
                    boolean loggato = login();
                    break;
                }
                case "2" -> {
                    registrazione();
                    break;
                }
                default -> {
                    System.out.println("Scelta sbagliata riprova");
                    break;
                }

            }

        }
        */
    }


    public static boolean login() {
        boolean effettuato = false;
        Scanner tastiera = new Scanner(System.in);

        System.out.println("Sei uno studente o un professore?");
        String scelta = tastiera.nextLine().trim();
        if (scelta.equalsIgnoreCase("Studente")) {
            System.out.println("Digita il nome utente");
            String nomeUtente = tastiera.nextLine();
            System.out.println("Digita la password");
            String password = tastiera.nextLine();
            /*
             * Metodo o logica per ricerca nel database, (eventualmente chiamando un metodo della classe StudenteDAO ?)
             * Se il l'utente è presente effettuato = true
             * */
        } else if (scelta.equalsIgnoreCase("Professore")) {
            System.out.println("Digita il nome utente");
            String nomeUtente = tastiera.nextLine();
            System.out.println("Digita la password");
            String password = tastiera.nextLine();
            /*
             *Metodo o logica per ricerca nel database, (eventualmente chiamando un metodo della classe ProfessoreDAO?)
             *Se il l'utente è presente effettuato = true
             */
        } else if (scelta.equalsIgnoreCase("esci")) {
            System.exit(0);
        } else
            System.out.println("Scelta sbagliata riprova");

        return effettuato;
    }


    public static boolean registrazione() {
        boolean effettuato = false;
        Scanner tastiera = new Scanner(System.in);

        System.out.println("Sei uno studente o un professore?");
        String scelta = tastiera.nextLine().trim();
        if (scelta.equalsIgnoreCase("Studente")) {
            System.out.println("Digita il nome utente");
            String nomeUtente = tastiera.nextLine();
            System.out.println("Digita la password");
            String password = tastiera.nextLine();
            /*
             * Metodo o logica per ricerca nel database, (eventualmente chiamando un metodo della classe StudenteDAO ?)
             * Se il l'utente è presente effettuato = true
             * */
        }else if (scelta.equalsIgnoreCase("Professore")) {
            System.out.println("Digita il nome");
            String nome = tastiera.nextLine();
            System.out.println("Digita il cognome");
            String cognome = tastiera.nextLine();
            System.out.println("Inserisci la data di nascita nel formato yyyy/mm/gg: ");
            String etaString = tastiera.nextLine().trim();
            LocalDate eta = Persona.parseData(etaString);
            /*
             *Metodo o logica per ricerca nel database, (eventualmente chiamando un metodo della classe ProfessoreDAO?)
             *Se il l'utente è presente effettuato = true
             */
           // Professore p = new Professore(nome, cognome, eta, id);
        }

        return effettuato;
    }

    //metodo per rendere la stringa dell'età presa da input un oggetto di tipo LocalDate

}
