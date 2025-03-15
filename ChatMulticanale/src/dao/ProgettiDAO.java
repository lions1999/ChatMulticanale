package dao;

import model.Dipendente;
import model.Progetto;
import model.Role;
import model.RuoloInProgetto;
import utility.ScannerUtility;
import utility.printList;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProgettiDAO extends SqlDAO{
    public static boolean vedereProgetti(Role role, String username, List<Progetto> progettiList) throws SQLException {
        openRoleConnection(role);

        String call = "{CALL getProgettiUtente(?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,username);

        ResultSet rs = cs.executeQuery();
        if (rs.first()){
            do {
                progettiList.add(new Progetto(rs.getString(1)));
            }while (rs.next());
        }

        return true;

    }

    public static boolean inserisciProgetto(String nomeProgetto, RuoloInProgetto ruoloInProgetto) throws SQLException {
        openRoleConnection(ruoloInProgetto.getRole());
        List<Dipendente> dipendenteList = new ArrayList<>();
        String usernameCapoProgetto = null;

        String query = "SELECT username from chatmulticanale.dipendente where username <> ?;";

        try {
            PreparedStatement ps = conn.prepareStatement(query,ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ps.setString(1,ruoloInProgetto.getUsername());
            ResultSet rs = ps.executeQuery();
            if (rs.first()){
                do {dipendenteList.add(new Dipendente(rs.getString(1)));}
                while (rs.next());
            }
            rs.close();
            boolean continua = true;
            while (continua) {
                try {
                    printList.printListsWithIndex(dipendenteList);
                    String input = ScannerUtility.askFirstChar("Digita il numero tra parentesi per scegliere il dipedente da nominare capo progetto per il progetto " + nomeProgetto + ".\n");
                    usernameCapoProgetto = dipendenteList.get(Integer.parseInt(input)).getUsername();
                    continua = false;
                }catch (Exception e){System.out.println("Errore scrivere solo uno dei numeri elencati sopra.\n");}
            }
        }catch (SQLException e){e.printStackTrace();}


        String call = "{CALL inserisciNuovoProgetto(?,?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,nomeProgetto);
        cs.setString(2,usernameCapoProgetto);

        cs.executeQuery();
        System.out.println("ho lanciato la query: "+cs+"\n\n");

        return true;
    }

    public static boolean inserisciDipendenteInProgetto(RuoloInProgetto ruoloInProgetto) throws SQLException{
        openRoleConnection(ruoloInProgetto.getRole());

        List<Progetto> progettoList = new ArrayList<>();
        List<Dipendente> dipendenteList = new ArrayList<>();
        String inputUsername = null;
        String inputNomeProgetto = null;


        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT nomeProgetto FROM chatmulticanale.progetto;");

        if (rs.first()){
            do {
                progettoList.add(new Progetto(rs.getString(1)));
            }while (rs.next());
        }rs.close();

        ResultSet rs1 = stmt.executeQuery("SELECT username FROM chatmulticanale.dipendente WHERE username <> '"+ruoloInProgetto.getUsername()+"';");
        if (rs1.first()){
            do {
                dipendenteList.add(new Dipendente(rs1.getString(1)));
            }while (rs1.next());
        }rs1.close();

        boolean continua = true;
        while (continua) {
            try {
                printList.printListsWithIndex(dipendenteList);
                String inputIndexForUsername = ScannerUtility.askFirstChar("Digita il numero tra parentesi per scegliere il dipendente da aggiungere al progetto.\n");
                inputUsername = dipendenteList.get(Integer.parseInt(inputIndexForUsername)).getUsername();
                printList.printListsWithIndex(progettoList);
                String inputIndexForNomeProgetto = ScannerUtility.askFirstChar("Digita il numero tra parentesi per scegliere il progetto in cui aggiungre il dipendente "+inputUsername+".\n");
                inputNomeProgetto = progettoList.get(Integer.parseInt(inputIndexForNomeProgetto)).getNome();
                continua = false;
            }catch (Exception e){System.out.println("Errore scrivere solo uno dei numeri elencati sopra.\n");}
        }

        String call = "{CALL InserisciUtenteInProgetto(?,?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);

        cs.setString(1,inputUsername);
        cs.setString(2,inputNomeProgetto);

        cs.executeQuery();
        return true;
    }
}
