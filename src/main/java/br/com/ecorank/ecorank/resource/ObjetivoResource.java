package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.ObjetivoDto;
import br.com.ecorank.ecorank.mapper.ObjetivoMapper;
import br.com.ecorank.ecorank.model.Objetivo;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.ObjetivoService;
import br.com.ecorank.ecorank.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/objetivos")
public class ObjetivoResource {

    private final AuthService authService;
    private final ObjetivoService service;

    public ObjetivoResource(AuthService authService, ObjetivoService service) {
        this.authService = authService;
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Usuario usuario = authService.autenticar(auth);
            return ResponseEntity.ok(ObjetivoMapper.toDtoList(service.listar(usuario)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody ObjetivoDto dto) {
        try {
            Usuario usuario = authService.autenticar(auth);
            Objetivo objetivo = service.salvar(usuario, ObjetivoMapper.toEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ObjetivoMapper.toDto(objetivo));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody ObjetivoDto dto) {
        try {
            Usuario usuario = authService.autenticar(auth);
            Objetivo objetivo = service.atualizar(usuario, id, ObjetivoMapper.toEntity(dto));
            return ResponseEntity.ok(ObjetivoMapper.toDto(objetivo));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            Usuario usuario = authService.autenticar(auth);
            service.excluir(usuario, id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
