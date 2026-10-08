package com.coordenapleito.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.disassembler.GenericDisassembler;
import com.coordenapleito.api.dto.GrupoModelBasico;
import com.coordenapleito.api.dto.UsuarioModelBasico;
import com.coordenapleito.api.input.RecuperarSenhaInput;
import com.coordenapleito.api.input.SenhaInput;
import com.coordenapleito.api.input.UsuarioInput;
import com.coordenapleito.domain.filter.UsuarioFilter;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.core.security.PermissoesExpressions;
import com.coordenapleito.domain.service.usuario.*;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final ListUsuarioService listUsuarioService;
    private final GetUsuarioService getUsuarioService;
    private final AtualizaUsuarioService atualizaUsuarioService;
    private final DeletaUsuarioService deletaUsuarioService;
    private final GenericAssembler genericAssembler;
    private final GenericDisassembler genericDisassembler;
    private final SenhaUsuarioService senhaUsuarioService;
    private final AtualizaPropriedadeAtivo atualizarPropriedadeAtivo;
    private final CadastroUsuarioService cadastroUsuarioService;

    @PreAuthorize("hasAuthority('" + Permissoes.Usuario.LISTAR + "')")
    @GetMapping
    public Page<UsuarioModelBasico> listar(UsuarioFilter filtro, @PageableDefault(size = 10) Pageable pageable) {
        Page<Usuario> usuarioPage = listUsuarioService.listar(filtro, pageable);
        List<UsuarioModelBasico> usuarioDTO = Objects.requireNonNull(
                genericAssembler.toCollectionModel(usuarioPage.getContent(), UsuarioModelBasico.class));
        normalizarGruposNaListagem(usuarioDTO);

        return new PageImpl<>(usuarioDTO, usuarioPage.getPageable(), usuarioPage.getTotalElements());
    }

    /**
     * Na listagem só interessam id/código/nome do grupo (sem permissões).
     * Deduplica por código: JOIN FETCH grupos+permissões na mesma query pode repetir o mesmo grupo.
     */
    private static void normalizarGruposNaListagem(List<UsuarioModelBasico> usuarios) {
        for (UsuarioModelBasico u : usuarios) {
            if (u.getGrupos() == null || u.getGrupos().isEmpty()) {
                continue;
            }
            Map<Object, GrupoModelBasico> unicos = new LinkedHashMap<>();
            for (GrupoModelBasico g : u.getGrupos()) {
                if (g == null) {
                    continue;
                }
                g.setPermissoes(null);
                Object chave = g.getCodigo() != null ? g.getCodigo() : g.getId();
                if (chave != null) {
                    unicos.putIfAbsent(chave, g);
                }
            }
            u.setGrupos(new ArrayList<>(unicos.values()));
        }
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Usuario.VISUALIZAR + "')")
    @GetMapping("/{codigo}")
    public UsuarioModelBasico buscar(@PathVariable UUID codigo) {
        Usuario usuario = getUsuarioService.findByCode(codigo);
        UsuarioModelBasico model = genericAssembler.toModel(usuario, UsuarioModelBasico.class);
        if (model != null) {
            normalizarGruposNaListagem(List.of(model));
        }
        return model;
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Usuario.CRIAR + "')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioModelBasico adicionar(@Valid @RequestBody UsuarioInput usuarioInput) {
        Usuario usuarioSalvo = cadastroUsuarioService.salvar(usuarioInput);
        return genericAssembler.toModel(getUsuarioService.findByCode(usuarioSalvo.getCodigo()), UsuarioModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Usuario.EDITAR + "')")
    @PutMapping("/{codigo}")
    public UsuarioModelBasico atualizar(@PathVariable UUID codigo, @Valid @RequestBody UsuarioInput usuarioInput) {
        Usuario usuario = getUsuarioService.findByCode(codigo);
        genericDisassembler.copyToDomainObject(usuarioInput, usuario);
        atualizaUsuarioService.atualiza(usuario, usuarioInput);

        return genericAssembler.toModel(getUsuarioService.findByCode(codigo), UsuarioModelBasico.class);
    }

    @PreAuthorize("isAuthenticated()")
    @PutMapping("/{codigo}/alterar-senha")
    public void alterarSenha(@PathVariable UUID codigo, @Valid @RequestBody SenhaInput senhaInput) {
        senhaUsuarioService.alterarSenha(codigo, senhaInput);
    }

    @PreAuthorize("permitAll()")
    @PutMapping("/recuperar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void recuperarSenha(@Valid @RequestBody RecuperarSenhaInput recuperarSenhaInput) {
        senhaUsuarioService.recuperarSenha(recuperarSenhaInput);
    }

    @PreAuthorize(PermissoesExpressions.ANALISTA_OU_ADMIN)
    @PutMapping("/{codigo}/reenviar-senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reenviarSenha(@PathVariable UUID codigo) {
        senhaUsuarioService.reenviarSenha(codigo);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Usuario.EDITAR + "')")
    @PutMapping("/{codigo}/ativo")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void atualizarPropriedadeAtivo(@PathVariable UUID codigo, @Valid @RequestBody Boolean ativo) {
        atualizarPropriedadeAtivo.atualizaPropriedadeAtivo(codigo, ativo);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Usuario.EXCLUIR + "')")
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable UUID codigo) {
        deletaUsuarioService.deletar(codigo);
    }
}
