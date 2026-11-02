package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.dto.DesafioProgressoDto;
import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.Desafio;
import br.com.ecorank.ecorank.model.ParticipacaoDesafio;
import br.com.ecorank.ecorank.model.Status;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.AcaoSustentavelRepository;
import br.com.ecorank.ecorank.repository.DesafioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// Regra dos desafios: o progresso é calculado a partir das ações APROVADAS
// registradas depois que o usuário começou a participar (não é um número fixo).
@Service
public class DesafioService {

    private final DesafioRepository repositorio;
    private final AcaoSustentavelRepository acaoRepositorio;
    private final UsuarioService usuarioService;
    private final NivelService nivelService;
    private final ConquistaService conquistaService;

    public DesafioService(DesafioRepository repositorio,
                          AcaoSustentavelRepository acaoRepositorio,
                          UsuarioService usuarioService,
                          NivelService nivelService,
                          ConquistaService conquistaService) {
        this.repositorio = repositorio;
        this.acaoRepositorio = acaoRepositorio;
        this.usuarioService = usuarioService;
        this.nivelService = nivelService;
        this.conquistaService = conquistaService;
    }

    public List<Desafio> listar() {
        return repositorio.findAtivos();
    }

    public Desafio buscarPorId(long id) {
        Desafio desafio = repositorio.findById(id);
        if (desafio == null) {
            throw new RecursoNaoEncontradoException("Desafio não encontrado com o id " + id);
        }
        return desafio;
    }

    @Transactional
    public Desafio salvar(Desafio desafio) {
        validarDados(desafio);
        desafio.setTitulo(desafio.getTitulo().trim());
        repositorio.save(desafio);
        return desafio;
    }

