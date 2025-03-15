package model;

public class RuoloInProgetto {
    String username;
    String nomeProgetto;
    Role role;

    public RuoloInProgetto(String username, String nomeProgetto, Role role) {
        this.username = username;
        this.nomeProgetto = nomeProgetto;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNomeProgetto() {
        return nomeProgetto;
    }

    public void setNomeProgetto(String nomeProgetto) {
        this.nomeProgetto = nomeProgetto;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public String toFormattedString(){
        return username +"  "+role;
    }

    @Override
    public String toString() {
        return nomeProgetto+" "+role;
    }
}
