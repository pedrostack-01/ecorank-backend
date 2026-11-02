package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.ContatoDto;
import br.com.ecorank.ecorank.mapper.ContatoMapper;
import br.com.ecorank.ecorank.model.Contato;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.ContatoService;
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

@RestController
@RequestMapping("/api/v1/contato")
public class ContatoResource {

    private final ContatoService service;
    private final AuthService authService;

    public ContatoResource(ContatoService service, AuthService authService) {
        this.service = service;
        this.authService = authService;
    }

    // Formulário "Fale conosco" (não exige login)
    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody ContatoDto dto) {
        try {
            Contato contato = service.salvar(ContatoMapper.toEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ContatoMapper.toDto(contato));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // As mensagens recebidas só podem ser vistas pela equipe (ADMIN)
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            authService.exigirAdmin(auth);
            return ResponseEntity.ok(ContatoMapper.toDtoList(service.listar()));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.exigirAdmin(auth);
            return ResponseEntity.ok(ContatoMapper.toDto(service.buscarPorId(id)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Atualiza o andamento: NOVO, EM_ANDAMENTO ou RESOLVIDO (usa o campo "status")
    @PutMapping("/{id}")
    public ResponseEntity<?> atualizarStatus(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody ContatoDto dto) {
        try {
            authService.exigirAdmin(auth);
            return ResponseEntity.ok(ContatoMapper.toDto(service.atualizarStatus(id, dto.status())));
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
