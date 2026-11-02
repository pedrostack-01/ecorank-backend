package br.com.ecorank.ecorank.model;

public class Comprovacao {

    private long id;
    private long idAcao;
    private String nomeOriginal;
    private String tipo;
    private long tamanho;
    private String arquivo;
    private int duracaoSeg;

    public Comprovacao() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdAcao() {
        return idAcao;
    }

    public void setIdAcao(long idAcao) {
        this.idAcao = idAcao;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public long getTamanho() {
        return tamanho;
    }

    public void setTamanho(long tamanho) {
        this.tamanho = tamanho;
    }

    public String getArquivo() {
        return arquivo;
    }

    public void setArquivo(String arquivo) {
        this.arquivo = arquivo;
    }

    public int getDuracaoSeg() {
        return duracaoSeg;
    }

    public void setDuracaoSeg(int duracaoSeg) {
        this.duracaoSeg = duracaoSeg;
    }
}
