package br.com.ecorank.ecorank.dto;

// Resposta do envio de uma ação: tudo calculado pelo servidor
public record ResultadoAcaoDto(long idAcao, String status, int pontosGanhos,
                               int bonus, int pontosTotais, String nivel) {}
