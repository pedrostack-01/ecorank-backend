package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.ConquistaDto;
import br.com.ecorank.ecorank.mapper.ConquistaMapper;
import br.com.ecorank.ecorank.model.Conquista;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.ConquistaService;
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
@RequestMapping("/api/v1/conquistas")
public class ConquistaResource {

    private final ConquistaService service;
    private final AuthService authService;

    public ConquistaResource(ConquistaService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            authService.exigirAdmin(auth);
            List<Conquista> conquistas = service.listar();
            return ResponseEntity.ok(ConquistaMapper.toDtoList(conquistas));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long idUsuario) {
        try {
            authService.exigirDonoOuAdmin(auth, idUsuario);
            List<Conquista> conquistas = service.listarPorUsuario(idUsuario);
            return ResponseEntity.ok(ConquistaMapper.toDtoList(conquistas));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.autenticar(auth);
            Conquista conquista = service.buscarPorId(id);
            authService.exigirDonoOuAdmin(auth, conquista.getIdUsuario());
            return ResponseEntity.ok(ConquistaMapper.toDto(conquista));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody ConquistaDto dto) {
        try {
            authService.exigirAdmin(auth);
            Conquista conquista = ConquistaMapper.toEntity(dto);
            conquista = service.salvar(conquista);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ConquistaMapper.toDto(conquista));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody ConquistaDto dto) {
        try {
            authService.exigirAdmin(auth);
            Conquista conquista = ConquistaMapper.toEntity(dto);
            conquista = service.atualizar(id, conquista);
            return ResponseEntity.ok(ConquistaMapper.toDto(conquista));
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
