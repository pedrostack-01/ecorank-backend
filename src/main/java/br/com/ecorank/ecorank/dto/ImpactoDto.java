package br.com.ecorank.ecorank.dto;

import java.util.List;

public record ImpactoDto(double co2Kg, int totalAcoes, int pontos, int pontosIniciais,
                         List<DiaPontosDto> evolucao,
                         List<CategoriaResumoDto> categorias, String metodologia) {}
