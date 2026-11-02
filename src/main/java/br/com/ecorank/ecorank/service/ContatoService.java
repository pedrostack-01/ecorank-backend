package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.MuitasTentativasException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Contato;
import br.com.ecorank.ecorank.repository.ContatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ContatoService {

    private static final List<String> MOTIVOS =
            List.of("duvida", "suporte", "feedback", "parceria", "outro");

    // Proteção contra spam: limites por hora
    private static final List<String> STATUS = List.of("NOVO", "EM_ANDAMENTO", "RESOLVIDO");

    private static final int MAX_POR_EMAIL = 3;
    private static final int MAX_GERAL = 200;

    private final ContatoRepository repositorio;

    public ContatoService(ContatoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Transactional
    public Contato salvar(Contato contato) {
        validarDados(contato);

        contato.setNome(contato.getNome().trim());
        contato.setEmail(contato.getEmail().trim().toLowerCase());
        contato.setMotivo(contato.getMotivo().trim().toLowerCase());
        contato.setMensagem(contato.getMensagem().trim());
        contato.setStatus("NOVO");

        if (repositorio.contarUltimaHora(contato.getEmail()) >= MAX_POR_EMAIL) {
            throw new MuitasTentativasException(
                    "Você já enviou várias mensagens recentemente. Tente novamente mais tarde.");
        }
        if (repositorio.contarUltimaHora(null) >= MAX_GERAL) {
            throw new MuitasTentativasException(
                    "Estamos recebendo muitas mensagens agora. Tente novamente mais tarde.");
        }

        repositorio.save(contato);
        return contato;
    }

    private void validarDados(Contato contato) {
        if (contato.getNome() == null || contato.getNome().isBlank()) {
            throw new EcoRankException("O nome é obrigatório");
        }
        if (contato.getNome().trim().length() > 100) {
            throw new EcoRankException("O nome deve ter no máximo 100 caracteres");
        }
        if (contato.getEmail() == null || contato.getEmail().isBlank()) {
            throw new EcoRankException("O email é obrigatório");
        }
        String email = contato.getEmail().trim();
        if (!email.contains("@") || email.contains(" ") || email.length() > 100) {
            throw new EcoRankException("O email informado é inválido");
        }
        if (contato.getMotivo() == null
                || !MOTIVOS.contains(contato.getMotivo().trim().toLowerCase())) {
            throw new EcoRankException("Selecione um motivo válido");
        }
        if (contato.getMensagem() == null || contato.getMensagem().trim().length() < 5) {
            throw new EcoRankException("A mensagem deve ter pelo menos 5 caracteres");
        }
        if (contato.getMensagem().trim().length() > 2000) {
            throw new EcoRankException("A mensagem deve ter no máximo 2000 caracteres");
        }
    }

    // ---- consulta e acompanhamento pela equipe (perfil ADMIN) ----

    public List<Contato> listar() {
        return repositorio.findAll();
    }

    public Contato buscarPorId(long id) {
        Contato contato = repositorio.findById(id);
        if (contato == null) {
            throw new RecursoNaoEncontradoException("Mensagem não encontrada com o id " + id);
        }
        return contato;
    }

    @Transactional
    public Contato atualizarStatus(long id, String status) {
        Contato contato = buscarPorId(id);

        if (status == null || !STATUS.contains(status.trim().toUpperCase())) {
            throw new EcoRankException("O status deve ser NOVO, EM_ANDAMENTO ou RESOLVIDO");
        }

        contato.setStatus(status.trim().toUpperCase());
        repositorio.updateStatus(id, contato.getStatus());
        return contato;
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }
}
