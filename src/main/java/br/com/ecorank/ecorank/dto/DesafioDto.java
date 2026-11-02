package br.com.ecorank.ecorank.dto;

public record DesafioDto(long id, String titulo, String descricao,
                         int recompensa, String categoria, int metaAcoes) {}
