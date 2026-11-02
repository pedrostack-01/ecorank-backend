package br.com.ecorank.ecorank.service;

import br.com.ecorank.ecorank.exception.AcessoNegadoException;
import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.NaoAutorizadoException;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.repository.SessaoRepository;
import br.com.ecorank.ecorank.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.HexFormat;

// Cadastro com senha, login, logout, identificação do usuário logado e
// verificação de permissões (dono do dado ou administrador).
//
// Senhas: PBKDF2 com HMAC-SHA256 e salt aleatório (algoritmo do próprio JDK).
// O hash fica no formato "pbkdf2$<iterações>$<hash>", então o número de
// iterações pode ser aumentado no futuro sem invalidar as senhas antigas.
// Senhas antigas (SHA-256 simples, como as do script de demonstração) continuam
// funcionando e são convertidas para PBKDF2 no primeiro login.
//
// Sessões: token aleatório de 256 bits (SecureRandom) salvo na tabela SESSAO,
// com validade de 7 dias e exclusão no logout.
@Service
public class AuthService {

    private static final SecureRandom ALEATORIO = new SecureRandom();
    private static final int ITERACOES = 310_000;
    private static final String PREFIXO = "pbkdf2";

    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepositorio;
    private final SessaoRepository sessaoRepositorio;
    private final ObjetivoService objetivoService;
    private final LimitadorLogin limitadorLogin;

    public AuthService(UsuarioService usuarioService,
                       UsuarioRepository usuarioRepositorio,
                       SessaoRepository sessaoRepositorio,
                       ObjetivoService objetivoService,
                       LimitadorLogin limitadorLogin) {
        this.usuarioService = usuarioService;
        this.usuarioRepositorio = usuarioRepositorio;
        this.sessaoRepositorio = sessaoRepositorio;
        this.objetivoService = objetivoService;
        this.limitadorLogin = limitadorLogin;
    }

    @Transactional
    public Usuario cadastrar(Usuario dados, String senha) {
        validarSenha(senha);

        // valida nome/email, e-mail duplicado e cria o usuário com 0 pontos
        // (o perfil de todo cadastro pelo site é sempre USUARIO)
        dados.setPerfil("USUARIO");
        Usuario usuario = usuarioService.salvar(dados);

        String salt = gerarAleatorio(16);
        usuarioRepositorio.salvarSenha(usuario.getId(), calcularHash(salt, senha), salt);
        objetivoService.criarObjetivosIniciais(usuario.getId());
        return usuario;
    }

    public Usuario login(String email, String senha) {
        String erro = "E-mail ou senha incorretos.";

        if (email == null || email.isBlank() || senha == null) {
            throw new NaoAutorizadoException(erro);
        }

        // bloqueia depois de muitas senhas erradas seguidas
        limitadorLogin.verificar(email);

        Usuario usuario = usuarioRepositorio.findByEmail(email.trim());
        String[] credenciais = usuario == null
                ? null : usuarioRepositorio.buscarSenha(usuario.getId());

        if (credenciais == null || !senhaConfere(credenciais[0], credenciais[1], senha)) {
            limitadorLogin.registrarFalha(email);
            throw new NaoAutorizadoException(erro);
        }

        limitadorLogin.registrarSucesso(email);

        // senha antiga (SHA-256 simples): troca pelo hash PBKDF2 agora que sabemos a senha
        if (!credenciais[0].startsWith(PREFIXO + "$")) {
            String novoSalt = gerarAleatorio(16);
            usuarioRepositorio.salvarSenha(usuario.getId(), calcularHash(novoSalt, senha), novoSalt);
        }
        return usuario;
    }

    public String criarSessao(Usuario usuario) {
        // aproveita o login para apagar as sessões vencidas
        sessaoRepositorio.deleteExpiradas();

        String token = gerarAleatorio(32);
        sessaoRepositorio.save(token, usuario.getId());
        return token;
    }

