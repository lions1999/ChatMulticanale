package model;

public class CanaleCorrente {
    private static String nomeCanale;
    private static boolean privato;
    private static String nomeProgetto;
    private static Boolean readOnly;


    public static boolean isPrivato() {
        return privato;
    }

    public static void setPrivato(boolean privato) {
        CanaleCorrente.privato = privato;
    }

    public static String getNomeCanale() {
        return nomeCanale;
    }

    public static void setNomeCanale(String nomeCanale) {
        CanaleCorrente.nomeCanale = nomeCanale;
    }

    public static String getNomeProgetto() {return nomeProgetto;}

    public static void setNomeProgetto(String nomeProgetto) {CanaleCorrente.nomeProgetto = nomeProgetto;}

    public static Boolean getReadOnly() {return readOnly;}

    public static void setReadOnly(Boolean readOnly) {CanaleCorrente.readOnly = readOnly;}

    public static Canale getCanale(){return new Canale(nomeCanale,nomeProgetto,isPrivato());}


}
