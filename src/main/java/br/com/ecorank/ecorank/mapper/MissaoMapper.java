package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.MissaoDto;
import br.com.ecorank.ecorank.model.Missao;

import java.util.ArrayList;
import java.util.List;

public class MissaoMapper {

    // O progresso e controlado pelo sistema (service)
    public static Missao toEntity(MissaoDto dto) {
        Missao missao = new Missao();
        missao.setId(dto.id());
        missao.setIdUsuario(dto.idUsuario());
        missao.setDescricao(dto.descricao());
        missao.setObjetivo(dto.objetivo());
        missao.setRecompensa(dto.recompensa());
        missao.setPrazo(dto.prazo());
        return missao;
    }

    public static MissaoDto toDto(Missao missao) {
        return new MissaoDto(
                missao.getId(),
                missao.getIdUsuario(),
                missao.getDescricao(),
                missao.getObjetivo(),
                missao.getProgresso(),
                missao.getRecompensa(),
                missao.getPrazo()
        );
    }

    public static List<MissaoDto> toDtoList(List<Missao> missoes) {
        List<MissaoDto> lista = new ArrayList<>();
        for (Missao missao : missoes) {
            lista.add(toDto(missao));
        }
        return lista;
    }
}
