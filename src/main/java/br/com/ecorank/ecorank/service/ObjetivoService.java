package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Objetivo;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.ObjetivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ObjetivoService {

    private final ObjetivoRepository repositorio;

    public ObjetivoService(ObjetivoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<Objetivo> listar(Usuario usuario) {
        return repositorio.findByUsuario(usuario.getId());
    }

    @Transactional
    public Objetivo salvar(Usuario usuario, Objetivo objetivo) {
        objetivo.setIdUsuario(usuario.getId());
        objetivo.setProgresso(Math.max(objetivo.getProgresso(), 0));
        validarDados(objetivo);

        objetivo.setTitulo(objetivo.getTitulo().trim());
        repositorio.save(objetivo);
        return objetivo;
    }

    // O progresso dos objetivos pessoais é informado pelo próprio usuário
    @Transactional
    public Objetivo atualizar(Usuario usuario, long id, Objetivo dados) {
        Objetivo objetivo = buscarDoUsuario(usuario, id);
        validarDados(dados);

        objetivo.setTitulo(dados.getTitulo().trim());
        objetivo.setDescricao(dados.getDescricao());
        objetivo.setMeta(dados.getMeta());
        objetivo.setProgresso(dados.getProgresso());
        repositorio.update(objetivo);
        return objetivo;
    }

    @Transactional
    public void excluir(Usuario usuario, long id) {
        buscarDoUsuario(usuario, id);
        repositorio.deleteById(id);
    }

    // Objetivos iniciais de todo usuário novo
    public void criarObjetivosIniciais(long idUsuario) {
        criarPadrao(idUsuario, "Começar a reciclar",
                "Separar corretamente os resíduos da sua casa.", 10);
        criarPadrao(idUsuario, "Reduzir o uso de plástico",
                "Evitar produtos descartáveis no dia a dia.", 7);
    }

    private void criarPadrao(long idUsuario, String titulo, String descricao, int meta) {
        Objetivo objetivo = new Objetivo();
        objetivo.setIdUsuario(idUsuario);
        objetivo.setTitulo(titulo);
        objetivo.setDescricao(descricao);
        objetivo.setMeta(meta);
        objetivo.setProgresso(0);
        repositorio.save(objetivo);
    }

    // Um usuário só enxerga os próprios objetivos
    private Objetivo buscarDoUsuario(Usuario usuario, long id) {
        Objetivo objetivo = repositorio.findById(id);
        if (objetivo == null || objetivo.getIdUsuario() != usuario.getId()) {
            throw new RecursoNaoEncontradoException("Objetivo não encontrado com o id " + id);
        }
        return objetivo;
    }

    private void validarDados(Objetivo objetivo) {
        if (objetivo.getTitulo() == null || objetivo.getTitulo().isBlank()) {
            throw new EcoRankException("O título do objetivo é obrigatório");
        }
        if (objetivo.getTitulo().trim().length() > 100) {
            throw new EcoRankException("O título deve ter no máximo 100 caracteres");
        }
        if (objetivo.getDescricao() != null && objetivo.getDescricao().length() > 300) {
            throw new EcoRankException("A descrição deve ter no máximo 300 caracteres");
        }
        if (objetivo.getMeta() <= 0 || objetivo.getMeta() > 9999) {
            throw new EcoRankException("A meta deve ser entre 1 e 9999");
        }
        if (objetivo.getProgresso() < 0 || objetivo.getProgresso() > objetivo.getMeta()) {
            throw new EcoRankException("O progresso deve ficar entre 0 e a meta");
        }
    }
}
