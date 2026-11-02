package br.com.ecorank.ecorank.exception;

// Limite de tentativas ou de envios atingido (HTTP 429)
public class MuitasTentativasException extends EcoRankException {

    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
