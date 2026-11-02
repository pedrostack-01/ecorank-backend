package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.DesafioDto;
import br.com.ecorank.ecorank.model.Desafio;

import java.util.ArrayList;
import java.util.List;

public class DesafioMapper {

    public static Desafio toEntity(DesafioDto dto) {
        Desafio x = new Desafio();
        x.setId(dto.id());
        x.setTitulo(dto.titulo());
        x.setDescricao(dto.descricao());
        x.setRecompensa(dto.recompensa());
        x.setCategoria(dto.categoria());
        x.setMetaAcoes(dto.metaAcoes());
        return x;
    }

    public static DesafioDto toDto(Desafio x) {
        return new DesafioDto(
                x.getId(),
                x.getTitulo(),
                x.getDescricao(),
                x.getRecompensa(),
                x.getCategoria(),
                x.getMetaAcoes()
        );
    }

    public static List<DesafioDto> toDtoList(List<Desafio> lista) {
        List<DesafioDto> resp = new ArrayList<>();
        for (Desafio x : lista) {
            resp.add(toDto(x));
        }
        return resp;
    }
}
