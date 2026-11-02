package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RankingService {

    private final UsuarioRepository repositorio;

    public RankingService(UsuarioRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Usuario> montarRanking() {
        List<Usuario> usuarios = repositorio.findAll();
        ordenarRanking(usuarios);
        return usuarios;
    }

    // Coloca quem tem mais pontos nas primeiras posições.
    // Em caso de empate, fica na frente quem se cadastrou primeiro (menor id).
    public void ordenarRanking(List<Usuario> usuarios) {

        Usuario auxiliar;

        for (int i = 0; i < usuarios.size(); i++) {

            for (int j = i + 1; j < usuarios.size(); j++) {

                Usuario a = usuarios.get(i);
                Usuario b = usuarios.get(j);

                boolean maisPontos = b.getPontos() > a.getPontos();
                boolean empateEMaisAntigo = b.getPontos() == a.getPontos() && b.getId() < a.getId();

                if (maisPontos || empateEMaisAntigo) {

                    auxiliar = usuarios.get(i);
                    usuarios.set(i, usuarios.get(j));
                    usuarios.set(j, auxiliar);
                }
            }
        }
    }
}
