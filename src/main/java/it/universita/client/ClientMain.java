package it.universita.client;

import it.universita.config.Config;
import it.universita.config.ConfigSAXParser;
import it.universita.model.Studente;
import it.universita.model.Utente;

import java.net.Socket;

public class ClientMain {
    public static void main(String[] args) throws  Exception{

        Config config = ConfigSAXParser.fromXmlFile("config.xml");
        ClientTCP ctcp = new ClientTCP(config.getClientHost(), config.getClientPort());
        Utente u = ctcp.registrazione("raul","petruzza","2000/09/22", "raulpet","1234");

    }
}
