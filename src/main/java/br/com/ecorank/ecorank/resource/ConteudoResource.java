package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.ConteudoDto;
import br.com.ecorank.ecorank.mapper.ConteudoMapper;
import br.com.ecorank.ecorank.model.Conteudo;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.ConteudoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/conteudos")
public class ConteudoResource {

    private final ConteudoService service;
    private final AuthService authService;

    public ConteudoResource(ConteudoService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    // Ex.: /api/v1/conteudos?categoria=Reciclagem&busca=coleta
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(value = "categoria", required = false) String categoria,
            @RequestParam(value = "busca", required = false) String busca) {
        try {
            return ResponseEntity.ok(
                    ConteudoMapper.toDtoList(service.listar(categoria, busca)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable long id) {
        try {
            return ResponseEntity.ok(ConteudoMapper.toDto(service.buscarPorId(id)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Somente ADMIN
    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody ConteudoDto dto) {
        try {
            authService.exigirAdmin(auth);
            Conteudo criado = service.salvar(ConteudoMapper.toEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(ConteudoMapper.toDto(criado));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Somente ADMIN
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody ConteudoDto dto) {
        try {
            authService.exigirAdmin(auth);
            Conteudo atualizado = service.atualizar(id, ConteudoMapper.toEntity(dto));
            return ResponseEntity.ok(ConteudoMapper.toDto(atualizado));
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
