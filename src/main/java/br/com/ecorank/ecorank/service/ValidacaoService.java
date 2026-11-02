package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.Status;
import org.springframework.stereotype.Service;

@Service
public class ValidacaoService {

    // Valida a evidência (link da foto ou vídeo) e define o status da ação
    public boolean validarAcao(AcaoSustentavel acao) {

        String link = acao.getLinkEvidencia();

        if (link == null || link.isBlank()) {
            acao.setStatus(Status.REJEITADA);
            return false;
        }

        if (link.contains("http")
                || link.endsWith(".jpg")
                || link.endsWith(".png")
                || link.endsWith(".mp4")) {

            acao.setStatus(Status.APROVADA);
            return true;
        }

        acao.setStatus(Status.REJEITADA);
        return false;
    }
}
