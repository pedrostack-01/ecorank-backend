package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.CatalogoAcao;
import br.com.ecorank.ecorank.repository.CatalogoAcaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Catálogo oficial de ações: os pontos são definidos aqui, nunca pelo navegador
@Service
public class CatalogoAcaoService {

    private final CatalogoAcaoRepository repositorio;

    public CatalogoAcaoService(CatalogoAcaoRepository repositorio) {
        this.repositorio = repositorio;
    }

    public List<CatalogoAcao> listar() {
        return repositorio.findAll();
    }

    public CatalogoAcao buscarPorCodigo(String codigo) {
        CatalogoAcao item = repositorio.findByCodigo(codigo);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Ação não encontrada no catálogo: " + codigo);
        }
        return item;
    }

    public CatalogoAcao buscarPorId(long id) {
        CatalogoAcao item = repositorio.findById(id);
        if (item == null) {
            throw new RecursoNaoEncontradoException("Ação do catálogo não encontrada com o id " + id);
        }
        return item;
    }

    @Transactional
    public CatalogoAcao salvar(CatalogoAcao item) {
        validarDados(item);

        if (repositorio.findByCodigo(item.getCodigo()) != null) {
            throw new EcoRankException("Já existe uma ação com o código " + item.getCodigo());
        }
        repositorio.save(item);
        return item;
    }

    @Transactional
    public CatalogoAcao atualizar(long id, CatalogoAcao dados) {
        CatalogoAcao item = buscarPorId(id);
        validarDados(dados);

        CatalogoAcao existente = repositorio.findByCodigo(dados.getCodigo());
        if (existente != null && existente.getId() != id) {
            throw new EcoRankException("Já existe uma ação com o código " + dados.getCodigo());
        }

        item.setCodigo(dados.getCodigo());
        item.setRotulo(dados.getRotulo());
        item.setCategoria(dados.getCategoria());
        item.setPontos(dados.getPontos());
        item.setCo2Kg(dados.getCo2Kg());
        repositorio.update(item);
        return item;
    }

    // As ações já registradas guardam categoria, pontos e CO2 próprios,
    // então apagar um item do catálogo não altera o histórico dos usuários.
    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    private void validarDados(CatalogoAcao item) {
        if (item.getCodigo() == null || !item.getCodigo().matches("[a-z0-9-]{3,50}")) {
            throw new EcoRankException(
                    "O código deve ter de 3 a 50 caracteres: letras minúsculas, números e hífen");
        }
        if (item.getRotulo() == null || item.getRotulo().isBlank() || item.getRotulo().length() > 150) {
            throw new EcoRankException("O rótulo é obrigatório (até 150 caracteres)");
        }
        if (item.getCategoria() == null || item.getCategoria().isBlank() || item.getCategoria().length() > 50) {
            throw new EcoRankException("A categoria é obrigatória (até 50 caracteres)");
        }
        if (item.getPontos() <= 0 || item.getPontos() > 10000) {
            throw new EcoRankException("Os pontos devem ser entre 1 e 10000");
        }
        if (item.getCo2Kg() < 0 || item.getCo2Kg() > 1000) {
            throw new EcoRankException("O CO₂ estimado deve ser entre 0 e 1000 kg");
        }
    }
}
