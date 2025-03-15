package view;

import controller.DatabaseConnectionController;
import controller.ViewController;
import main.Main;
import model.CorrenteRuoloInProgetto;
import model.Role;
import model.RuoloInProgetto;
import utility.ScannerUtility;

//ADMIN GRANT sulle operazioni: IP, ID1, ID2
public class ViewAdmin {

    protected enum OPERATION{
        CREA_PROGETTO, //IP
        INSERISCI_NUOVO_DIPENDENTE, //ID1
        INSERISCI_DIPENDENTE_IN_PROGETTO, //ID2
        LOGOUT,
        TERMINARE_APPLICAZIONE;

        public static OPERATION dispatchMap(String input) {
            return switch(input.toLowerCase()){
                case "0" -> CREA_PROGETTO;
                case "1" -> INSERISCI_DIPENDENTE_IN_PROGETTO;
                case "2" -> INSERISCI_NUOVO_DIPENDENTE;

                case "l" -> LOGOUT;
                case "u" -> TERMINARE_APPLICAZIONE;
                default -> null;
            };
        }
    }

    private static final String MAIN_DISPATCH = """
            
            Operazioni possibili:
            (0) CREA NUOVO PROGETTO.
            (1) INSERISCI DIPENDENTE NEL PROGETTO.
            (2) INSERISCI DIPENDENTE.
            """;




    public static void begin() {
        System.out.println("Sei nella view dell'amministratore.\n");

        OPERATION operation;
        do {
            do {
                operation = OPERATION.dispatchMap(ScannerUtility.askFirstChar(MAIN_DISPATCH));
            }while (operation == null);

            mainDispatch(operation);

        }while (operation != OPERATION.LOGOUT);
    }

    protected static void mainDispatch(OPERATION operation){
        switch (operation){
            case CREA_PROGETTO -> creaProgetto();
            case INSERISCI_DIPENDENTE_IN_PROGETTO -> inserisciDipendenteInProgetto();
            case INSERISCI_NUOVO_DIPENDENTE -> inserisciNuovoDipendente();
            case LOGOUT -> logout();
            case TERMINARE_APPLICAZIONE -> {
                System.out.println("Chiusura della connessione con il database.");
                String message = DatabaseConnectionController.closeConnection();
                if (message != null)
                    System.out.println(message);
                System.out.println("Uscita dall'applicazione.");
                System.exit(0);
            }
        }
    }

    private static void inserisciNuovoDipendente() {
        String newUsername = ScannerUtility.askString("Digita l'username del nuovo utente che vuoi inserire",45);
        String newPassword =  ScannerUtility.askString("Digita la password del nuovo utente che vuoi inserire",45);
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.inserisciNuovoDipendente(newUsername,newPassword,ruoloInProgetto);
    }

    private static void inserisciDipendenteInProgetto() {
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.inserisciDipendenteInProgetto(ruoloInProgetto);
    }

    private static void creaProgetto() {
        String nomeNuovoProgetto = ScannerUtility.askString("Come vuoi chiamare il nuovo progetto?",45);
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.inserisciProgetto(nomeNuovoProgetto,ruoloInProgetto);
    }

    private static void logout(){
        System.out.print("Log out dall'applicazione... ");
        CorrenteRuoloInProgetto.setRole(Role.LOGIN);
        CorrenteRuoloInProgetto.setUsername(null);
        CorrenteRuoloInProgetto.setNomeProgetto(null);
        System.out.println("terminato con successo.\n-----------------------------------------\n");
        Main.main(null);
    }
}
