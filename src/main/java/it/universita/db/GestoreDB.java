package it.universita.db;

import it.universita.config.Config;
import it.universita.config.ConfigSAXParser;

import java.sql.*;


public final class GestoreDB {
    private static String URL, USER, PASS;

    static {
            try{
            Config config = ConfigSAXParser.fromXmlFile("config.xml");
            URL = config.getDbUrl();
            USER = config.getDbUser();
            PASS = config.getDbPassword();
            if (URL == null || USER == null) throw new IllegalStateException("dbUrl/dbUser mancanti");
        } catch (Exception e) {
            throw new RuntimeException("Errore caricando la config DB", e);
        }
    }

    private GestoreDB() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS); // usa i valori del config
    }

}

