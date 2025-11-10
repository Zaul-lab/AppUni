package it.universita.server;

import java.sql.*;

public class ServerMain {
    public static void main(String[] args) {
        Connection cn;
        Statement st;
        ResultSet rs;
        String sql;

        try{
            cn = DriverManager.getConnection("jdbc:mariadb://localhost:3306/appuni", "root", "");
            sql = "SELECT 1";
            st = cn.createStatement();
            rs = st.executeQuery(sql);
            rs.next();
            System.out.println("Inserimento effettuato");
            System.out.println("DB OK: " + rs.getInt(1));
            rs.close();
            st.close();
            cn.close();
        }catch (SQLException e){
            System.out.println("SQLException: ");
            System.err.println(e.getMessage());
        }

    }
}
