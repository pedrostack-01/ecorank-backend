package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.Status;
import br.com.ecorank.ecorank.model.Usuario;
import org.springframework.stereotype.Service;

@Service
public class PontuacaoService {

    // Define quantos pontos a ação vale de acordo com a categoria
    public void calcularPontos(AcaoSustentavel acao) {

        if (acao.getCategoria().equalsIgnoreCase("Reciclagem")) {
            acao.setPontos(50);
        } else if (acao.getCategoria().equalsIgnoreCase("Economia de Agua")) {
            acao.setPontos(40);
        } else if (acao.getCategoria().equalsIgnoreCase("Plantio")) {
            acao.setPontos(70);
        } else if (acao.getCategoria().equalsIgnoreCase("Economia de Energia")) {
            acao.setPontos(60);
        } else {
            acao.setPontos(20);
        }
    }

    // Só ações aprovadas geram pontos para o usuário
    public void adicionarPontos(Usuario usuario, AcaoSustentavel acao) {

        if (acao.getStatus() == Status.APROVADA) {
            usuario.adicionarPontos(acao.getPontos());
        }
    }
}
