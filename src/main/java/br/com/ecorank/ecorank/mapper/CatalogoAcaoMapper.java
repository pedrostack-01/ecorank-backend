package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.CatalogoAcaoDto;
import br.com.ecorank.ecorank.model.CatalogoAcao;

import java.util.ArrayList;
import java.util.List;

public class CatalogoAcaoMapper {

    public static CatalogoAcao toEntity(CatalogoAcaoDto dto) {
        CatalogoAcao x = new CatalogoAcao();
        x.setId(dto.id());
        x.setCodigo(dto.codigo());
        x.setRotulo(dto.rotulo());
        x.setCategoria(dto.categoria());
        x.setPontos(dto.pontos());
        x.setCo2Kg(dto.co2Kg());
        return x;
    }

    public static CatalogoAcaoDto toDto(CatalogoAcao x) {
        return new CatalogoAcaoDto(
                x.getId(),
                x.getCodigo(),
                x.getRotulo(),
                x.getCategoria(),
                x.getPontos(),
                x.getCo2Kg()
        );
    }

    public static List<CatalogoAcaoDto> toDtoList(List<CatalogoAcao> lista) {
        List<CatalogoAcaoDto> resp = new ArrayList<>();
        for (CatalogoAcao x : lista) {
            resp.add(toDto(x));
        }
        return resp;
    }
}
