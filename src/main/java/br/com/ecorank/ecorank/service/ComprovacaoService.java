package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.Comprovacao;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.AcaoSustentavelRepository;
import br.com.ecorank.ecorank.repository.ComprovacaoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

// Valida (tipo, tamanho e duração do vídeo) e guarda a foto/vídeo de comprovação.
// O arquivo é salvo no próprio banco Oracle (coluna BLOB), junto com a ação, na
// mesma transação. Assim ele não se perde quando a hospedagem reinicia ou
// reimplanta a aplicação (o disco local do servidor não é permanente).
@Service
public class ComprovacaoService {

    private static final long MAX_IMAGEM = 10L * 1024 * 1024;
    private static final long MAX_VIDEO = 25L * 1024 * 1024;
    private static final double MAX_SEGUNDOS = 45;

    private final ComprovacaoRepository repositorio;
    private final AcaoSustentavelRepository acaoRepositorio;

    public ComprovacaoService(ComprovacaoRepository repositorio,
                              AcaoSustentavelRepository acaoRepositorio) {
        this.repositorio = repositorio;
        this.acaoRepositorio = acaoRepositorio;
    }

    // Confere o arquivo e devolve os dados que serão gravados no banco
    public Comprovacao validar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new EcoRankException("Envie uma foto ou um vídeo");
        }

        byte[] dados;
        try {
            dados = arquivo.getBytes();
        } catch (IOException e) {
            throw new EcoRankException("Não foi possível ler o arquivo enviado");
        }

        String tipo = arquivo.getContentType() == null
                ? "" : arquivo.getContentType().toLowerCase();

        Comprovacao comprovacao = new Comprovacao();
        String extensao;

        if (tipo.startsWith("image/")) {
            extensao = extensaoDaImagem(tipo, dados);
            if (dados.length > MAX_IMAGEM) {
                throw new EcoRankException("A foto deve ter no máximo 10 MB");
            }
        } else if (tipo.startsWith("video/")) {
            extensao = extensaoDoVideo(tipo, dados);
            if (dados.length > MAX_VIDEO) {
                throw new EcoRankException("O vídeo deve ter no máximo 25 MB");
            }

            double segundos = duracaoEmSegundos(dados);
            if (segundos <= 0) {
                throw new EcoRankException("Não foi possível identificar a duração do vídeo");
            }
            if (segundos > MAX_SEGUNDOS) {
                throw new EcoRankException("O vídeo deve ter no máximo 45 segundos");
            }
            comprovacao.setDuracaoSeg((int) Math.ceil(segundos));
        } else {
            throw new EcoRankException(
                    "Envie somente uma foto (JPG, PNG, WEBP ou GIF) ou um vídeo (MP4 ou MOV)");
        }

        comprovacao.setNomeOriginal(nomeSeguro(arquivo.getOriginalFilename()));
        comprovacao.setTipo(tipo);
        comprovacao.setTamanho(dados.length);
        comprovacao.setArquivo(UUID.randomUUID() + "." + extensao);
        return comprovacao;
    }

    // Grava os dados e o arquivo no banco. Se algo falhar, a transação da ação
    // é desfeita e nenhuma ação "sem comprovante" fica registrada.
    public void salvar(Comprovacao comprovacao, MultipartFile arquivo) {
        try {
            repositorio.save(comprovacao, arquivo.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Erro ao ler o arquivo de comprovação", e);
        }
    }

    // Só o dono da ação consegue acessar a comprovação
    public Comprovacao buscarDoUsuario(Usuario usuario, String nomeArquivo) {
        Comprovacao comprovacao = repositorio.findByArquivo(nomeArquivo);
        if (comprovacao == null) {
            throw new RecursoNaoEncontradoException("Comprovação não encontrada");
        }

        AcaoSustentavel acao = acaoRepositorio.findById(comprovacao.getIdAcao());
        if (acao == null || acao.getIdUsuario() != usuario.getId()) {
            throw new RecursoNaoEncontradoException("Comprovação não encontrada");
        }
        return comprovacao;
    }

    public byte[] ler(Comprovacao comprovacao) {
        byte[] conteudo = repositorio.findConteudo(comprovacao.getId());
        if (conteudo == null) {
            throw new RecursoNaoEncontradoException("O arquivo da comprovação não está disponível");
        }
        return conteudo;
    }

    // ---- tipos aceitos (confere o tipo informado E os primeiros bytes do arquivo) ----

    private String extensaoDaImagem(String tipo, byte[] d) {
        if (tipo.equals("image/jpeg") && d.length > 3
                && (d[0] & 0xFF) == 0xFF && (d[1] & 0xFF) == 0xD8 && (d[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (tipo.equals("image/png") && d.length > 4
                && (d[0] & 0xFF) == 0x89 && d[1] == 'P' && d[2] == 'N' && d[3] == 'G') {
            return "png";
        }
        if (tipo.equals("image/gif") && d.length > 4
                && d[0] == 'G' && d[1] == 'I' && d[2] == 'F' && d[3] == '8') {
            return "gif";
        }
        if (tipo.equals("image/webp") && d.length > 12
                && texto(d, 0, 4).equals("RIFF") && texto(d, 8, 4).equals("WEBP")) {
            return "webp";
        }
        throw new EcoRankException("Foto inválida. Use JPG, PNG, WEBP ou GIF");
    }

    private String extensaoDoVideo(String tipo, byte[] d) {
        boolean mp4OuMov = d.length > 12 && texto(d, 4, 4).equals("ftyp");

        if (tipo.equals("video/mp4") && mp4OuMov) {
            return "mp4";
        }
        if (tipo.equals("video/quicktime") && mp4OuMov) {
            return "mov";
        }
        throw new EcoRankException("Formato de vídeo não suportado. Envie MP4 ou MOV");
    }

    // ---- duração do vídeo: lê a caixa "mvhd" (dentro de "moov") do MP4/MOV ----

    private double duracaoEmSegundos(byte[] d) {
        long[] moov = procurarCaixa(d, 0, d.length, "moov");
        if (moov == null) {
            return -1;
        }

        long[] mvhd = procurarCaixa(d, moov[0], moov[1], "mvhd");
        if (mvhd == null) {
            return -1;
        }

        int pos = (int) mvhd[0];
        int versao = d[pos] & 0xFF;
        long escala;
        long duracao;

        if (versao == 1) {
            if (pos + 32 > mvhd[1]) {
                return -1;
            }
            escala = inteiro32(d, pos + 20);
            duracao = inteiro64(d, pos + 24);
        } else {
            if (pos + 20 > mvhd[1]) {
                return -1;
            }
            escala = inteiro32(d, pos + 12);
            duracao = inteiro32(d, pos + 16);
        }

        if (escala <= 0) {
            return -1;
        }
        return (double) duracao / escala;
    }

    // Percorre as caixas (tamanho + tipo) e devolve {inicio, fim} do conteúdo da procurada
    private long[] procurarCaixa(byte[] d, long inicio, long fim, String tipo) {
        long pos = inicio;

        while (pos + 8 <= fim) {
            long tamanho = inteiro32(d, (int) pos);
            String nome = texto(d, (int) pos + 4, 4);
            int cabecalho = 8;

            if (tamanho == 1) {
                if (pos + 16 > fim) {
                    return null;
                }
                tamanho = inteiro64(d, (int) pos + 8);
                cabecalho = 16;
            } else if (tamanho == 0) {
                tamanho = fim - pos;
            }

            if (tamanho < cabecalho || pos + tamanho > fim) {
                return null;
            }
            if (nome.equals(tipo)) {
                return new long[]{pos + cabecalho, pos + tamanho};
            }
            pos += tamanho;
        }
        return null;
    }

    private long inteiro32(byte[] d, int pos) {
        return ((d[pos] & 0xFFL) << 24) | ((d[pos + 1] & 0xFFL) << 16)
                | ((d[pos + 2] & 0xFFL) << 8) | (d[pos + 3] & 0xFFL);
    }

    private long inteiro64(byte[] d, int pos) {
        return (inteiro32(d, pos) << 32) | inteiro32(d, pos + 4);
    }

    private String texto(byte[] d, int pos, int tamanho) {
        return new String(d, pos, tamanho, StandardCharsets.ISO_8859_1);
    }

    // Só o nome do arquivo (sem pasta) e no máximo 200 caracteres
    private String nomeSeguro(String nome) {
        if (nome == null || nome.isBlank()) {
            return "arquivo";
        }
        String limpo = nome.replace("\\", "/");
        limpo = limpo.substring(limpo.lastIndexOf('/') + 1);
        if (limpo.length() > 200) {
            limpo = limpo.substring(limpo.length() - 200);
        }
        return limpo.isBlank() ? "arquivo" : limpo;
    }
}
