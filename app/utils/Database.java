
package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL =
       "jdbc:sqlserver://localhost:1433;"
        + "databaseName=ProductDB;"
        + "encrypt=true;"
        + "trustServerCertificate=true;"
        + "integratedSecurity=true;";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}