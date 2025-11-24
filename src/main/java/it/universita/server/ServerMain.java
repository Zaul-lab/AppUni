package it.universita.server;

import it.universita.config.Config;
import it.universita.config.ConfigSAXParser;

import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;

public class ServerMain {
    public static void main(String[] args) {
        try{
            Config config = ConfigSAXParser.fromXmlFile("config.xml");
            ServerSocket ss = new ServerSocket(config.getServerPort());
            System.out.println("Server attivo: mi metto in ascolto");
            while (true){
                Socket socket = ss.accept();
                System.out.println("Client connesso");
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (Exception e) {
            System.out.println("Errore nel server");
            e.printStackTrace();
        }
    }
}
