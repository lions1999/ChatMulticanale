package dao;


import model.Messaggio;
import model.RuoloInProgetto;

import java.sql.*;
import java.util.List;

public class MessaggiDAO extends SqlDAO {

    public static boolean getListMessaggiByCanale(String nomeCanale, RuoloInProgetto ruoloInProgetto, List<Messaggio> messaggiList) throws SQLException{
        openRoleConnection(ruoloInProgetto.getRole());
        String call = "{CALL ListaMessaggiInCanale(?,?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,nomeCanale);
        cs.setString(2,ruoloInProgetto.getNomeProgetto());

        ResultSet rs = cs.executeQuery();
        System.out.println("ho lanciato la query: "+cs+"\n\n");

        if (rs.first()){
            do {
                Messaggio messaggio = new Messaggio(rs.getInt("id"),rs.getString("contenuto"),rs.getBoolean("privato"),rs.getInt("risposta"),rs.getString("canale"),rs.getString("nomeProgetto"),rs.getString("mittente"),rs.getTimestamp("data"),null);
                messaggiList.add(messaggio);
            }while (rs.next());
        }

        for (Messaggio messaggio : messaggiList){
            if (messaggio.getRisposta() != null && messaggio.getRisposta() != 0){
                String query = "SELECT * FROM messaggio WHERE id = ?";
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setInt(1, messaggio.getRisposta());
                    ResultSet rs1 = ps.executeQuery();
                    if (rs1.next()) {
                        Messaggio messaggioOriginale = new Messaggio(rs1.getInt("id"), rs1.getString("contenuto"), rs1.getBoolean("privato"), rs1.getInt("risposta"), rs1.getString("canale"), rs1.getString("nomeProgetto"), rs1.getString("mittente"), rs1.getTimestamp("data"), null);
                        messaggio.setMessaggioRisposta(messaggioOriginale);
                    }
                }
            }
        }

        return true;
    }

    public static boolean inviaMessaggio(Messaggio messaggio, RuoloInProgetto ruoloInProgetto) throws SQLException{
        openRoleConnection(ruoloInProgetto.getRole());
        String call = "{CALL InviaMessaggio(?,?,?,?,?,?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,messaggio.getContenuto());
        if (messaggio.getRisposta() == null) {cs.setNull(2, Types.INTEGER);} else {cs.setInt(2, messaggio.getRisposta());}
        cs.setBoolean(3,messaggio.getPrivate());
        cs.setString(4,ruoloInProgetto.getNomeProgetto());
        cs.setString(5,messaggio.getMittente());
        cs.setString(6,messaggio.getCanale());

        System.out.println("ho lanciato la query: "+cs+"\n\n");
        cs.executeQuery();

        return true;
    }

    public static boolean inviaMessaggioPrivato(Messaggio messaggio, RuoloInProgetto ruoloInProgetto) throws SQLException{
        openRoleConnection(ruoloInProgetto.getRole());
        String call = "{CALL InviaRispostaPrivata(?,?,?,?,?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,messaggio.getContenuto());
        cs.setString(2,messaggio.getMittente());
        cs.setInt(3,messaggio.getRisposta());
        cs.setBoolean(4,messaggio.getPrivate());
        cs.setString(5,ruoloInProgetto.getNomeProgetto());

        System.out.println("ho lanciato la query: "+cs+"\n\n");
        ResultSet rs = cs.executeQuery();

        int insertedId = -1;
        if (rs.next()){insertedId = rs.getInt(1);}

        rs.close();

        String query = "SELECT canale FROM messaggio WHERE id = ?";
        try (PreparedStatement ps = conn.prepareStatement(query)) {
            ps.setInt(1, insertedId);
            ResultSet rs1 = ps.executeQuery();
            if (rs1.next()) {messaggio.setCanale(rs1.getString("canale"));}
        }

        return true;
    }
}