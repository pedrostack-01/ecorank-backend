package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.ObjetivoDto;
import br.com.ecorank.ecorank.model.Objetivo;

import java.util.ArrayList;
import java.util.List;

public class ObjetivoMapper {

    // O dono do objetivo (idUsuario) vem do login, nunca do corpo da requisição
    public static Objetivo toEntity(ObjetivoDto dto) {
        Objetivo x = new Objetivo();
        x.setId(dto.id());
        x.setTitulo(dto.titulo());
        x.setDescricao(dto.descricao());
        x.setMeta(dto.meta());
        x.setProgresso(dto.progresso());
        return x;
    }

    public static ObjetivoDto toDto(Objetivo x) {
        return new ObjetivoDto(
                x.getId(),
                x.getTitulo(),
                x.getDescricao(),
                x.getMeta(),
                x.getProgresso()
        );
    }

    public static List<ObjetivoDto> toDtoList(List<Objetivo> lista) {
        List<ObjetivoDto> resp = new ArrayList<>();
        for (Objetivo x : lista) {
            resp.add(toDto(x));
        }
        return resp;
    }
}
