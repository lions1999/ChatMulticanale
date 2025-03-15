package controller;

import dao.*;
import model.*;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class ViewController {
    protected static String getGenericSQLExceptionMessage(SQLException e) {
        return getExceptionMessage(null, e);
    }

    protected static String getExceptionMessage(String customMessage, SQLException e) {
        String message;
        if (customMessage != null) {
            message = customMessage;
        }
        else if (false && Objects.equals(e.getSQLState(), "42000")) {
            message = "L'utente non dispone dei permessi necessari";
        }
        else {
            message = e.getMessage();
        }

        return String.format("%s, [%s]", message, e.getSQLState());
    }

    public static DBResult login(Credenziali credenziali, RuoloInProgetto ruoloInProgetto) {
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(SqlDAO.selectCredenziali(CorrenteRuoloInProgetto.getRole(), credenziali,ruoloInProgetto));
        } catch (SQLException e) {
            dbResult.setMessage(switch (e.getSQLState()) {
                case "S1000" -> getExceptionMessage("Credenziali di accesso non valide", e);
                default -> getGenericSQLExceptionMessage(e);
            });
        }
        return dbResult;
    }


    public static DBResult vedereProgetti(List<Progetto> progettoList) {
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(ProgettiDAO.vedereProgetti(CorrenteRuoloInProgetto.getRole(), CorrenteRuoloInProgetto.getUsername(),progettoList));
        }catch (SQLException e){
            e.printStackTrace();
        }

        return dbResult;
    }

    public static void ListaCanaliUtenteInProgetto(RuoloInProgetto ruoloInProgetto, List<Canale> canaliList){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(CanaliDAO.ListaCanaliUtenteInProgetto(ruoloInProgetto,canaliList));
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void getListMessaggiByCanale(String nomeCanale,RuoloInProgetto ruoloInProgetto,List<Messaggio> messaggiList){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(MessaggiDAO.getListMessaggiByCanale(nomeCanale,ruoloInProgetto,messaggiList));
        }catch (SQLException e){
            e.printStackTrace();
        }

    }

    public static void inviaMessaggio(Messaggio messaggio, RuoloInProgetto ruoloInProgetto){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(MessaggiDAO.inviaMessaggio(messaggio,ruoloInProgetto));
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void inviaMessaggioPrivato(Messaggio messaggio, RuoloInProgetto ruoloInProgetto){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(MessaggiDAO.inviaMessaggioPrivato(messaggio,ruoloInProgetto));
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void listaDipendentiNelCanale(Canale canale, RuoloInProgetto ruoloInProgetto, List<RuoloInProgetto> ruoloInProgettoList){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(CanaliDAO.ListaUtentiNelCanale(canale,ruoloInProgetto,ruoloInProgettoList));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public static void inserisciProgetto(String nomeProgetto, RuoloInProgetto ruoloInProgetto){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(ProgettiDAO.inserisciProgetto(nomeProgetto,ruoloInProgetto));
        }catch (SQLException e){
            e.printStackTrace();
        }
    }

    public static void inserisciNuovoDipendente(String newUsername, String newPassword, RuoloInProgetto ruoloInProgetto){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(DipendenteDAO.inserisciNuovoDipendente(newUsername,newPassword,ruoloInProgetto));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void inserisciDipendenteInProgetto(RuoloInProgetto ruoloInProgetto){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(ProgettiDAO.inserisciDipendenteInProgetto(ruoloInProgetto));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void creaCanalePubblico(String nomeCanale,Boolean privato,RuoloInProgetto ruoloInProgetto){
        DBResult dbResult = new DBResult(false);
        try {
            dbResult.setResult(CanaliDAO.CreaCanale(nomeCanale,privato,ruoloInProgetto));
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
