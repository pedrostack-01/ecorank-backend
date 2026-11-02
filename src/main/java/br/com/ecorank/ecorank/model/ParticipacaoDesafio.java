package br.com.ecorank.ecorank.model;

import java.time.LocalDate;

public class ParticipacaoDesafio {

    private long id;
    private long idUsuario;
    private long idDesafio;
    private LocalDate dataInicio;
    private boolean concluido;

    public ParticipacaoDesafio() {
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

    public long getIdDesafio() {
        return idDesafio;
    }

    public void setIdDesafio(long idDesafio) {
        this.idDesafio = idDesafio;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public void setDataInicio(LocalDate dataInicio) {
        this.dataInicio = dataInicio;
    }

    public boolean getConcluido() {
        return concluido;
    }

    public void setConcluido(boolean concluido) {
        this.concluido = concluido;
    }
}
