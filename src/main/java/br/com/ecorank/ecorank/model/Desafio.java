package br.com.ecorank.ecorank.model;

public class Desafio {

    private long id;
    private String titulo;
    private String descricao;
    private int recompensa;
    private String categoria;
    private int metaAcoes;

    public Desafio() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(int recompensa) {
        this.recompensa = recompensa;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getMetaAcoes() {
        return metaAcoes;
    }

    public void setMetaAcoes(int metaAcoes) {
        this.metaAcoes = metaAcoes;
    }
}
