package br.com.ecorank.ecorank.dto;

public record UsuarioDto(long id, String nome, String email, int pontos,
                         String nivel, String recompensa) {}
