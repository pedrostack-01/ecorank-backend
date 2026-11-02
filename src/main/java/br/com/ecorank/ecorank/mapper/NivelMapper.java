package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.NivelDto;
import br.com.ecorank.ecorank.model.Nivel;

import java.util.ArrayList;
import java.util.List;

public class NivelMapper {

    public static Nivel toEntity(NivelDto dto) {
        Nivel nivel = new Nivel();
        nivel.setId(dto.id());
        nivel.setNome(dto.nome());
        nivel.setPontosNecessarios(dto.pontosNecessarios());
        nivel.setMultiplicador(dto.multiplicador());
        return nivel;
    }

    public static NivelDto toDto(Nivel nivel) {
        return new NivelDto(
                nivel.getId(),
                nivel.getNome(),
                nivel.getPontosNecessarios(),
                nivel.getMultiplicador()
        );
    }

    public static List<NivelDto> toDtoList(List<Nivel> niveis) {
        List<NivelDto> lista = new ArrayList<>();
        for (Nivel nivel : niveis) {
            lista.add(toDto(nivel));
        }
        return lista;
    }
}
