package it.universita.server;

import it.universita.model.Utente;

import java.net.ServerSocket;
import java.sql.*;

public class ServerMain {
    public static void main(String[] args) {
        Utente u = new Utente();
        try{
            ServerConfig sc = ServerConfig.fromXmlFile("server-config.xml");
            ServerSocket ss = new ServerSocket(sc.getPorta());
            System.out.println("Server attivo: mi metto in ascolto");
            while (true){
                var socket = ss.accept();
                socket.setSoTimeout(60000);
                System.out.println("Client connesso");
                new Thread(new ClientHandler(socket)).start();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
