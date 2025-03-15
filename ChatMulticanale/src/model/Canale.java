package model;

public class Canale {

    String nome;
    String nomeProgetto;
    Boolean isPrivate;
    Boolean readOnly;

    public Canale(String nome, String nomeProgetto, Boolean isPrivate) {
        this.nome = nome;
        this.nomeProgetto = nomeProgetto;
        this.isPrivate = isPrivate;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getNomeProgetto() {
        return nomeProgetto;
    }

    public void setNomeProgetto(String nomeProgetto) {
        this.nomeProgetto = nomeProgetto;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Boolean isReadOnly() {return readOnly;}

    public void setReadOnly(boolean aReadOnly) {
        readOnly = aReadOnly;
    }

    @Override
    public String toString() {
        if (readOnly){ return "Canale: "+ nome + " del Progetto: " + nomeProgetto;}
       else { return "Canale: "+ nome + " del Progetto: " + nomeProgetto+" READ ONLY";}
    }
}
