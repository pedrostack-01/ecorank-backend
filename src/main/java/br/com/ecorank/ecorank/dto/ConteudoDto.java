package br.com.ecorank.ecorank.dto;

import java.time.LocalDate;

public record ConteudoDto(long id, String categoria, String titulo, String resumo,
                          String fonte, LocalDate data, String url) {}
