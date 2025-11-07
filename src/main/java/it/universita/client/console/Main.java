package it.universita.client.console;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        System.out.println("Benvenuti in AppUni!");
        Scanner tastiera = new Scanner(System.in);
        while (true) {
            System.out.println("Digita 1 per effettuare il  login");
            System.out.println("Digita 2 per registrarsi");
            int scelta = Integer.parseInt(tastiera.nextLine().trim());
            switch (scelta) {
                case 1:
                    login();
                    break;
                case 2:
                    registrazione();
                    break;
                default:
                    System.out.println("Scelta sbagliata riprova");
                    break;
            }
        }
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
        }
        if (scelta.equalsIgnoreCase("Professore")) {
            System.out.println("Digita il nome utente");
            String nomeUtente = tastiera.nextLine();
            System.out.println("Digita la password");
            String password = tastiera.nextLine();
            /*
             *Metodo o logica per ricerca nel database, (eventualmente chiamando un metodo della classe ProfessoreDAO?)
             *Se il l'utente è presente effettuato = true
             */
        }


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
        }
        if (scelta.equalsIgnoreCase("Professore")) {
            System.out.println("Digita il nome utente");
            String nomeUtente = tastiera.nextLine();
            System.out.println("Digita la password");
            String password = tastiera.nextLine();
            /*
             *Metodo o logica per ricerca nel database, (eventualmente chiamando un metodo della classe ProfessoreDAO?)
             *Se il l'utente è presente effettuato = true
             */
        }

        return effettuato;
    }

}
