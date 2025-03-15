package model;

public class CorrenteRuoloInProgetto {
    private static String username;
    private static String nomeProgetto;
    private static Role role;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        CorrenteRuoloInProgetto.username = username;
    }

    public static String getNomeProgetto() {
        return nomeProgetto;
    }

    public static void setNomeProgetto(String nomeProgetto) {
        CorrenteRuoloInProgetto.nomeProgetto = nomeProgetto;
    }

    public static Role getRole() {return role;}

    public static void setRole(Role role) {
        CorrenteRuoloInProgetto.role = role;
    }

    public static RuoloInProgetto getRuoloInProgetto(){
        return new RuoloInProgetto(username,nomeProgetto,role);
    }
}
