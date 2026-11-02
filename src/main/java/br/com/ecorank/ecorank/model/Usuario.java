package br.com.ecorank.ecorank.model;

public class Usuario {

    private long id;
    private String nome;
    private String email;
    private int pontos;
    private String nivel;
    private String recompensa;
    private String perfil = "USUARIO";

    public Usuario() {
    }

    public Usuario(long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
        this.pontos = 0;
        this.nivel = "Bronze";
        this.recompensa = "";
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getPontos() {
        return pontos;
    }

    public void setPontos(int pontos) {
        this.pontos = pontos;
    }

    public String getNivel() {
        return nivel;
    }

    public void setNivel(String nivel) {
        this.nivel = nivel;
    }

    public String getRecompensa() {
        return recompensa;
    }

    public void setRecompensa(String recompensa) {
        this.recompensa = recompensa;
    }

    public void adicionarPontos(int pontos) {
        this.pontos = this.pontos + pontos;
    }

    public void removerPontos(int pontos) {
        this.pontos = this.pontos - pontos;
        if (this.pontos < 0) {
            this.pontos = 0;
        }
    }

    // USUARIO (padrão) ou ADMIN
    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(perfil);
    }
}
