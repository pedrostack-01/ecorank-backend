package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Nivel;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.NivelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class NivelService {

    private final NivelRepository repositorio;

    public NivelService(NivelRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Nivel> listar() {
        return repositorio.findAll();
    }

    public Nivel buscarPorId(long id) {
        Nivel nivel = repositorio.findById(id);
        if (nivel == null) {
            throw new RecursoNaoEncontradoException("Nível não encontrado com o id " + id);
        }
        return nivel;
    }

    @Transactional
    public Nivel salvar(Nivel nivel) {
        validarDados(nivel);
        nivel.setNome(nivel.getNome().trim());
        repositorio.save(nivel);
        return nivel;
    }

    @Transactional
    public Nivel atualizar(long id, Nivel dados) {
        Nivel nivel = buscarPorId(id);
        validarDados(dados);
        nivel.setNome(dados.getNome().trim());
        nivel.setPontosNecessarios(dados.getPontosNecessarios());
        nivel.setMultiplicador(dados.getMultiplicador());
        repositorio.update(nivel);
        return nivel;
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    // Define o nível do usuário de acordo com os pontos dele, usando a tabela NIVEL
    // (maior nível cujos pontos necessários o usuário já alcançou)
    public void atualizarNivel(Usuario usuario) {

        int pontos = usuario.getPontos();
        String nome = null;

        for (Nivel nivel : repositorio.findAll()) {
            if (pontos >= nivel.getPontosNecessarios()) {
                nome = nivel.getNome();
            }
        }

        if (nome != null) {
            usuario.setNivel(nome);
            return;
        }

        // Tabela vazia: usa as regras padrão
        if (pontos >= 1000) {
            usuario.setNivel("Esmeralda");
        } else if (pontos >= 700) {
            usuario.setNivel("Diamante");
        } else if (pontos >= 400) {
            usuario.setNivel("Ouro");
        } else if (pontos >= 200) {
            usuario.setNivel("Prata");
        } else {
            usuario.setNivel("Bronze");
        }
    }

    private void validarDados(Nivel nivel) {
        if (nivel.getNome() == null || nivel.getNome().isBlank()) {
            throw new EcoRankException("O nome do nível é obrigatório");
        }
        if (nivel.getNome().trim().length() > 30) {
            throw new EcoRankException("O nome do nível deve ter no máximo 30 caracteres");
        }
        if (nivel.getPontosNecessarios() < 0) {
            throw new EcoRankException("Os pontos necessários não podem ser negativos");
        }
        if (nivel.getMultiplicador() <= 0 || nivel.getMultiplicador() >= 100) {
            throw new EcoRankException("O multiplicador deve ser maior que 0 e menor que 100");
        }
    }
}
