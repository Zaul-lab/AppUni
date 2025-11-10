package it.universita.db;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

public final class GestoreDB {
    private static String URL, USER, PASS;

    static {
        try (InputStream in = GestoreDB.class.getClassLoader()
                .getResourceAsStream("server-config.xml")) {
            if (in == null) throw new IllegalStateException("server-config.xml non trovato");
            Properties p = new Properties();
            p.loadFromXML(in);
            URL = p.getProperty("dbUrl");
            USER = p.getProperty("dbUser");
            PASS = p.getProperty("dbPassword", "");
            if (URL == null || USER == null) throw new IllegalStateException("dbUrl/dbUser mancanti");
        } catch (Exception e) {
            throw new RuntimeException("Errore caricando la config DB", e);
        }
    }

    public GestoreDB() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS); // usa i valori del config
    }


    public void test() {
        String sql = "SELECT 1";
        try (Connection cn = getConnection();
             Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            System.out.println("DB OK: " + rs.getInt(1));
        } catch (SQLException e) {
            System.out.println("SQLException");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GestoreDB gdb = new GestoreDB();
        gdb.test();
    }
}
