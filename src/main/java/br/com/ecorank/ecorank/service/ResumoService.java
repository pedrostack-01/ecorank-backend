package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.dto.CategoriaResumoDto;
import br.com.ecorank.ecorank.dto.DiaPontosDto;
import br.com.ecorank.ecorank.dto.ImpactoDto;
import br.com.ecorank.ecorank.dto.ResumoEcoRankDto;
import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.CatalogoAcao;
import br.com.ecorank.ecorank.model.Nivel;
import br.com.ecorank.ecorank.model.Status;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.AcaoSustentavelRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Monta os indicadores do dashboard e da página de impacto a partir
// das ações reais registradas no banco (nada é guardado em duplicidade).
@Service
public class ResumoService {

    private static final String METODOLOGIA =
            "Estimativa: cada tipo de ação do catálogo tem um fator fixo de kg de CO₂ evitado "
            + "(ex.: usar bicicleta ou caminhar = 2 kg). O total soma esses fatores das ações "
            + "aprovadas. Não é uma medição real de emissões.";

    private final AcaoSustentavelRepository acaoRepositorio;
    private final NivelService nivelService;
    private final RankingService rankingService;
    private final CatalogoAcaoService catalogoService;

    public ResumoService(AcaoSustentavelRepository acaoRepositorio,
                         NivelService nivelService,
                         RankingService rankingService,
                         CatalogoAcaoService catalogoService) {
        this.acaoRepositorio = acaoRepositorio;
        this.nivelService = nivelService;
        this.rankingService = rankingService;
        this.catalogoService = catalogoService;
    }

    public ResumoEcoRankDto montarResumo(Usuario usuario) {
        List<AcaoSustentavel> aprovadas = listarAprovadas(usuario.getId());

        // ---- nível atual e próximo (regras da tabela NIVEL) ----
        List<Nivel> niveis = nivelService.listar();
        Nivel atual = null;
        Nivel proximo = null;

        for (Nivel nivel : niveis) {
            if (nivel.getPontosNecessarios() <= usuario.getPontos()) {
                atual = nivel;
            } else if (proximo == null) {
                proximo = nivel;
            }
        }

        int pontosNivelAtual = atual == null ? 0 : atual.getPontosNecessarios();
        double multiplicador = atual == null ? 1.0 : atual.getMultiplicador();
        String nomeProximo = usuario.getNivel();
        int pontosProximo = pontosNivelAtual;
        int restantes = 0;
        int progresso = 100;

        if (proximo != null) {
            nomeProximo = proximo.getNome();
            pontosProximo = proximo.getPontosNecessarios();
            restantes = Math.max(0, pontosProximo - usuario.getPontos());

            int faixa = Math.max(1, pontosProximo - pontosNivelAtual);
            progresso = (usuario.getPontos() - pontosNivelAtual) * 100 / faixa;
            progresso = Math.max(0, Math.min(100, progresso));
        }

        // ---- posição no ranking ----
        List<Usuario> ranking = rankingService.montarRanking();
        int posicao = 0;
        for (int i = 0; i < ranking.size(); i++) {
            if (ranking.get(i).getId() == usuario.getId()) {
                posicao = i + 1;
            }
        }

        // ---- indicadores da semana (hoje e os 6 dias anteriores) ----
        LocalDate inicioSemana = LocalDate.now().minusDays(6);
        int pontosSemana = 0;
        int acoesSemana = 0;
        double co2 = 0;

        for (AcaoSustentavel acao : aprovadas) {
            co2 += acao.getCo2Kg();
            if (!acao.getData().isBefore(inicioSemana)) {
                pontosSemana += acao.getPontos();
                acoesSemana++;
            }
        }

        return new ResumoEcoRankDto(usuario.getId(), usuario.getNome(),
                usuario.getPontos(), aprovadas.size(), usuario.getNivel(), multiplicador,
                nomeProximo, pontosNivelAtual, pontosProximo, restantes, progresso,
                posicao, ranking.size(), pontosSemana, acoesSemana, arredondar(co2),
                montarCategorias(aprovadas));
    }

    public ImpactoDto montarImpacto(Usuario usuario) {
        List<AcaoSustentavel> aprovadas = listarAprovadas(usuario.getId());

        LocalDate hoje = LocalDate.now();
        LocalDate inicio = hoje.minusDays(29);

        Map<LocalDate, Integer> porDia = new TreeMap<>();
        int somaJanela = 0;
        double co2 = 0;

        for (AcaoSustentavel acao : aprovadas) {
            co2 += acao.getCo2Kg();

            if (!acao.getData().isBefore(inicio) && !acao.getData().isAfter(hoje)) {
                porDia.merge(acao.getData(), acao.getPontos(), Integer::sum);
                somaJanela += acao.getPontos();
            }
        }

        // pontos que o usuário tinha antes dos últimos 30 dias
        int pontosIniciais = Math.max(0, usuario.getPontos() - somaJanela);
        int acumulado = pontosIniciais;
        List<DiaPontosDto> evolucao = new ArrayList<>();

        for (Map.Entry<LocalDate, Integer> dia : porDia.entrySet()) {
            acumulado += dia.getValue();
            evolucao.add(new DiaPontosDto(dia.getKey(), dia.getValue(), acumulado));
        }

        return new ImpactoDto(arredondar(co2), aprovadas.size(), usuario.getPontos(),
                pontosIniciais, evolucao, montarCategorias(aprovadas), METODOLOGIA);
    }

    private List<AcaoSustentavel> listarAprovadas(long idUsuario) {
        List<AcaoSustentavel> resp = new ArrayList<>();

        for (AcaoSustentavel acao : acaoRepositorio.findByUsuario(idUsuario)) {
            if (acao.getStatus() == Status.APROVADA) {
                resp.add(acao);
            }
        }
        return resp;
    }

    // Quantidade e percentual de ações por categoria do catálogo.
    // Categorias que não existem no catálogo entram em "Outros".
    private List<CategoriaResumoDto> montarCategorias(List<AcaoSustentavel> aprovadas) {
        List<String> rotulos = new ArrayList<>();
        for (CatalogoAcao item : catalogoService.listar()) {
            if (!rotulos.contains(item.getCategoria()) && !item.getCategoria().equals("Outros")) {
                rotulos.add(item.getCategoria());
            }
        }
        rotulos.add("Outros");

        int[] quantidades = new int[rotulos.size()];

        for (AcaoSustentavel acao : aprovadas) {
            int indice = rotulos.size() - 1;
            for (int i = 0; i < rotulos.size(); i++) {
                if (rotulos.get(i).equalsIgnoreCase(acao.getCategoria())) {
                    indice = i;
                }
            }
            quantidades[indice]++;
        }

        // percentuais arredondados; a diferença vai para a maior categoria (soma = 100)
        int total = aprovadas.size();
        int[] percentuais = new int[rotulos.size()];
        int soma = 0;
        int maior = 0;

        for (int i = 0; i < percentuais.length; i++) {
            percentuais[i] = total == 0 ? 0 : Math.round(quantidades[i] * 100f / total);
            soma += percentuais[i];
            if (quantidades[i] > quantidades[maior]) {
                maior = i;
            }
        }
        if (total > 0) {
            percentuais[maior] += 100 - soma;
        }

        List<CategoriaResumoDto> resp = new ArrayList<>();
        for (int i = 0; i < rotulos.size(); i++) {
            resp.add(new CategoriaResumoDto(rotulos.get(i), quantidades[i], percentuais[i]));
        }
        return resp;
    }

    private double arredondar(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