    @Transactional
    public Desafio atualizar(long id, Desafio dados) {
        Desafio desafio = buscarPorId(id);
        validarDados(dados);

        desafio.setTitulo(dados.getTitulo().trim());
        desafio.setDescricao(dados.getDescricao());
        desafio.setRecompensa(dados.getRecompensa());
        desafio.setCategoria(dados.getCategoria());
        desafio.setMetaAcoes(dados.getMetaAcoes());
        repositorio.update(desafio);
        return desafio;
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    private void validarDados(Desafio d) {
        if (d.getTitulo() == null || d.getTitulo().isBlank()) {
            throw new EcoRankException("O título do desafio é obrigatório");
        }
        if (d.getTitulo().trim().length() > 100) {
            throw new EcoRankException("O título deve ter no máximo 100 caracteres");
        }
        if (d.getDescricao() == null || d.getDescricao().isBlank()) {
            throw new EcoRankException("A descrição do desafio é obrigatória");
        }
        if (d.getDescricao().length() > 300) {
            throw new EcoRankException("A descrição deve ter no máximo 300 caracteres");
        }
        if (d.getRecompensa() <= 0 || d.getRecompensa() > 10000) {
            throw new EcoRankException("A recompensa deve ser entre 1 e 10000 pontos");
        }
        if (d.getMetaAcoes() <= 0 || d.getMetaAcoes() > 1000) {
            throw new EcoRankException("A meta deve ser entre 1 e 1000 ações");
        }
        if (d.getCategoria() != null && d.getCategoria().isBlank()) {
            d.setCategoria(null);
        }
        if (d.getCategoria() != null && d.getCategoria().length() > 50) {
            throw new EcoRankException("A categoria deve ter no máximo 50 caracteres");
        }
    }

    public List<DesafioProgressoDto> listarComProgresso(Usuario usuario) {
        List<Desafio> desafios = repositorio.findAtivos();
        List<ParticipacaoDesafio> participacoes = repositorio.findParticipacoes(usuario.getId());
        List<AcaoSustentavel> acoes = acaoRepositorio.findByUsuario(usuario.getId());
        List<DesafioProgressoDto> resp = new ArrayList<>();

        for (Desafio desafio : desafios) {
            ParticipacaoDesafio part = buscarParticipacao(participacoes, desafio.getId());

            if (part == null) {
                resp.add(montarDto(desafio, false, false, 0));
            } else if (part.getConcluido()) {
                resp.add(montarDto(desafio, true, true, desafio.getMetaAcoes()));
            } else {
                int feitas = contarAcoes(acoes, desafio, part.getDataInicio());
                resp.add(montarDto(desafio, true, false, feitas));
            }
        }
        return resp;
    }

    @Transactional
    public void participar(Usuario usuario, long idDesafio) {
        Desafio desafio = repositorio.findById(idDesafio);
        if (desafio == null) {
            throw new RecursoNaoEncontradoException("Desafio não encontrado com o id " + idDesafio);
        }

        List<ParticipacaoDesafio> participacoes = repositorio.findParticipacoes(usuario.getId());
        if (buscarParticipacao(participacoes, idDesafio) != null) {
            throw new EcoRankException("Você já participa deste desafio");
        }

        ParticipacaoDesafio nova = new ParticipacaoDesafio();
        nova.setIdUsuario(usuario.getId());
        nova.setIdDesafio(idDesafio);
        nova.setDataInicio(LocalDate.now());
        repositorio.save(nova);

        // ações já registradas hoje também contam
        int bonus = avaliar(usuario);
        if (bonus > 0) {
            nivelService.atualizarNivel(usuario);
            usuarioService.atualizarPontuacao(usuario);
        }
    }

    // Chamado depois de cada ação aprovada: conclui os desafios que bateram a meta
    // e soma a recompensa aos pontos do usuário. Retorna os pontos de bônus.
    // Quem chama é responsável por gravar o usuário (nível e pontuação).
    public int avaliar(Usuario usuario) {
        List<ParticipacaoDesafio> participacoes = repositorio.findParticipacoes(usuario.getId());
        List<Desafio> desafios = repositorio.findAtivos();
        List<AcaoSustentavel> acoes = acaoRepositorio.findByUsuario(usuario.getId());
        int bonus = 0;

        for (ParticipacaoDesafio part : participacoes) {
            if (part.getConcluido()) {
                continue;
            }

            Desafio desafio = buscarDesafio(desafios, part.getIdDesafio());
            if (desafio == null) {
                continue;
            }

            if (contarAcoes(acoes, desafio, part.getDataInicio()) >= desafio.getMetaAcoes()) {
                repositorio.concluir(part.getId());
                usuario.adicionarPontos(desafio.getRecompensa());
                usuario.setRecompensa("Desafio concluído: " + desafio.getTitulo());
                bonus += desafio.getRecompensa();

                conquistaService.desbloquear(
                        usuario.getId(),
                        "Desafio: " + desafio.getTitulo(),
                        "Você concluiu o desafio " + desafio.getTitulo() + "."
                );
            }
        }
        return bonus;
    }

    private int contarAcoes(List<AcaoSustentavel> acoes, Desafio desafio, LocalDate inicio) {
        int total = 0;

        for (AcaoSustentavel acao : acoes) {
            boolean aprovada = acao.getStatus() == Status.APROVADA;
            boolean depoisDoInicio = !acao.getData().isBefore(inicio);
            boolean categoriaOk = desafio.getCategoria() == null
                    || desafio.getCategoria().equalsIgnoreCase(acao.getCategoria());

            if (aprovada && depoisDoInicio && categoriaOk) {
                total++;
            }
        }
        return total;
    }

    private DesafioProgressoDto montarDto(Desafio d, boolean participando,
                                          boolean concluido, int feitas) {
        int progresso = Math.min(100, feitas * 100 / d.getMetaAcoes());
        return new DesafioProgressoDto(d.getId(), d.getTitulo(), d.getDescricao(),
                d.getRecompensa(), d.getCategoria(), d.getMetaAcoes(),
                participando, concluido, Math.min(feitas, d.getMetaAcoes()), progresso);
    }

    private ParticipacaoDesafio buscarParticipacao(List<ParticipacaoDesafio> lista, long idDesafio) {
        for (ParticipacaoDesafio p : lista) {
            if (p.getIdDesafio() == idDesafio) {
                return p;
            }
        }
        return null;
    }

    private Desafio buscarDesafio(List<Desafio> lista, long id) {
        for (Desafio d : lista) {
            if (d.getId() == id) {
                return d;
            }
        }
        return null;
    }
}
