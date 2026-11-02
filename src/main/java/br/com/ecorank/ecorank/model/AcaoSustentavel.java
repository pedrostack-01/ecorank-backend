package br.com.ecorank.ecorank.model;

import java.time.LocalDate;

public class AcaoSustentavel {

    private long id;
    private long idUsuario;
    private String categoria;
    private String descricao;
    private String linkEvidencia;
    private Status status;
    private int pontos;
    private LocalDate data;
    private double co2Kg;

    public AcaoSustentavel() {
    }

    public AcaoSustentavel(long idUsuario, String categoria, String descricao,
                           String linkEvidencia) {
        this.idUsuario = idUsuario;
        this.categoria = categoria;
        this.descricao = descricao;
        this.linkEvidencia = linkEvidencia;
        this.pontos = 0;
        this.status = Status.PENDENTE;
        this.data = LocalDate.now();
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

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLinkEvidencia() {
        return linkEvidencia;
    }

    public void setLinkEvidencia(String linkEvidencia) {
        this.linkEvidencia = linkEvidencia;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    public LocalDate getData() {
        return data;
    }

    public void setData(LocalDate data) {
        this.data = data;
    }

    public double getCo2Kg() {
        return co2Kg;
    }

    public void setCo2Kg(double co2Kg) {
        this.co2Kg = co2Kg;
    }
}
