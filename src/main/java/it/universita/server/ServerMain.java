package it.universita.server;

import java.net.ServerSocket;
import java.sql.*;

public class ServerMain {
    public static void main(String[] args) {

        try{
            ServerConfig sc = ServerConfig.fromXmlFile("server-config.xml");
            ServerSocket ss = new ServerSocket(sc.getPorta());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
