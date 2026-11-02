package br.com.ecorank.ecorank.dto;

import java.time.LocalDate;

public record MissaoDto(long id, long idUsuario, String descricao, int objetivo,
                        int progresso, int recompensa, LocalDate prazo) {}
