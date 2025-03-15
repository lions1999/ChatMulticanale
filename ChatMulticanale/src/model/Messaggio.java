package model;

import java.sql.Timestamp;

public class Messaggio {

    Integer id;
    String contenuto;
    Boolean isPrivate;
    Integer risposta;
    String canale;
    String mittente;
    String nomeProgetto;
    Messaggio messaggioRisposta;
    Timestamp dataora;

    public Messaggio(Integer id, String contenuto, Boolean isPrivate, Integer risposta, String canale,String nomeProgetto, String mittente, Timestamp dataora, Messaggio messaggioRisposta) {
        this.id = id;
        this.contenuto = contenuto;
        this.isPrivate = isPrivate;
        this.risposta = risposta;
        this.canale = canale;
        this.nomeProgetto = nomeProgetto;
        this.mittente = mittente;
        this.dataora = dataora;
        this.messaggioRisposta = messaggioRisposta;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContenuto() {
        return contenuto;
    }

    public void setContenuto(String contenuto) {
        this.contenuto = contenuto;
    }

    public Boolean getPrivate() {
        return isPrivate;
    }

    public void setPrivate(Boolean aPrivate) {
        isPrivate = aPrivate;
    }

    public Integer getRisposta() {
        return risposta;
    }

    public void setRisposta(Integer risposta) {
        this.risposta = risposta;
    }

    public String getCanale() {
        return canale;
    }

    public void setCanale(String canale) {
        this.canale = canale;
    }

    public String getNomeProgetto() {return nomeProgetto;}

    public void setNomeProgetto(String nomeProgetto) {this.nomeProgetto = nomeProgetto;}

    public String getMittente() {
        return mittente;
    }

    public void setMittente(String mittente) {
        this.mittente = mittente;
    }

    public Timestamp getDataora() {
        return dataora;
    }

    public void setDataora(Timestamp dataora) {
        this.dataora = dataora;
    }

    public Messaggio getMessaggioRisposta() {return messaggioRisposta;}

    public void setMessaggioRisposta(Messaggio messaggioRisposta) {this.messaggioRisposta = messaggioRisposta;}

    @Override
    public String toString() {
        if (messaggioRisposta == null){return dataora+"\t:"+mittente+"\t: "+contenuto;}
        else if(!messaggioRisposta.getCanale().equals(canale)){return dataora+"\t:"+mittente+"\t: "+contenuto+"\n\tin risposta a: ("+messaggioRisposta.getId()+") "+messaggioRisposta.toString()+"\tdal Canale: "+messaggioRisposta.getCanale(); }
        else {return "\tIn risposta a: ("+messaggioRisposta.getId()+") "+messaggioRisposta.toString()+"\n"+dataora+"\t:"+mittente+"\t: "+contenuto;}
    }

    //quando viene stampato probabilmente non si capisce ma il messaggio sopra è quello più vecchio a cui si risponde mentre quelle sotto è quello nuovo
}
