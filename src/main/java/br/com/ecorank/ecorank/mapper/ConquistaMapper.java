package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.ConquistaDto;
import br.com.ecorank.ecorank.model.Conquista;

import java.util.ArrayList;
import java.util.List;

public class ConquistaMapper {

    // A data da conquista e definida pelo sistema (service)
    public static Conquista toEntity(ConquistaDto dto) {
        Conquista conquista = new Conquista();
        conquista.setId(dto.id());
        conquista.setIdUsuario(dto.idUsuario());
        conquista.setNome(dto.nome());
        conquista.setDescricao(dto.descricao());
        return conquista;
    }

    public static ConquistaDto toDto(Conquista conquista) {
        return new ConquistaDto(
                conquista.getId(),
                conquista.getIdUsuario(),
                conquista.getNome(),
                conquista.getDescricao(),
                conquista.getDataConquista()
        );
    }

    public static List<ConquistaDto> toDtoList(List<Conquista> conquistas) {
        List<ConquistaDto> lista = new ArrayList<>();
        for (Conquista conquista : conquistas) {
            lista.add(toDto(conquista));
        }
        return lista;
    }
}
