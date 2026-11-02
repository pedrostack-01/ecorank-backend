package br.com.ecorank.ecorank.mapper;

import br.com.ecorank.ecorank.dto.UsuarioDto;
import br.com.ecorank.ecorank.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class UsuarioMapper {

    // Pontos, nivel e recompensa sao controlados pelo sistema (service)
    public static Usuario toEntity(UsuarioDto dto) {
        Usuario usuario = new Usuario();
        usuario.setId(dto.id());
        usuario.setNome(dto.nome());
        usuario.setEmail(dto.email());
        return usuario;
    }

    public static UsuarioDto toDto(Usuario usuario) {
        return new UsuarioDto(
                usuario.getId(),
                usuario.getNome(),
                usuario.getEmail(),
                usuario.getPontos(),
                usuario.getNivel(),
                usuario.getRecompensa()
        );
    }

    public static List<UsuarioDto> toDtoList(List<Usuario> usuarios) {
        List<UsuarioDto> lista = new ArrayList<>();
        for (Usuario usuario : usuarios) {
            lista.add(toDto(usuario));
        }
        return lista;
    }
}
