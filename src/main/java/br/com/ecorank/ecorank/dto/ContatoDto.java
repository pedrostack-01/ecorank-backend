package br.com.ecorank.ecorank.dto;

public record ContatoDto(long id, String nome, String email, String motivo,
                         String mensagem, String status) {}
