package org.andrei.sample_project.connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionFactory {

    private static final String URL = "jdbc:postgresql://localhost:5432/library_app";
    private static final String USER = "postgres";
    private static final String PASSWORD = "barboi2005";

    private static Connection connection;
    public static Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {

                Class.forName("org.postgresql.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);

                System.out.println(">>> Database connected successfully");
                //debug
                try (Statement stmt = connection.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT current_database(), current_user, current_schema(), inet_server_addr()"
                     )) {

                    if (rs.next()) {
                        System.out.println(">>> DEBUG DB NAME    : " + rs.getString(1));
                        System.out.println(">>> DEBUG DB USER    : " + rs.getString(2));
                        System.out.println(">>> DEBUG DB SCHEMA  : " + rs.getString(3));
                        System.out.println(">>> DEBUG DB HOST    : " + rs.getString(4));
                    }
                }
            }
            return connection;

        } catch (ClassNotFoundException e) {
            System.out.println(">>> PostgreSQL JDBC Driver not found!");
            e.printStackTrace();

        } catch (SQLException e) {
            System.out.println(">>> Database connection failed!");
            System.out.println(">>> URL: " + URL);
            System.out.println(">>> Error: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }
    public static void close(Connection conn) {
        if (conn != null) {
            try {
                if (conn != connection) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println(">>> Error closing connection: " + e.getMessage());
            }
        }
    }

    public static void close(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException e) {
                System.out.println(">>> Error closing statement: " + e.getMessage());
            }
        }
    }

    public static void close(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println(">>> Error closing result set: " + e.getMessage());
            }
        }
    }


}
