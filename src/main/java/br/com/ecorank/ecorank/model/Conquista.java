package br.com.ecorank.ecorank.model;

import java.time.LocalDate;

public class Conquista {

    private long id;
    private long idUsuario;
    private String nome;
    private String descricao;
    private LocalDate dataConquista;

    public Conquista() {
    }

    public Conquista(long idUsuario, String nome, String descricao) {
        this.idUsuario = idUsuario;
        this.nome = nome;
        this.descricao = descricao;
        this.dataConquista = LocalDate.now();
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataConquista() {
        return dataConquista;
    }

    public void setDataConquista(LocalDate dataConquista) {
        this.dataConquista = dataConquista;
    }
}
