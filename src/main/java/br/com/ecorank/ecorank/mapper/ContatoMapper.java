package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.ContatoDto;
import br.com.ecorank.ecorank.model.Contato;

import java.util.ArrayList;
import java.util.List;

public class ContatoMapper {

    // O status é definido pelo sistema (service)
    public static Contato toEntity(ContatoDto dto) {
        Contato x = new Contato();
        x.setNome(dto.nome());
        x.setEmail(dto.email());
        x.setMotivo(dto.motivo());
        x.setMensagem(dto.mensagem());
        return x;
    }

    public static ContatoDto toDto(Contato x) {
        return new ContatoDto(
                x.getId(),
                x.getNome(),
                x.getEmail(),
                x.getMotivo(),
                x.getMensagem(),
                x.getStatus()
        );
    }

    public static List<ContatoDto> toDtoList(List<Contato> lista) {
        List<ContatoDto> resp = new ArrayList<>();
        for (Contato x : lista) {
            resp.add(toDto(x));
        }
        return resp;
    }
}
