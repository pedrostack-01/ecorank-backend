package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.AcaoSustentavelDto;
import br.com.ecorank.ecorank.model.AcaoSustentavel;

import java.util.ArrayList;
import java.util.List;

public class AcaoSustentavelMapper {

    // Status, pontos e data sao definidos pelo sistema (service)
    public static AcaoSustentavel toEntity(AcaoSustentavelDto dto) {
        AcaoSustentavel acao = new AcaoSustentavel();
        acao.setId(dto.id());
        acao.setIdUsuario(dto.idUsuario());
        acao.setCategoria(dto.categoria());
        acao.setDescricao(dto.descricao());
        acao.setLinkEvidencia(dto.linkEvidencia());
        return acao;
    }

    public static AcaoSustentavelDto toDto(AcaoSustentavel acao) {
        return new AcaoSustentavelDto(
                acao.getId(),
                acao.getIdUsuario(),
                acao.getCategoria(),
                acao.getDescricao(),
                acao.getLinkEvidencia(),
                acao.getStatus().name(),
                acao.getPontos(),
                acao.getData()
        );
    }

    public static List<AcaoSustentavelDto> toDtoList(List<AcaoSustentavel> acoes) {
        List<AcaoSustentavelDto> lista = new ArrayList<>();
        for (AcaoSustentavel acao : acoes) {
            lista.add(toDto(acao));
        }
        return lista;
    }
}
