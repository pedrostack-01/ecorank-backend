package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Conteudo;
import br.com.ecorank.ecorank.repository.ConteudoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ConteudoService {

    private final ConteudoRepository repositorio;

    public ConteudoService(ConteudoRepository repositorio) {
        this.repositorio = repositorio;
    }

    // Filtra por categoria e/ou por texto (título, categoria, resumo e fonte)
    public List<Conteudo> listar(String categoria, String busca) {
        List<Conteudo> todos = repositorio.findAll();
        List<Conteudo> resp = new ArrayList<>();

        boolean temCategoria = categoria != null && !categoria.isBlank();
        String termos = normalizar(busca);

        for (Conteudo c : todos) {
            if (temCategoria && !c.getCategoria().equalsIgnoreCase(categoria.trim())) {
                continue;
            }
            if (!termos.isEmpty() && !contemTodos(c, termos)) {
                continue;
            }
            resp.add(c);
        }
        return resp;
    }

    // Todas as palavras digitadas precisam aparecer no conteúdo
    private boolean contemTodos(Conteudo c, String termos) {
        String texto = normalizar(c.getTitulo() + " " + c.getCategoria() + " "
                + c.getResumo() + " " + c.getFonte());

        for (String termo : termos.split(" ")) {
            if (!texto.contains(termo)) {
                return false;
            }
        }
        return true;
    }

    // minúsculo e sem acentos, para a busca ignorar maiúsculas e acentuação
    private String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String semAcento = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        return semAcento.toLowerCase().trim().replaceAll("\\s+", " ");
    }

    public Conteudo buscarPorId(long id) {
        Conteudo conteudo = repositorio.findById(id);
        if (conteudo == null) {
            throw new RecursoNaoEncontradoException("Conteúdo não encontrado com o id " + id);
        }
        return conteudo;
    }

    @Transactional
    public Conteudo salvar(Conteudo conteudo) {
        validarDados(conteudo);
        repositorio.save(conteudo);
        return conteudo;
    }

    @Transactional
    public Conteudo atualizar(long id, Conteudo dados) {
        Conteudo conteudo = buscarPorId(id);
        validarDados(dados);

        conteudo.setCategoria(dados.getCategoria());
        conteudo.setTitulo(dados.getTitulo());
        conteudo.setResumo(dados.getResumo());
        conteudo.setFonte(dados.getFonte());
        conteudo.setData(dados.getData());
        conteudo.setUrl(dados.getUrl());
        repositorio.update(conteudo);
        return conteudo;
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    private void validarDados(Conteudo c) {
        if (c.getCategoria() == null || c.getCategoria().isBlank() || c.getCategoria().length() > 50) {
            throw new EcoRankException("A categoria é obrigatória (até 50 caracteres)");
        }
        if (c.getTitulo() == null || c.getTitulo().isBlank() || c.getTitulo().length() > 250) {
            throw new EcoRankException("O título é obrigatório (até 250 caracteres)");
        }
        if (c.getResumo() == null || c.getResumo().isBlank() || c.getResumo().length() > 500) {
            throw new EcoRankException("O resumo é obrigatório (até 500 caracteres)");
        }
        if (c.getFonte() == null || c.getFonte().isBlank() || c.getFonte().length() > 100) {
            throw new EcoRankException("A fonte é obrigatória (até 100 caracteres)");
        }
        if (c.getUrl() == null || c.getUrl().length() > 500
                || !(c.getUrl().startsWith("http://") || c.getUrl().startsWith("https://"))) {
            throw new EcoRankException("A URL deve começar com http:// ou https:// (até 500 caracteres)");
        }
        if (c.getData() == null) {
            c.setData(LocalDate.now());
        }
    }
}
