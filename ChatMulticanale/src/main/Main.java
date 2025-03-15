package main;

import controller.DatabaseConnectionController;
import controller.ViewController;
import dao.DBResult;
import model.Credenziali;
import model.CorrenteRuoloInProgetto;
import model.Role;
import model.RuoloInProgetto;
import utility.ScannerUtility;
import view.ViewAdmin;
import view.ViewCapop;
import view.ViewDip;

public class Main{

    private enum MODE{
        LOGIN, //LG
        EXIT
    }

    private static final String startString = """
            (L) LOGIN.
            (U) USCIRE.
            """;

    public static void main(String[] args) {
        try{
            CorrenteRuoloInProgetto.setRole(Role.LOGIN);
            MODE selectedMode;

            while (true){
                selectedMode = null;
                do {
                    switch (ScannerUtility.askFirstChar(startString)) {
                        case "l", "L" -> selectedMode = MODE.LOGIN;
                        case "u", "U" -> selectedMode = MODE.EXIT;
                    }
                }while (selectedMode == null);

                switch (selectedMode) {
                    case LOGIN -> login();
                    case EXIT -> {
                        System.out.println("Chiusura della connessione con il database.");
                        String message = DatabaseConnectionController.closeConnection();
                        if (message != null)
                            System.out.println(message);
                        System.out.println("Uscita dall'applicazione.");
                        System.exit(0);
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static void login() {
        String username, password;

        username = ScannerUtility.askString("Username", 30);
        password = ScannerUtility.askString("Password", 30);
        Credenziali credenziali = new Credenziali(username, password);

        System.out.printf("Login di '%s'... \n\n", credenziali.getUsername());

        RuoloInProgetto ruoloInProgetto = new RuoloInProgetto(null,null,null);
        DBResult loginResult = ViewController.login(credenziali,ruoloInProgetto);

        if (loginResult.getResult()) {
            CorrenteRuoloInProgetto.setRole(ruoloInProgetto.getRole());
            CorrenteRuoloInProgetto.setUsername(credenziali.getUsername());
        }

        if (loginResult.getResult())
            System.out.print("eseguito con successo.\n");
        else {
            System.out.printf("eseguito con insuccesso (%s).\n", loginResult.getMessage());
        }

        dispatch(loginResult.getResult());
    }

    private static void dispatch(boolean result) {
        if (result) {
            switch (CorrenteRuoloInProgetto.getRole()) {
                case DIPENDENTE -> ViewDip.begin();
                case CAPO_PROGETTO -> ViewCapop.begin();
                case AMMINISTRATORE -> ViewAdmin.begin();
            }
        } else {
            main(null);
        }
    }
}
