package dao;

import model.CorrenteRuoloInProgetto;
import model.Credenziali;
import model.RuoloInProgetto;
import model.Role;
import utility.ScannerUtility;
import utility.printList;

import java.sql.*;
import java.util.ArrayList;

public class SqlDAO {

    private static final String DB_URL = "jdbc:mysql://localhost:3306/chatmulticanale";

    private static Role LAST_ROLE = null;

    private static final String DIPENDENTE_USER = "dipendente";
    private static final String DIPENDENTE_PASS = "dipendente";

    private static final String CAPOP_USER = "capoProgetto";
    private static final String CAPOP_PASS = "capoProgetto";

    private static final String AMMIN_USER = "amministratore";
    private static final String AMMIN_PASS = "amministratore";

    private static final String LOGIN_USER = "login";
    private static final String LOGIN_PASS = "login";

    static Connection conn;

    Statement stmt;
    PreparedStatement preset;

    public SqlDAO() {
        this.stmt = null;
        conn = null;
        this.preset = null;
    }


    //CONNESSIONE AL DB
    static void openRoleConnection(Role role) throws SQLException {
        if (conn == null || LAST_ROLE != role) {

            if (conn != null) conn.close();

            String targetUser = null;
            String targetPass = null;

            switch (role) {
                case DIPENDENTE -> {
                    targetUser = DIPENDENTE_USER;
                    targetPass = DIPENDENTE_PASS;
                }
                case CAPO_PROGETTO -> {
                    targetUser = CAPOP_USER;
                    targetPass = CAPOP_PASS;
                }
                case AMMINISTRATORE -> {
                    targetUser = AMMIN_USER;
                    targetPass = AMMIN_PASS;
                }
                case LOGIN -> {
                    targetUser = LOGIN_USER;
                    targetPass = LOGIN_PASS;
                }
            }

            conn = DriverManager.getConnection(DB_URL, targetUser, targetPass);
            LAST_ROLE = role;
        }
    }

    //CHIUSURA CONNESSIONE AL DB
    public static void disconnect() throws SQLException{
        if (conn != null)
            conn.close();
    }

    //LOGIN ALIAS LG
    public static Boolean selectCredenziali(Role role, Credenziali credenziali, RuoloInProgetto ruoloInProgetto) throws SQLException {
        Role selectedRole = null;
        openRoleConnection(role);

        String call = "{call `login`(?, ?)}";

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        cs.setString(1, credenziali.getUsername());
        cs.setString(2, credenziali.getPassword());
        cs.closeOnCompletion();

        ResultSet rs = cs.executeQuery();

        ArrayList<RuoloInProgetto> ruoloInProgettoList = new ArrayList<>();
        if (rs.first()){
            do {
                ruoloInProgettoList.add(new RuoloInProgetto(credenziali.getUsername(),rs.getString("nomeProgetto"),Role.valueOf(rs.getString("ruolo"))));

            }while (rs.next());
        }
        String index = "";
        while (selectedRole == null){
            boolean continua = true;
            while (continua){
                try{
                    if (!ruoloInProgettoList.get(0).getRole().equals(Role.AMMINISTRATORE)){
                        printList.printListsWithIndex(ruoloInProgettoList);
                        index = ScannerUtility.askString("a quale progetto vuoi accedere?",2);
                        selectedRole = ruoloInProgettoList.get(Integer.parseInt(index)).getRole();
                        continua = false;}
                    else {
                        selectedRole = Role.AMMINISTRATORE;
                        break;
                    }
                }
                catch (Exception e){
                    System.out.println("Errore scrivere solo uno dei numeri elecati qui");
                }
            }
            if (!selectedRole.equals(Role.AMMINISTRATORE)){
                ruoloInProgetto.setNomeProgetto(ruoloInProgettoList.get(Integer.parseInt(index)).getNomeProgetto());
                ruoloInProgetto.setRole(ruoloInProgettoList.get(Integer.parseInt(index)).getRole());
                CorrenteRuoloInProgetto.setNomeProgetto(ruoloInProgetto.getNomeProgetto());
            }else {
                ruoloInProgetto.setRole(Role.AMMINISTRATORE);
            }

            ruoloInProgetto.setUsername(credenziali.getUsername());



        }
        return true;
    }
}

