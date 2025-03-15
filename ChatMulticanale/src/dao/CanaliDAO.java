package dao;

import model.Canale;
import model.Role;
import model.RuoloInProgetto;
import utility.ScannerUtility;

import java.sql.CallableStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CanaliDAO extends SqlDAO{
    public static boolean ListaCanaliUtenteInProgetto(RuoloInProgetto ruoloInProgetto, List<Canale> canaliList) throws SQLException {
        openRoleConnection(ruoloInProgetto.getRole());

        String call = "{CALL ListaCanaliPerUtenteProgetto(?,?,?)}";
        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,ruoloInProgetto.getNomeProgetto());
        cs.setString(2,ruoloInProgetto.getUsername());
        cs.setString(3,ruoloInProgetto.getRole().toString());

        ResultSet rs = cs.executeQuery();
        System.out.println("ho lanciato la query:"+cs+"\n\n");
        if (rs.first()){
            do {
                Canale canale = new Canale(rs.getString(1),rs.getString(2),rs.getBoolean(3));
                if (ruoloInProgetto.getRole().equals(Role.CAPO_PROGETTO)){
                    canale.setReadOnly(rs.getBoolean("AppartieneUtente"));}
                else {canale.setReadOnly(true);}
                canaliList.add(canale);
            }while (rs.next());
        }

        return true;
    }

    public static boolean ListaUtentiNelCanale(Canale canale, RuoloInProgetto ruoloInProgetto,List<RuoloInProgetto> ruoloInProgettoList) throws SQLException {
        openRoleConnection(ruoloInProgetto.getRole());

        String call = "{CALL ListaDipendentiInCanale(?,?)}";
        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,canale.getNome());
        cs.setString(2,ruoloInProgetto.getNomeProgetto());

        ResultSet rs = cs.executeQuery();
        System.out.println("ho lanciato la query:"+cs+"\n\n");
        if (rs.first()){
            do {
                ruoloInProgettoList.add(new RuoloInProgetto(rs.getString("username"),ruoloInProgetto.getNomeProgetto(), Role.valueOf(rs.getString("ruolo"))));
            }while (rs.next());
        }
        return true;
    }

    public static boolean CreaCanalePrivato(RuoloInProgetto ruoloInProgetto) throws SQLException{
        openRoleConnection(ruoloInProgetto.getRole());
        return true;

    }

    public static boolean CreaCanale(String nomeCanale, Boolean privato, RuoloInProgetto ruoloInProgetto) throws SQLException{
        openRoleConnection(ruoloInProgetto.getRole());

        List<RuoloInProgetto> ruoloInProgettoList = new ArrayList<>();
        List<RuoloInProgetto> ruoloInProgettoListInput = new ArrayList<>();

        Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet rs = stmt.executeQuery("SELECT username,ruolo FROM ruoloinprogetto WHERE nomeProgetto ='"+ruoloInProgetto.getNomeProgetto()+"'");

        if (rs.first()){
            do {
                ruoloInProgettoList.add(new RuoloInProgetto(rs.getString(1),ruoloInProgetto.getNomeProgetto(),Role.valueOf(rs.getString(2))));
            }while (rs.next());
        }
        String input ;
        String choice ;

        boolean continua = true;
        while (continua){
            try {
                int i = 0;
                for (RuoloInProgetto ruoloInProgetto1 : ruoloInProgettoList ){
                    System.out.println("("+i+") "+ruoloInProgetto1.toFormattedString());
                    i++;
                }
                if (ruoloInProgettoListInput.size()==2 && privato){
                    System.out.println("Il canale che vuoi creare è privato può quindi avere solo 2 utenti all'interno\n");
                    continua = false;
                    continue;
                }
                input = ScannerUtility.askFirstChar("\nDigitare il numero corrispondente all'utente da inserire nel canale");
                if (ruoloInProgettoListInput.size()==0){
                    ruoloInProgettoListInput.add(ruoloInProgettoList.get(Integer.parseInt(input)));
                    ruoloInProgettoList.remove(Integer.parseInt(input));
                }
                else if (Integer.parseInt(input)>ruoloInProgettoList.size()){
                    System.out.println("Numero troppo grande inserisci solo uno tra quelli indicati\n");
                }else {
                    if (!privato){
                        ruoloInProgettoListInput.add(ruoloInProgettoList.get(Integer.parseInt(input)));
                        ruoloInProgettoList.remove(Integer.parseInt(input));
                    }
                    else {
                        choice = ScannerUtility.askFirstChar("\nVuoi inserire altri membri? (Y) o (N)");
                        switch (choice.toLowerCase()) {
                            case "y" -> {
                                ruoloInProgettoListInput.add(ruoloInProgettoList.get(Integer.parseInt(input)));
                                ruoloInProgettoList.remove(Integer.parseInt(input));
                            }
                            case "n" -> {
                                ruoloInProgettoListInput.add(ruoloInProgettoList.get(Integer.parseInt(input)));
                                continua = false;
                            }
                        }
                    }
                }
            }catch (Exception e){System.out.println("Errore ritenta\n");}
        }

        String combinedUsernameToInput = ruoloInProgettoListInput.stream().map(RuoloInProgetto::getUsername).collect(Collectors.joining(","));

        String call;
        if (privato){call = "{CALL inserisciCanalePrivato(?,?,?)}";}
        else {call = "{CALL inserisciCanalePubblico(?,?,?)}";}

        System.out.println(call);

        CallableStatement cs = conn.prepareCall(call, ResultSet.TYPE_SCROLL_INSENSITIVE,ResultSet.CONCUR_READ_ONLY);
        cs.setString(1,nomeCanale);
        cs.setString(2,ruoloInProgetto.getNomeProgetto());
        cs.setString(3,combinedUsernameToInput);

        cs.executeQuery();
        return true;
    }
}
