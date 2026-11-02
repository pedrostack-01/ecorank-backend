package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.model.Comprovacao;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.ComprovacaoService;
import org.springframework.http.MediaType;
import br.com.ecorank.ecorank.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/comprovacoes")
public class ComprovacaoResource {

    private final AuthService authService;
    private final ComprovacaoService service;

    public ComprovacaoResource(AuthService authService, ComprovacaoService service) {
        this.authService = authService;
        this.service = service;
    }

    // Devolve a foto/vídeo enviado; só o dono da ação consegue acessar
    @GetMapping("/{arquivo}")
    public ResponseEntity<?> buscar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable String arquivo) {
        try {
            Usuario usuario = authService.autenticar(auth);
            Comprovacao comprovacao = service.buscarDoUsuario(usuario, arquivo);
            byte[] conteudo = service.ler(comprovacao);

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(comprovacao.getTipo()))
                    .body(conteudo);
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
