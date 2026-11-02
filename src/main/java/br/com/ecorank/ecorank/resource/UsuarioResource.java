package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.UsuarioDto;
import br.com.ecorank.ecorank.mapper.UsuarioMapper;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/usuarios")
public class UsuarioResource {

    private final UsuarioService service;
    private final AuthService authService;

    public UsuarioResource(UsuarioService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            authService.exigirAdmin(auth);
            List<Usuario> usuarios = service.listar();
            return ResponseEntity.ok(UsuarioMapper.toDtoList(usuarios));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.exigirDonoOuAdmin(auth, id);
            Usuario usuario = service.buscarPorId(id);
            return ResponseEntity.ok(UsuarioMapper.toDto(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody UsuarioDto dto) {
        try {
            authService.exigirAdmin(auth);
            Usuario usuario = UsuarioMapper.toEntity(dto);
            usuario = service.salvar(usuario);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(UsuarioMapper.toDto(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody UsuarioDto dto) {
        try {
            authService.exigirDonoOuAdmin(auth, id);
            Usuario usuario = UsuarioMapper.toEntity(dto);
            usuario = service.atualizar(id, usuario);
            return ResponseEntity.ok(UsuarioMapper.toDto(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.exigirDonoOuAdmin(auth, id);
            service.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
