package br.com.ecorank.ecorank.exception;

// Usuário logado, mas sem permissão para esta operação (HTTP 403)
public class AcessoNegadoException extends EcoRankException {

    public AcessoNegadoException(String mensagem) {
        super(mensagem);
    }
}
