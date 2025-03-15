package controller;

import dao.SqlDAO;

import java.sql.SQLException;

public class DatabaseConnectionController {
    public static String closeConnection() {
        try {
            SqlDAO.disconnect();
        } catch (SQLException e) {
            return String.format("Impossibile chiudere la connessione [%s, %s].", e.getSQLState(), e.getMessage());
        }
        return null;
    }
}
