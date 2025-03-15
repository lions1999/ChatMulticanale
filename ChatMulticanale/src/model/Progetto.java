package model;

public class Progetto {

    String nome;

    public Progetto(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {this.nome = nome;}

    @Override
    public String toString() {
        return nome;
    }
}
