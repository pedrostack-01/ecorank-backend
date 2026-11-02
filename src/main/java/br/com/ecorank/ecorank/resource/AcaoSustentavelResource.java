package br.com.ecorank.ecorank.resource;

import br.com.ecorank.ecorank.dto.AcaoSustentavelDto;
import br.com.ecorank.ecorank.mapper.AcaoSustentavelMapper;
import br.com.ecorank.ecorank.dto.CatalogoAcaoDto;
import br.com.ecorank.ecorank.mapper.CatalogoAcaoMapper;
import br.com.ecorank.ecorank.model.CatalogoAcao;
import br.com.ecorank.ecorank.model.AcaoSustentavel;
import br.com.ecorank.ecorank.model.Usuario;
import br.com.ecorank.ecorank.service.AuthService;
import br.com.ecorank.ecorank.service.CatalogoAcaoService;
import br.com.ecorank.ecorank.service.AcaoSustentavelService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/acoes")
public class AcaoSustentavelResource {

    private final AcaoSustentavelService service;
    private final AuthService authService;
    private final CatalogoAcaoService catalogoService;

    public AcaoSustentavelResource(AcaoSustentavelService service,
                                   AuthService authService,
                                   CatalogoAcaoService catalogoService) {
        this.service = service;
        this.authService = authService;
        this.catalogoService = catalogoService;
    }

    // Catálogo oficial de ações (rótulo, categoria e pontos) usado pelo front-end
    @GetMapping("/catalogo")
    public ResponseEntity<?> listarCatalogo() {
        try {
            return ResponseEntity.ok(CatalogoAcaoMapper.toDtoList(catalogoService.listar()));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/catalogo/{id}")
    public ResponseEntity<?> buscarItemDoCatalogo(@PathVariable long id) {
        try {
            return ResponseEntity.ok(CatalogoAcaoMapper.toDto(catalogoService.buscarPorId(id)));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Cadastro, edição e exclusão do catálogo: somente ADMIN
    @PostMapping("/catalogo")
    public ResponseEntity<?> salvarItemDoCatalogo(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody CatalogoAcaoDto dto) {
        try {
            authService.exigirAdmin(auth);
            CatalogoAcao criado = catalogoService.salvar(CatalogoAcaoMapper.toEntity(dto));
            return ResponseEntity.status(HttpStatus.CREATED).body(CatalogoAcaoMapper.toDto(criado));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/catalogo/{id}")
    public ResponseEntity<?> atualizarItemDoCatalogo(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id, @RequestBody CatalogoAcaoDto dto) {
        try {
            authService.exigirAdmin(auth);
            CatalogoAcao atualizado = catalogoService.atualizar(id, CatalogoAcaoMapper.toEntity(dto));
            return ResponseEntity.ok(CatalogoAcaoMapper.toDto(atualizado));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @DeleteMapping("/catalogo/{id}")
    public ResponseEntity<?> excluirItemDoCatalogo(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.exigirAdmin(auth);
            catalogoService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Histórico de ações do usuário logado
    @GetMapping("/me")
    public ResponseEntity<?> listarMinhas(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            Usuario usuario = authService.autenticar(auth);
            List<AcaoSustentavel> acoes = service.listarPorUsuario(usuario.getId());
            return ResponseEntity.ok(AcaoSustentavelMapper.toDtoList(acoes));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    // Envio de uma ação com foto/vídeo (multipart/form-data)
    @PostMapping(value = "/enviar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> enviar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestParam(value = "codigo", required = false) String codigo,
            @RequestParam(value = "descricao", required = false) String descricao,
            @RequestParam(value = "arquivo", required = false) MultipartFile arquivo) {
        try {
            Usuario usuario = authService.autenticar(auth);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(service.registrarComComprovacao(usuario, codigo, descricao, arquivo));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping
    public ResponseEntity<?> listar(
            @RequestHeader(value = "Authorization", required = false) String auth) {
        try {
            authService.exigirAdmin(auth);
            List<AcaoSustentavel> acoes = service.listar();
            return ResponseEntity.ok(AcaoSustentavelMapper.toDtoList(acoes));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<?> listarPorUsuario(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long idUsuario) {
        try {
            authService.exigirDonoOuAdmin(auth, idUsuario);
            List<AcaoSustentavel> acoes = service.listarPorUsuario(idUsuario);
            return ResponseEntity.ok(AcaoSustentavelMapper.toDtoList(acoes));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.autenticar(auth);
            AcaoSustentavel acao = service.buscarPorId(id);
            authService.exigirDonoOuAdmin(auth, acao.getIdUsuario());
            return ResponseEntity.ok(AcaoSustentavelMapper.toDto(acao));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PostMapping
    public ResponseEntity<?> salvar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @RequestBody AcaoSustentavelDto dto) {
        try {
            authService.exigirAdmin(auth);
            AcaoSustentavel acao = AcaoSustentavelMapper.toEntity(dto);
            acao = service.salvar(acao);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(AcaoSustentavelMapper.toDto(acao));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> atualizar(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id,
                                       @RequestBody AcaoSustentavelDto dto) {
        try {
            authService.exigirAdmin(auth);
            AcaoSustentavel acao = AcaoSustentavelMapper.toEntity(dto);
            acao = service.atualizar(id, acao);
            return ResponseEntity.ok(AcaoSustentavelMapper.toDto(acao));
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> excluir(
            @RequestHeader(value = "Authorization", required = false) String auth,
            @PathVariable long id) {
        try {
            authService.autenticar(auth);
            AcaoSustentavel existente = service.buscarPorId(id);
            authService.exigirDonoOuAdmin(auth, existente.getIdUsuario());
            service.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return Erros.tratar(e);
        }
    }
}
