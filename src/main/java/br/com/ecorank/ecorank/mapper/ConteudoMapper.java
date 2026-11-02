package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.ConteudoDto;
import br.com.ecorank.ecorank.model.Conteudo;

import java.util.ArrayList;
import java.util.List;

public class ConteudoMapper {

    public static Conteudo toEntity(ConteudoDto dto) {
        Conteudo x = new Conteudo();
        x.setId(dto.id());
        x.setCategoria(dto.categoria());
        x.setTitulo(dto.titulo());
        x.setResumo(dto.resumo());
        x.setFonte(dto.fonte());
        x.setData(dto.data());
        x.setUrl(dto.url());
        return x;
    }

    public static ConteudoDto toDto(Conteudo x) {
        return new ConteudoDto(
                x.getId(),
                x.getCategoria(),
                x.getTitulo(),
                x.getResumo(),
                x.getFonte(),
                x.getData(),
                x.getUrl()
        );
    }

    public static List<ConteudoDto> toDtoList(List<Conteudo> lista) {
        List<ConteudoDto> resp = new ArrayList<>();
        for (Conteudo x : lista) {
            resp.add(toDto(x));
        }
        return resp;
    }
}
