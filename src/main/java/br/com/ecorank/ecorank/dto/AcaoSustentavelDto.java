package br.com.ecorank.ecorank.dto;

import java.time.LocalDate;

public record AcaoSustentavelDto(long id, long idUsuario, String categoria,
                                 String descricao, String linkEvidencia,
                                 String status, int pontos, LocalDate data) {}
