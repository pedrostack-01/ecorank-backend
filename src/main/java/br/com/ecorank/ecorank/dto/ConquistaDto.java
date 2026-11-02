package br.com.ecorank.ecorank.dto;

import java.time.LocalDate;

public record ConquistaDto(long id, long idUsuario, String nome,
                           String descricao, LocalDate dataConquista) {}
