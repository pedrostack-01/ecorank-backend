package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.NivelDto;
import br.com.ecorank.ecorank.mapper.NivelMapper;
import br.com.ecorank.ecorank.model.Nivel;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.NivelService;
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
@RequestMapping("/api/v1/niveis")
public class NivelResource {

    private final NivelService service;
    private final AuthService authService;

    public NivelResource(NivelService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Nivel> niveis = service.listar();
            return ResponseEntity.ok(NivelMapper.toDtoList(niveis));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id) {
        try {
            Nivel nivel = service.buscarPorId(id);
            return ResponseEntity.ok(NivelMapper.toDto(nivel));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody NivelDto dto) {
        try {
            authService.exigirAdmin(auth);
            Nivel nivel = NivelMapper.toEntity(dto);
            nivel = service.salvar(nivel);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(NivelMapper.toDto(nivel));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody NivelDto dto) {
        try {
            authService.exigirAdmin(auth);
            Nivel nivel = NivelMapper.toEntity(dto);
            nivel = service.atualizar(id, nivel);
            return ResponseEntity.ok(NivelMapper.toDto(nivel));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

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
