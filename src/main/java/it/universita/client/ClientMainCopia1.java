package it.universita.client;

import it.universita.config.Config;
import it.universita.config.ConfigSAXParser;
import it.universita.model.Utente;

import java.util.Scanner;

public class ClientMainCopia1 {
    public static void main(String[] args) throws Exception {

        Scanner tastiera = new Scanner(System.in);
        Config config = ConfigSAXParser.fromXmlFile("config.xml");
        ClientTCP ctcp = new ClientTCP(config.getClientHost(), config.getClientPort());

        Utente u = null;

        while (true) {
            System.out.println("""
                    Benvenuto:
                    Digita 1 per la registrazione
                    Digita 2 per il login
                    Digita 3 per chiudere
                    """);

            switch (tastiera.nextLine().trim()) {
                case "1" -> {
                    while (true) {
                        System.out.println("Digita il tuo nome");
                        String nome = tastiera.nextLine().trim();
                        System.out.println("Digita il tuo cognome");
                        String cognome = tastiera.nextLine().trim();
                        System.out.println("Digita la tua data di nascita");
                        String dataNascita = tastiera.nextLine();
                        System.out.println("Digita l'username desiderato");
                        String username = tastiera.nextLine();
                        System.out.println("Digita la password");
                        String password = tastiera.nextLine();
                        u = ctcp.registrazione(nome, cognome, dataNascita, username, password);
                        if (u == null) {
                            System.out.println("Impossibile registrarsi come professore, vuoi riprovare? Si/No: ");
                            if (!"si".equalsIgnoreCase(tastiera.nextLine().trim()))
                                break;
                        } else {
                            Menu m = new Menu(ctcp, u.getId(),tastiera);
                            if (u.getRuolo().equalsIgnoreCase("STUDENTE")) {
                                m.menuStudente();
                            } else {
                                m.menuProfessore();
                            }
                            break;
                        }
                    }
                }
                case "2" -> {
                    while (true) {
                        System.out.println("Digita il tuo username");
                        String username = tastiera.nextLine();
                        System.out.println("Digita la tua password");
                        String password = tastiera.nextLine();
                        u = ctcp.login(username, password);
                        if (u == null) {
                            System.out.println("Credenziali sbagliate, vuoi riprovare? Si/No: ");
                            if (!"si".equalsIgnoreCase(tastiera.nextLine().trim())) {
                                break;
                            }
                        } else {
                            Menu m = new Menu(ctcp, u.getId(),tastiera);
                            if (u.getRuolo().equalsIgnoreCase("STUDENTE")) {
                                m.menuStudente();
                            } else {
                                m.menuProfessore();
                            }
                            break;
                        }
                    }
                }
                case "3" -> {
                    System.out.println("Arrivederci bello!");
                    return;
                }
            }
        }
    }
}
