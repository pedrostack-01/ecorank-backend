package br.com.ecorank.ecorank.model;

public class Nivel {

    private long id;
    private String nome;
    private int pontosNecessarios;
    private double multiplicador;

    public Nivel() {
    }

    public Nivel(long id, String nome, int pontosNecessarios, double multiplicador) {
        this.id = id;
        this.nome = nome;
        this.pontosNecessarios = pontosNecessarios;
        this.multiplicador = multiplicador;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getPontosNecessarios() {
        return pontosNecessarios;
    }

    public void setPontosNecessarios(int pontosNecessarios) {
        this.pontosNecessarios = pontosNecessarios;
    }

    public double getMultiplicador() {
        return multiplicador;
    }

    public void setMultiplicador(double multiplicador) {
        this.multiplicador = multiplicador;
    }
}
