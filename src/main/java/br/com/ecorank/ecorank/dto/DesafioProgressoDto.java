package br.com.ecorank.ecorank.dto;

public record DesafioProgressoDto(long id, String titulo, String descricao,
                                  int recompensa, String categoria, int metaAcoes,
                                  boolean participando, boolean concluido,
                                  int acoesRealizadas, int progresso) {}