    public void logout(String cabecalho) {
        String token = extrairToken(cabecalho);
        if (token != null) {
            sessaoRepositorio.deleteByToken(token);
        }
    }

    // Recebe o header "Authorization: Bearer <token>" e devolve o usuário logado
    public Usuario autenticar(String cabecalho) {
        String token = extrairToken(cabecalho);
        if (token == null) {
            throw new NaoAutorizadoException("Faça login para continuar.");
        }

        long idUsuario = sessaoRepositorio.findUsuarioId(token);
        if (idUsuario == 0) {
            throw new NaoAutorizadoException("Sessão inválida ou expirada. Faça login novamente.");
        }

        Usuario usuario = usuarioRepositorio.findById(idUsuario);
        if (usuario == null) {
            throw new NaoAutorizadoException("Sessão inválida ou expirada. Faça login novamente.");
        }
        return usuario;
    }

    // Operações administrativas: só o perfil ADMIN
    public Usuario exigirAdmin(String cabecalho) {
        Usuario usuario = autenticar(cabecalho);
        if (!usuario.isAdmin()) {
            throw new AcessoNegadoException("Acesso restrito a administradores.");
        }
        return usuario;
    }

    // Dados de um usuário: só o próprio usuário ou um administrador
    public Usuario exigirDonoOuAdmin(String cabecalho, long idDono) {
        Usuario usuario = autenticar(cabecalho);
        if (usuario.getId() != idDono && !usuario.isAdmin()) {
            throw new AcessoNegadoException("Você não tem permissão para acessar estes dados.");
        }
        return usuario;
    }

    private String extrairToken(String cabecalho) {
        if (cabecalho == null || !cabecalho.startsWith("Bearer ")) {
            return null;
        }
        String token = cabecalho.substring(7).trim();
        return token.isEmpty() ? null : token;
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.isEmpty()) {
            throw new EcoRankException("A senha é obrigatória");
        }
        if (senha.length() < 6) {
            throw new EcoRankException("A senha deve ter pelo menos 6 caracteres");
        }
        if (senha.length() > 100) {
            throw new EcoRankException("A senha deve ter no máximo 100 caracteres");
        }
    }

    private String gerarAleatorio(int bytes) {
        byte[] valor = new byte[bytes];
        ALEATORIO.nextBytes(valor);
        return HexFormat.of().formatHex(valor);
    }

    // ---- hash de senha ----

    private boolean senhaConfere(String armazenado, String salt, String senha) {
        String esperado = armazenado;
        String calculado;

        if (armazenado.startsWith(PREFIXO + "$")) {
            String[] partes = armazenado.split("\\$");
            if (partes.length != 3) {
                return false;
            }
            int iteracoes;
            try {
                iteracoes = Integer.parseInt(partes[1]);
            } catch (NumberFormatException e) {
                return false;
            }
            calculado = pbkdf2(salt, senha, iteracoes);
            esperado = partes[2];
        } else {
            calculado = sha256Antigo(salt, senha);
        }

        return MessageDigest.isEqual(
                esperado.getBytes(StandardCharsets.UTF_8),
                calculado.getBytes(StandardCharsets.UTF_8));
    }

    private String calcularHash(String salt, String senha) {
        return PREFIXO + "$" + ITERACOES + "$" + pbkdf2(salt, senha, ITERACOES);
    }

    private String pbkdf2(String saltHex, String senha, int iteracoes) {
        try {
            PBEKeySpec spec = new PBEKeySpec(senha.toCharArray(),
                    HexFormat.of().parseHex(saltHex), iteracoes, 256);
            byte[] hash = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
            spec.clearPassword();
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException("Algoritmo de hash indisponível", e);
        }
    }

    // Formato das senhas do script de demonstração: SHA-256(salt + senha)
    private String sha256Antigo(String salt, String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((salt + senha).getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().withUpperCase().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algoritmo de hash indisponível", e);
        }
    }
}
