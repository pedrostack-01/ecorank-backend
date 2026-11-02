package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repositorio;
    private final NivelService nivelService;

    public UsuarioService(UsuarioRepository repositorio, NivelService nivelService) {
        this.repositorio = repositorio;
        this.nivelService = nivelService;
    }

    public List<Usuario> listar() {
        return repositorio.findAll();
    }

    public Usuario buscarPorId(long id) {
        Usuario usuario = repositorio.findById(id);
        if (usuario == null) {
            throw new RecursoNaoEncontradoException("Usuário não encontrado com o id " + id);
        }
        return usuario;
    }

    @Transactional
    public Usuario salvar(Usuario usuario) {
        validarDados(usuario);

        usuario.setNome(usuario.getNome().trim());
        usuario.setEmail(usuario.getEmail().trim().toLowerCase());

        if (repositorio.findByEmail(usuario.getEmail()) != null) {
            throw new EcoRankException("Já existe um usuário cadastrado com este email");
        }

        // Todo usuário novo começa com 0 pontos
        usuario.setPontos(0);
        usuario.setRecompensa("");
        nivelService.atualizarNivel(usuario);

        repositorio.save(usuario);
        return usuario;
    }

    @Transactional
    public Usuario atualizar(long id, Usuario dados) {
        Usuario usuario = buscarPorId(id);
        validarDados(dados);

        String email = dados.getEmail().trim().toLowerCase();
        Usuario outro = repositorio.findByEmail(email);
        if (outro != null && outro.getId() != id) {
            throw new EcoRankException("Já existe um usuário cadastrado com este email");
        }

        usuario.setNome(dados.getNome().trim());
        usuario.setEmail(email);
        repositorio.update(usuario);
        return usuario;
    }

    // Usado pelas outras regras de negócio (ações e missões) para gravar pontos e nível
    public void atualizarPontuacao(Usuario usuario) {
        repositorio.update(usuario);
    }

    @Transactional
    public void excluir(long id) {
        buscarPorId(id);
        repositorio.deleteById(id);
    }

    private void validarDados(Usuario usuario) {
        if (usuario.getNome() == null || usuario.getNome().isBlank()) {
            throw new EcoRankException("O nome é obrigatório");
        }
        if (usuario.getNome().trim().length() > 100) {
            throw new EcoRankException("O nome deve ter no máximo 100 caracteres");
        }
        if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
            throw new EcoRankException("O email é obrigatório");
        }
        String email = usuario.getEmail().trim();
        if (!email.contains("@") || email.contains(" ")) {
            throw new EcoRankException("O email informado é inválido");
        }
        if (email.length() > 100) {
            throw new EcoRankException("O email deve ter no máximo 100 caracteres");
        }
    }
}
