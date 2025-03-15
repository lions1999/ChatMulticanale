package view;

import controller.DatabaseConnectionController;
import controller.ViewController;
import main.Main;
import model.*;
import utility.MessageFilterUtility;
import utility.ScannerUtility;
import utility.printList;

import java.util.ArrayList;
import java.util.List;

//CAPO PROGETTO GRANT sulle operazioni: LD, LM, IM, RM1, RM2, LC1, LC2, LP, ID3, IC1, IC2
public class ViewCapop {

    private static List<Messaggio> listaMessaggi = null;

    protected enum OPERATION{
        VISUALIZZA_APPARTENENTI_CANALE, //LD
        VISUALIZZA_MESSAGGI_NEL_CANALE, //LM
        INVIO_MESSAGGIO_NEL_CANALE, //IM
        RISPOSTA_MESSAGGIO_PUBBLICA, //RM1
        RISPOSTA_MESSAGGIO_PRIVATA, //RM2
        LISTA_CANALI_NEL_PROGETTO, //LC1
        LISTA_MIEI_PROGETTI, //LP
        AGGIUNGI_DIPENDENTI_NEL_CANALE, //ID3
        CREA_CANALE_PUBBLICO, //IC1
        CREA_CANALE_PRIVATO, //IC2
        LOGOUT,
        TERMINARE_APPLICAZIONE;


        public static OPERATION mainDispatchMap(String input) {
            return switch(input){
                case "0" -> LISTA_CANALI_NEL_PROGETTO;
                case "1" -> CREA_CANALE_PUBBLICO;
                case "2" -> CREA_CANALE_PRIVATO;

                case "l", "L" -> LOGOUT;
                case "u", "U" -> TERMINARE_APPLICAZIONE;
                default -> null;
            };
        }


        public static OPERATION readOnlyChannel(String input){
            return switch (input.toLowerCase()){
                case "0" -> VISUALIZZA_APPARTENENTI_CANALE;
                case "1" -> LISTA_CANALI_NEL_PROGETTO;

                case "l" -> LOGOUT;
                case "u" -> TERMINARE_APPLICAZIONE;
                default -> null;
            };
        }

        public static OPERATION publicChannelDispatchMap(String input){
            return switch (input.toLowerCase()){
                case "0" -> INVIO_MESSAGGIO_NEL_CANALE;
                case "1" -> RISPOSTA_MESSAGGIO_PUBBLICA;
                case "2" -> RISPOSTA_MESSAGGIO_PRIVATA;
                case "3" -> VISUALIZZA_APPARTENENTI_CANALE;
                case "4" -> LISTA_CANALI_NEL_PROGETTO;

                case "l" -> LOGOUT;
                case "u" -> TERMINARE_APPLICAZIONE;
                default -> null;
            };
        }

        public static OPERATION privateChannelDispatchMap(String input){
            return switch (input.toLowerCase()){
                case "0" -> INVIO_MESSAGGIO_NEL_CANALE;
                case "1" -> RISPOSTA_MESSAGGIO_PRIVATA;
                case "2" -> VISUALIZZA_APPARTENENTI_CANALE;
                case "3" -> LISTA_CANALI_NEL_PROGETTO;

                case "l" -> LOGOUT;
                case "u" -> TERMINARE_APPLICAZIONE;
                default -> null;
            };
        }
    }

    private static final String MAIN_DISPATCH = """
            
            Operazioni possibili:
            (0) LISTA DEI MIEI CANALI.
            (1) CREA CANALE PUBBLICO.
            (2) CREA CANALE PRIVATO.
            
            (L) LOGOUT.
            (U) USCIRE DALL'APPLICAZIONE.
            
            """;

    private static final String PUBBLIC_CHANNEL_DISPATCH = """
            
            Operazioni possibili:
            (0) INVIA MESSAGGIO NEL CANALE.
            (1) RISPONDI PUBBLICAMENTE A UN MESSAGGIO PRECEDENTE.
            (2) RISPONDI PRIVATAMENTE A UN MESSAGGIO PRECEDENTE.
            (3) MEMBRI DEL CANALE.
            (4) ESCI DAL CANALE.
            
            
            (L) LOGOUT.
            (U) USCIRE DALL'APPLICAZIONE.
            
            """;

