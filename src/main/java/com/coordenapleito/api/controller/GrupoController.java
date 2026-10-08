package com.coordenapleito.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.disassembler.GenericDisassembler;
import com.coordenapleito.api.dto.GrupoModelBasico;
import com.coordenapleito.api.input.GrupoInput;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.service.grupo.*;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/grupos")
@RequiredArgsConstructor
public class GrupoController {

    private final ListGrupoService listGrupoService;
    private final GetGrupoService getGrupoService;
    private final CadastroGrupoService1 cadastroGrupoService;
    private final AtualizaGrupoService atualizaGrupoService;
    private final DeletaGrupoService deletaGrupoService;
    private final SyncGrupoPermissoesService syncGrupoPermissoesService;

    private final GenericAssembler genericAssembler;
    private final GenericDisassembler genericDisassembler;

    @GetMapping
    public Set<GrupoModelBasico> listar() {
        Set<Grupo> grupos = listGrupoService.listarTodos();

        return genericAssembler.toCollectionModelSet(grupos, GrupoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.VISUALIZAR + "')")
    @GetMapping("/{codigo}")
    public GrupoModelBasico buscar(@PathVariable UUID codigo) {
        Grupo grupo = getGrupoService.findByCode(codigo);

        return genericAssembler.toModel(grupo, GrupoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.CRIAR + "')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GrupoModelBasico adicionar(@RequestBody @Valid GrupoInput grupoInput) {
        Grupo grupoSalvo = cadastroGrupoService.salvar(grupoInput);

        if (grupoInput.getPermissoesCodigos() != null) {
            syncGrupoPermissoesService.syncPermissoes(grupoSalvo.getCodigo(), grupoInput.getPermissoesCodigos());
            grupoSalvo = getGrupoService.findByCode(grupoSalvo.getCodigo());
        }

        return genericAssembler.toModel(grupoSalvo, GrupoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.EDITAR + "')")
    @PutMapping("/{codigo}")
    public GrupoModelBasico atualizar(@PathVariable UUID codigo, @RequestBody @Valid GrupoInput grupoInput) {
        Grupo grupo = getGrupoService.findByCode(codigo);
        genericDisassembler.copyToDomainObject(grupoInput, grupo);
        Grupo grupoAtualizado = atualizaGrupoService.atualiza(grupo);

        if (grupoInput.getPermissoesCodigos() != null) {
            syncGrupoPermissoesService.syncPermissoes(codigo, grupoInput.getPermissoesCodigos());
            grupoAtualizado = getGrupoService.findByCode(codigo);
        }

        return genericAssembler.toModel(grupoAtualizado, GrupoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.EXCLUIR + "')")
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID codigo) {
        deletaGrupoService.deletar(codigo);
    }

}
