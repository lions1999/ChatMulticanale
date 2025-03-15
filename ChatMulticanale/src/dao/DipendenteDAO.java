package dao;

import model.RuoloInProgetto;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DipendenteDAO extends SqlDAO{
    public static boolean inserisciNuovoDipendente(String newUsername, String newPassword, RuoloInProgetto ruoloInProgetto) throws SQLException {
        openRoleConnection(ruoloInProgetto.getRole());

        String call = "{CALL InserisciNuovoUtente(?,?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

        cs.setString(1,newUsername);
        cs.setString(2,newPassword);

        cs.executeQuery();
        return true;
    }
}
