package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.ResumoService;
import br.com.ecorank.ecorank.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/impacto")
public class ImpactoResource {

    private final AuthService authService;
    private final ResumoService resumoService;

    public ImpactoResource(AuthService authService, ResumoService resumoService) {
        this.authService = authService;
        this.resumoService = resumoService;
    }

    @GetMapping("/me")
    public ResponseEntity<?> impacto(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Usuario usuario = authService.autenticar(auth);
            return ResponseEntity.ok(resumoService.montarImpacto(usuario));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
