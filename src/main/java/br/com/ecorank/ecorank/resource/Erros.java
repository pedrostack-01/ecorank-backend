package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.ErroDto;
import br.com.ecorank.ecorank.exception.AcessoNegadoException;
import br.com.ecorank.ecorank.exception.EcoRankException;
import br.com.ecorank.ecorank.exception.MuitasTentativasException;
import br.com.ecorank.ecorank.exception.NaoAutorizadoException;
import br.com.ecorank.ecorank.exception.RecursoNaoEncontradoException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// Converte as exceções do sistema no status HTTP correto (usado pelos resources)
public class Erros {

    public static ResponseEntity<ErroDto> tratar(Exception e) {
        if (e instanceof NaoAutorizadoException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErroDto(e.getMessage()));
        }
        if (e instanceof AcessoNegadoException) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErroDto(e.getMessage()));
        }
        if (e instanceof MuitasTentativasException) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(new ErroDto(e.getMessage()));
        }
        if (e instanceof RecursoNaoEncontradoException) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErroDto(e.getMessage()));
        }
        if (e instanceof EcoRankException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ErroDto(e.getMessage()));
        }
        e.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErroDto("Erro interno no servidor"));
    }
}