    private static final String PRIVATE_CHANNEL_DISPATCH = """
            
            Operazioni possibili:
            (0) INVIA MESSAGGIO NEL CANALE.
            (1) RISPONDI PRIVATAMENTE A UN MESSAGGIO PRECEDENTE.
            (2) MEMBRI DEL CANALE.
            (3) ESCI DAL CANALE.
            
            
            (L) LOGOUT.
            (U) USCIRE DALL'APPLICAZIONE.
            
            """;

    private static final String READ_ONLY_CHANNEL_DISPATCH = """
            
            Operazioni possibili:
            (0) MEMBRI DEL CANALE.
            (1) ESCI DAL CANALE.
            
            
            (L) LOGOUT.
            (U) USCIRE DALL'APPLICAZIONE.
            
            
            """;

    public static void begin() {
        System.out.println("Sei nella view del capo progetto "+ CorrenteRuoloInProgetto.getUsername()+".");
    OPERATION operation;

        do{
            do {
                operation = OPERATION.mainDispatchMap(ScannerUtility.askFirstChar(MAIN_DISPATCH));
            } while (operation == null);

            mainDispatch(operation);

        }while (operation != OPERATION.LOGOUT);

        System.out.print("Log out dall'applicazione... ");
        CorrenteRuoloInProgetto.setRole(Role.LOGIN);
        CorrenteRuoloInProgetto.setUsername(null);
        DatabaseConnectionController.closeConnection();
        System.out.println("terminato con successo.\n-----------------------------------------\n");
        //ScannerUtility.askAny();
        Main.main(null);
    }

