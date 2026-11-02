package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.DesafioDto;
import br.com.ecorank.ecorank.dto.DesafioProgressoDto;
import br.com.ecorank.ecorank.mapper.DesafioMapper;
import br.com.ecorank.ecorank.model.Desafio;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.DesafioService;
import br.com.ecorank.ecorank.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/v1/desafios")
public class DesafioResource {

    private final AuthService authService;
    private final DesafioService service;

    public DesafioResource(AuthService authService, DesafioService service) {
        this.authService = authService;
        this.service = service;
    }

    // Catálogo de desafios (público)
    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<DesafioProgressoDto> resp = new ArrayList<>();
            for (Desafio d : service.listar()) {
                resp.add(new DesafioProgressoDto(d.getId(), d.getTitulo(), d.getDescricao(),
                        d.getRecompensa(), d.getCategoria(), d.getMetaAcoes(),
                        false, false, 0, 0));
            }
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Desafios com o progresso do usuário logado
    @GetMapping("/me")
    public ResponseEntity<?> listarDoUsuario(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Usuario usuario = authService.autenticar(auth);
            return ResponseEntity.ok(service.listarComProgresso(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping("/{id}/participar")
    public ResponseEntity<?> participar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            Usuario usuario = authService.autenticar(auth);
            service.participar(usuario, id);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.listarComProgresso(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id) {
        try {
            return ResponseEntity.ok(DesafioMapper.toDto(service.buscarPorId(id)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Somente ADMIN
    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody DesafioDto dto) {
        try {
            authService.exigirAdmin(auth);
            Desafio criado = service.salvar(DesafioMapper.toEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(DesafioMapper.toDto(criado));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Somente ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody DesafioDto dto) {
        try {
            authService.exigirAdmin(auth);
            Desafio atualizado = service.atualizar(id, DesafioMapper.toEntity(dto));
            return ResponseEntity.ok(DesafioMapper.toDto(atualizado));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Somente ADMIN
    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.exigirAdmin(auth);
            service.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
