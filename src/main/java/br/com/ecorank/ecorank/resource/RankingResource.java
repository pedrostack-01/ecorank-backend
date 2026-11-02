package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.ErroDto;
import br.com.ecorank.ecorank.mapper.RankingMapper;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.RankingService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ranking")
public class RankingResource {

    private final RankingService service;

    public RankingResource(RankingService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> listar() {
        try {
            List<Usuario> usuarios = service.montarRanking();
            return ResponseEntity.ok(RankingMapper.toDtoList(usuarios));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErroDto("Erro interno no servidor"));
        }
    }
}
