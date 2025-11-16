package it.universita.client;

import it.universita.config.Config;
import it.universita.config.ConfigSAXParser;
import it.universita.model.Studente;
import it.universita.model.Utente;

import java.net.Socket;
import java.util.Scanner;

public class ClientMainCopia {
    public static void main(String[] args) throws Exception {


        Scanner tastiera = new Scanner(System.in);
        Config config = ConfigSAXParser.fromXmlFile("config.xml");
        ClientTCP ctcp = new ClientTCP(config.getClientHost(), config.getClientPort());

        System.out.println("""
                Benvenuto:
                Digita 1 per la registrazione
                Digita 2 per il login
                """);

        switch (tastiera.nextLine().trim()){
            case "1" ->{
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
                Utente u = ctcp.registrazione(nome,cognome, dataNascita, username, password);
                Menu m = new Menu(ctcp, u.getId());
                if(u.getRuolo().equalsIgnoreCase("STUDENTE")){
                    m.menuStudente();
                }else {
                    m.menuProfessore();
                }
            }
            case "2" ->{
                System.out.println("Digita il tuo username");
                String username = tastiera.nextLine();
                System.out.println("Digita la tua password");
                String password = tastiera.nextLine();
                Utente u = ctcp.login(username, password);
                Menu m = new Menu(ctcp, u.getId());

                if(u.getRuolo().equalsIgnoreCase("STUDENTE")){
                    m.menuStudente();
                }else {
                    m.menuProfessore();
                }
            }
        }
    }
}
