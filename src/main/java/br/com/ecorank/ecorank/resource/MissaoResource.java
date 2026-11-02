package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.MissaoDto;
import br.com.ecorank.ecorank.mapper.MissaoMapper;
import br.com.ecorank.ecorank.model.Missao;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.MissaoService;
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
@RequestMapping("/api/v1/missoes")
public class MissaoResource {

    private final MissaoService service;
    private final AuthService authService;

    public MissaoResource(MissaoService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            authService.exigirAdmin(auth);
            List<Missao> missoes = service.listar();
            return ResponseEntity.ok(MissaoMapper.toDtoList(missoes));
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
            List<Missao> missoes = service.listarPorUsuario(idUsuario);
            return ResponseEntity.ok(MissaoMapper.toDtoList(missoes));
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
            Missao missao = service.buscarPorId(id);
            authService.exigirDonoOuAdmin(auth, missao.getIdUsuario());
            return ResponseEntity.ok(MissaoMapper.toDto(missao));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody MissaoDto dto) {
        try {
            authService.exigirAdmin(auth);
            Missao missao = MissaoMapper.toEntity(dto);
            missao = service.salvar(missao);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(MissaoMapper.toDto(missao));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody MissaoDto dto) {
        try {
            authService.exigirAdmin(auth);
            Missao missao = MissaoMapper.toEntity(dto);
            missao = service.atualizar(id, missao);
            return ResponseEntity.ok(MissaoMapper.toDto(missao));
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
