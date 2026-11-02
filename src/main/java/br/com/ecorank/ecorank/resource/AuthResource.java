package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.CadastroDto;
import br.com.ecorank.ecorank.dto.LoginDto;
import br.com.ecorank.ecorank.dto.SessaoDto;
import br.com.ecorank.ecorank.mapper.UsuarioMapper;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthResource {

    private final AuthService service;

    public AuthResource(AuthService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<?> cadastrar(@RequestBody CadastroDto dto) {
        try {
            Usuario dados = new Usuario();
            dados.setNome(dto.nome());
            dados.setEmail(dto.email());

            Usuario usuario = service.cadastrar(dados, dto.senha());
            String token = service.criarSessao(usuario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new SessaoDto(token, UsuarioMapper.toDto(usuario)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDto dto) {
        try {
            Usuario usuario = service.login(dto.email(), dto.senha());
            String token = service.criarSessao(usuario);
            return ResponseEntity.ok(new SessaoDto(token, UsuarioMapper.toDto(usuario)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            service.logout(auth);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
