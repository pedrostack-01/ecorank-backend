package br.com.ecorank.ecorank.model;

import java.time.LocalDate;

public class Missao {

    private long id;
    private long idUsuario;
    private String descricao;
    private int objetivo;
    private int progresso;
    private int recompensa;
    private LocalDate prazo;

    public Missao() {
    }

    public Missao(long idUsuario, String descricao, int objetivo,
                  int recompensa, LocalDate prazo) {
        this.idUsuario = idUsuario;
        this.descricao = descricao;
        this.objetivo = objetivo;
        this.progresso = 0;
        this.recompensa = recompensa;
        this.prazo = prazo;
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public int getObjetivo() {
        return objetivo;
    }

    public void setObjetivo(int objetivo) {
        this.objetivo = objetivo;
    }

    public int getProgresso() {
        return progresso;
    }

    public void setProgresso(int progresso) {
        this.progresso = progresso;
    }

    public int getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(int recompensa) {
        this.recompensa = recompensa;
    }

    public LocalDate getPrazo() {
        return prazo;
    }

    public void setPrazo(LocalDate prazo) {
        this.prazo = prazo;
    }
}
