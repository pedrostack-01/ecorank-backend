package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Conquista;
import br.com.ecorank.ecorank.repository.ConquistaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class ConquistaService {

    private final ConquistaRepository repositorio;
    private final UsuarioService usuarioService;

    public ConquistaService(ConquistaRepository repositorio, UsuarioService usuarioService) {
        this.repositorio = repositorio;
        this.usuarioService = usuarioService;
    }

    public List<Conquista> listar() {
        return repositorio.findAll();
    }

    public List<Conquista> listarPorUsuario(long idUsuario) {
        usuarioService.buscarPorId(idUsuario);
        return repositorio.findByUsuario(idUsuario);
    }

    public Conquista buscarPorId(long id) {
        Conquista conquista = repositorio.findById(id);
        if (conquista == null) {
            throw new RecursoNaoEncontradoException("Conquista não encontrada com o id " + id);
        }
        return conquista;
    }

    @Transactional
    public Conquista salvar(Conquista conquista) {
        if (conquista.getIdUsuario() <= 0) {
            throw new EcoRankException("O idUsuario é obrigatório");
        }
        validarDados(conquista);
        usuarioService.buscarPorId(conquista.getIdUsuario());

        conquista.setNome(conquista.getNome().trim());
        conquista.setDataConquista(LocalDate.now());
        repositorio.save(conquista);
        return conquista;
    }

    @Transactional
    public Conquista atualizar(long id, Conquista dados) {
        Conquista conquista = buscarPorId(id);
        validarDados(dados);

        conquista.setNome(dados.getNome().trim());
        conquista.setDescricao(dados.getDescricao());
        repositorio.update(conquista);
        return conquista;
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    // Concede a conquista ao usuário, caso ele ainda não tenha ganhado
    public void desbloquear(long idUsuario, String nome, String descricao) {
        List<Conquista> conquistas = repositorio.findByUsuario(idUsuario);

        for (Conquista conquista : conquistas) {
            if (conquista.getNome().equalsIgnoreCase(nome)) {
                return;
            }
        }

        Conquista nova = new Conquista(idUsuario, nome, descricao);
        repositorio.save(nova);
    }

    private void validarDados(Conquista conquista) {
        if (conquista.getNome() == null || conquista.getNome().isBlank()) {
            throw new EcoRankException("O nome da conquista é obrigatório");
        }
        if (conquista.getNome().trim().length() > 100) {
            throw new EcoRankException("O nome da conquista deve ter no máximo 100 caracteres");
        }
        if (conquista.getDescricao() != null && conquista.getDescricao().length() > 300) {
            throw new EcoRankException("A descrição deve ter no máximo 300 caracteres");
        }
    }
}
