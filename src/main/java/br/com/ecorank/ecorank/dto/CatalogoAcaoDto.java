package br.com.ecorank.ecorank.dto;

public record CatalogoAcaoDto(long id, String codigo, String rotulo,
                              String categoria, int pontos, double co2Kg) {}
