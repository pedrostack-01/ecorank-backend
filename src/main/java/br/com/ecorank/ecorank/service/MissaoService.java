package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Missao;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.MissaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class MissaoService {

    private final MissaoRepository repositorio;
    private final UsuarioService usuarioService;

    public MissaoService(MissaoRepository repositorio, UsuarioService usuarioService) {
        this.repositorio = repositorio;
        this.usuarioService = usuarioService;
    }

    public List<Missao> listar() {
        return repositorio.findAll();
    }

    public List<Missao> listarPorUsuario(long idUsuario) {
        usuarioService.buscarPorId(idUsuario);
        return repositorio.findByUsuario(idUsuario);
    }

    public Missao buscarPorId(long id) {
        Missao missao = repositorio.findById(id);
        if (missao == null) {
            throw new RecursoNaoEncontradoException("Missão não encontrada com o id " + id);
        }
        return missao;
    }

    @Transactional
    public Missao salvar(Missao missao) {
        if (missao.getIdUsuario() <= 0) {
            throw new EcoRankException("O idUsuario é obrigatório");
        }
        validarDados(missao);
        if (missao.getPrazo().isBefore(LocalDate.now())) {
            throw new EcoRankException("O prazo da missão não pode ser uma data passada");
        }
        usuarioService.buscarPorId(missao.getIdUsuario());

        missao.setDescricao(missao.getDescricao().trim());
        missao.setProgresso(0);
        repositorio.save(missao);
        return missao;
    }

    // O progresso não é alterado aqui, ele só aumenta quando o usuário
    // registra uma ação aprovada
    @Transactional
    public Missao atualizar(long id, Missao dados) {
        Missao missao = buscarPorId(id);
        validarDados(dados);

        missao.setDescricao(dados.getDescricao().trim());
        missao.setObjetivo(dados.getObjetivo());
        missao.setRecompensa(dados.getRecompensa());
        missao.setPrazo(dados.getPrazo());
        repositorio.update(missao);
        return missao;
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    // Soma +1 de progresso em todas as missões ativas do usuário
    public void registrarProgresso(Usuario usuario) {
        List<Missao> missoes = repositorio.findByUsuario(usuario.getId());

        for (Missao missao : missoes) {
            if (estaAtiva(missao)) {
                atualizarMissao(usuario, missao);
                repositorio.update(missao);
            }
        }
    }

    // Missão ativa: ainda não foi concluída e o prazo não terminou
    public boolean estaAtiva(Missao missao) {
        return missao.getProgresso() < missao.getObjetivo()
                && !missao.getPrazo().isBefore(LocalDate.now());
    }

    // Retorna true se a missão foi concluída neste progresso
    public boolean atualizarMissao(Usuario usuario, Missao missao) {

        missao.setProgresso(missao.getProgresso() + 1);

        if (missao.getProgresso() >= missao.getObjetivo()) {

            usuario.adicionarPontos(missao.getRecompensa());
            usuario.setRecompensa("Missão concluída: " + missao.getDescricao());
            return true;
        }
        return false;
    }

    private void validarDados(Missao missao) {
        if (missao.getDescricao() == null || missao.getDescricao().isBlank()) {
            throw new EcoRankException("A descrição da missão é obrigatória");
        }
        if (missao.getDescricao().trim().length() > 200) {
            throw new EcoRankException("A descrição da missão deve ter no máximo 200 caracteres");
        }
        if (missao.getObjetivo() <= 0 || missao.getObjetivo() > 99999) {
            throw new EcoRankException("O objetivo deve ser entre 1 e 99999");
        }
        if (missao.getRecompensa() < 0) {
            throw new EcoRankException("A recompensa não pode ser negativa");
        }
        if (missao.getPrazo() == null) {
            throw new EcoRankException("O prazo da missão é obrigatório");
        }
    }
}