    protected static void mainDispatch(OPERATION operation){
        switch (operation){
            case LISTA_CANALI_NEL_PROGETTO -> listaCanaliNelProgetto();
            case CREA_CANALE_PUBBLICO -> creaCanale(false);
            case CREA_CANALE_PRIVATO -> creaCanale(true);

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

    private static void creaCanale(Boolean privato) {
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ScannerUtility.askString("Come vuoi chiamare il canale da creare nel progetto "+ruoloInProgetto.getNomeProgetto()+".",45);
        ViewController.creaCanalePubblico("rgerhh",privato,ruoloInProgetto);
    }

    private static void listaCanaliNelProgetto() {
        CanaleCorrente.setNomeCanale("");
        RuoloInProgetto ruoloInProgetto = new RuoloInProgetto(CorrenteRuoloInProgetto.getUsername(),CorrenteRuoloInProgetto.getNomeProgetto(),CorrenteRuoloInProgetto.getRole());
        List<Canale> listaCanali = new ArrayList<>();
        ViewController.ListaCanaliUtenteInProgetto(ruoloInProgetto,listaCanali);

        System.out.println("Questi sono i canali disponibili nel progetto: "+CorrenteRuoloInProgetto.getNomeProgetto()+" per il tuo utente:"+CorrenteRuoloInProgetto.getUsername()+"\n");
        printList.printListsWithIndex(listaCanali);

        boolean continua = true;
        String inputChar;
        while (continua) {
            try {
                inputChar = ScannerUtility.askFirstChar("Digita il numero corrispondente al canale per visualizzare i messaggi all'interno\n");
                CanaleCorrente.setNomeCanale(listaCanali.get(Integer.parseInt(inputChar)).getNome());
                CanaleCorrente.setPrivato(listaCanali.get(Integer.parseInt(inputChar)).getPrivate());
                CanaleCorrente.setReadOnly(listaCanali.get(Integer.parseInt(inputChar)).isReadOnly());
                continua = false;
            } catch (Exception e) {
                System.out.println("Errore scrivere solo uno dei numeri elecati qui");
            }
        }
        System.out.println("\n\n--------------------------  "+CanaleCorrente.getNomeCanale()+"  --------------------------\n\n");
        List<Messaggio> messaggiList = visualizzaMessaggiNelCanaleSelezionato();
        listaMessaggi = messaggiList;
        printList.printListsWithoutIndex(messaggiList);

        OPERATION operation;
        do{
            do {
                if(!CanaleCorrente.getReadOnly()){operation = OPERATION.readOnlyChannel(ScannerUtility.askFirstChar(READ_ONLY_CHANNEL_DISPATCH));}
                else if (CanaleCorrente.isPrivato()){operation = OPERATION.privateChannelDispatchMap(ScannerUtility.askFirstChar(PRIVATE_CHANNEL_DISPATCH));}
                else {operation = OPERATION.publicChannelDispatchMap(ScannerUtility.askFirstChar(PUBBLIC_CHANNEL_DISPATCH));}
            } while (operation == null);

            channelDispatch(operation);

        }while (operation != OPERATION.LOGOUT);

        System.out.print("Log out dall'applicazione... ");
        CorrenteRuoloInProgetto.setRole(Role.LOGIN);
        CorrenteRuoloInProgetto.setUsername(null);
        CorrenteRuoloInProgetto.setNomeProgetto(null);
        DatabaseConnectionController.closeConnection();
        System.out.println("terminato con successo.\n-----------------------------------------\n");
        //ScannerUtility.askAny();
        Main.main(null);

    }

    protected static void channelDispatch(OPERATION operation){
        switch (operation){
            case INVIO_MESSAGGIO_NEL_CANALE -> inviaMessaggio();
            case RISPOSTA_MESSAGGIO_PUBBLICA -> rispostaPublica();
            case RISPOSTA_MESSAGGIO_PRIVATA -> rispostaPrivata();
            case VISUALIZZA_APPARTENENTI_CANALE -> visualizzaAppartenentiCanale();
            case LISTA_CANALI_NEL_PROGETTO -> listaCanaliNelProgetto();

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

    private static void rispostaPrivata() {
        System.out.println("rispostaPrivata\n");
        listaMessaggi = visualizzaMessaggiNelCanaleSelezionato();
        printList.printListsWithIndex(listaMessaggi);
        MessageFilterUtility filterUtility = new MessageFilterUtility(listaMessaggi);
        String input = ScannerUtility.askFirstChar("A quale messaggio vuoi rispondere?\nDigita il carattere tra ( ) per scegliere o digita (F) per filtrare i messaggi per utente\n");
        if(input.equalsIgnoreCase("f")){
            boolean continua = true;
            while (continua){
                try {
                    String usernameToFilter = ScannerUtility.askString("Hai selezionato filtrare per nome utente.\nDigitare username utente per filtrare messaggi",45);
                    if (usernameToFilter.equalsIgnoreCase("u")){break;}
                    else {
                        System.out.println("filtro per username " + usernameToFilter + "\n");
                        continua = filterUtility.filterByUser(usernameToFilter);
                        if (filterUtility.getFilteredMessages().isEmpty()) {
                            throw new Exception("Nessun messaggio per l'utente " + usernameToFilter + " se hai sbaliato scrivi correttamente altrimenti digita (U) per uscire.\n");
                        } else {
                            printList.printListsWithIndex(filterUtility.getFilteredMessages());
                            input = ScannerUtility.askString("Digitare il numero tra ( ) corrispondente al messaggio a cui si vuole rispondere.\n", 3);
                        }
                    }
                }catch (Exception e){System.out.println(e.getMessage());}
            }
        }
        Messaggio messaggio = new Messaggio(
                null,
                ScannerUtility.askText("Digita il testo del messaggio da inviare:\n",255),
                true,
                filterUtility.getFilteredMessages().get(Integer.parseInt(input)).getId(),
                null,
                CorrenteRuoloInProgetto.getNomeProgetto(),
                CorrenteRuoloInProgetto.getUsername(),
                null,
                filterUtility.getFilteredMessages().get(Integer.parseInt(input))
        );
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.inviaMessaggioPrivato(messaggio,ruoloInProgetto);

        String choice = ScannerUtility.askFirstChar("Vuoi andare nel canale "+messaggio.getCanale()+" dove hai appena scritto? (Y) o (N)?");
        switch (choice.toLowerCase()){
            case "y":
                CanaleCorrente.setNomeCanale(messaggio.getCanale());
                CanaleCorrente.setPrivato(true);
                CanaleCorrente.setNomeProgetto(messaggio.getNomeProgetto());
                listaMessaggi = visualizzaMessaggiNelCanaleSelezionato();
                printList.printListsWithoutIndex(listaMessaggi);
                break;
            case "n":
                break;
        }
    }

    private static void rispostaPublica() {
        System.out.println("rispostaPublica\n");
        listaMessaggi = visualizzaMessaggiNelCanaleSelezionato();
        printList.printListsWithIndex(listaMessaggi);
        MessageFilterUtility filterUtility = new MessageFilterUtility(listaMessaggi);
        String input = ScannerUtility.askFirstChar("A quale messaggio vuoi rispondere?\nDigita il carattere tra ( ) per scegliere o digita (F) per filtrare i messaggi per utente\n");
        if(input.equalsIgnoreCase("f")){
            boolean continua = true;
            while (continua){
                try {
                    String usernameToFilter = ScannerUtility.askString("Hai selezionato filtrare per nome utente.\nDigitare username utente per filtrare messaggi",45);
                    if (usernameToFilter.equalsIgnoreCase("u")){break;}
                    else {
                        System.out.println("filtro per username " + usernameToFilter + "\n");
                        continua = filterUtility.filterByUser(usernameToFilter);
                        if (filterUtility.getFilteredMessages().isEmpty()) {
                            throw new Exception("Nessun messaggio per l'utente " + usernameToFilter + " se hai sbaliato scrivi correttamente altrimenti digita (U) per uscire.\n");
                        } else {
                            printList.printListsWithIndex(filterUtility.getFilteredMessages());
                            input = ScannerUtility.askString("Digitare il numero tra ( ) corrispondente al messaggio a cui si vuole rispondere.\n", 3);
                        }
                    }
                }catch (Exception e){System.out.println(e.getMessage());}
            }
        }
        Messaggio messaggio = new Messaggio(
                null,
                ScannerUtility.askText("Digita il testo del messaggio da inviare:\n",255),
                false,
                filterUtility.getFilteredMessages().get(Integer.parseInt(input)).getId(),
                CanaleCorrente.getNomeCanale(),
                CorrenteRuoloInProgetto.getNomeProgetto(),
                CorrenteRuoloInProgetto.getUsername(),
                null,
                filterUtility.getFilteredMessages().get(Integer.parseInt(input))
        );
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.inviaMessaggio(messaggio,ruoloInProgetto);
        printList.printListsWithoutIndex(visualizzaMessaggiNelCanaleSelezionato());
    }

    private static void inviaMessaggio() {
        System.out.println("invioMessaggio\n");
        String contenuto = ScannerUtility.askText("Digita il testo del messaggio da inviare:\n",255);
        Messaggio messaggio = new Messaggio(null,contenuto,CanaleCorrente.isPrivato(),null,CanaleCorrente.getNomeCanale(),CorrenteRuoloInProgetto.getNomeProgetto(),CorrenteRuoloInProgetto.getUsername(),null,null);
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.inviaMessaggio(messaggio,ruoloInProgetto);
        printList.printListsWithoutIndex(visualizzaMessaggiNelCanaleSelezionato());
    }

    private static void visualizzaAppartenentiCanale() {
        System.out.println("Visualizzo la lista dei dipendenti appartenenti al canale: "+CanaleCorrente.getNomeCanale()+"\n");
        List<RuoloInProgetto> ruoloInProgettoList = new ArrayList<>();
        ViewController.listaDipendentiNelCanale(CanaleCorrente.getCanale(),CorrenteRuoloInProgetto.getRuoloInProgetto(),ruoloInProgettoList);
        for (RuoloInProgetto ruoloInProgetto : ruoloInProgettoList ){System.out.println(ruoloInProgetto.toFormattedString());}
    }

    private static List<Messaggio> visualizzaMessaggiNelCanaleSelezionato() {
        List<Messaggio> messaggiList = new ArrayList<>();
        RuoloInProgetto ruoloInProgetto = CorrenteRuoloInProgetto.getRuoloInProgetto();
        ViewController.getListMessaggiByCanale(CanaleCorrente.getNomeCanale(),ruoloInProgetto,messaggiList);
        return messaggiList;
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
