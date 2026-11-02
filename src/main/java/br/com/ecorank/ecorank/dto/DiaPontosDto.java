package br.com.ecorank.ecorank.dto;

import java.time.LocalDate;

public record DiaPontosDto(LocalDate data, int pontos, int acumulado) {}
