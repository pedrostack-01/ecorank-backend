package br.com.ecorank.ecorank.exception;

// Usuário sem login válido (HTTP 401)
public class NaoAutorizadoException extends EcoRankException {

    public NaoAutorizadoException(String mensagem) {
        super(mensagem);
    }
}
