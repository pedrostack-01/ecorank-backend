package br.com.ecorank.ecorank.dto;

import java.util.List;

public record ResumoEcoRankDto(long idUsuario, String nome, int pontos, int acoes,
                               String nivel, double multiplicador,
                               String proximoNivel, int pontosNivelAtual,
                               int pontosProximoNivel, int pontosRestantes,
                               int progressoNivel, int posicao, int totalUsuarios,
                               int pontosSemana, int acoesSemana, double co2Kg,
                               List<CategoriaResumoDto> categorias) {}
