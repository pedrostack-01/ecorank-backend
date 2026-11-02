package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.RankingDto;
import br.com.ecorank.ecorank.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class RankingMapper {

    // A lista de usuarios ja chega ordenada; o email nao e exibido no ranking
    public static List<RankingDto> toDtoList(List<Usuario> usuarios) {
        List<RankingDto> lista = new ArrayList<>();
        for (int i = 0; i < usuarios.size(); i++) {
            Usuario usuario = usuarios.get(i);
            lista.add(new RankingDto(
                    i + 1,
                    usuario.getId(),
                    usuario.getNome(),
                    usuario.getPontos(),
                    usuario.getNivel()
            ));
        }
        return lista;
    }
}
