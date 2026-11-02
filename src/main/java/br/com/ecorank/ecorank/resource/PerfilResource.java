package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.UsuarioDto;
import br.com.ecorank.ecorank.mapper.UsuarioMapper;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.UsuarioService;
import br.com.ecorank.ecorank.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios/me")
public class PerfilResource {

    private final AuthService authService;
    private final UsuarioService usuarioService;

    public PerfilResource(AuthService authService, UsuarioService usuarioService) {
        this.authService = authService;
        this.usuarioService = usuarioService;
    }

    // Dados do usuário logado
    @GetMapping
    public ResponseEntity<?> consultar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Usuario usuario = authService.autenticar(auth);
            return ResponseEntity.ok(UsuarioMapper.toDto(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // O usuário só consegue alterar o próprio perfil (nome e e-mail)
    @PutMapping
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody UsuarioDto dto) {
        try {
            Usuario logado = authService.autenticar(auth);
            Usuario atualizado = usuarioService.atualizar(logado.getId(),
                    UsuarioMapper.toEntity(dto));
            return ResponseEntity.ok(UsuarioMapper.toDto(atualizado));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
