package com.coordenapleito.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.dto.PermissaoModelBasico;
import com.coordenapleito.api.input.GrupoPermissoesChavesInput;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.service.grupo.AssociaGrupoPermissaoService;
import com.coordenapleito.domain.service.grupo.DesassociaGrupoPermissaoService;
import com.coordenapleito.domain.service.grupo.GetGrupoService;
import com.coordenapleito.domain.service.grupo.SyncGrupoPermissoesService;

import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(value = "/grupos/{codigoGrupo}/permissoes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class GrupoPermissaoController {

    private final GetGrupoService getGrupoService;
    private final AssociaGrupoPermissaoService associaGrupoPermissaoService;
    private final DesassociaGrupoPermissaoService desassociaGrupoPermissaoService;
    private final SyncGrupoPermissoesService syncGrupoPermissoesService;
    private final GenericAssembler genericAssembler;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('" + Permissoes.Grupo.GERENCIAR_PERMISSOES + "', '" + Permissoes.Grupo.EDITAR + "')")
    public Set<PermissaoModelBasico> listar(@PathVariable UUID codigoGrupo) {
        Grupo grupo = getGrupoService.findByCode(codigoGrupo);

        return genericAssembler.toCollectionModelSet(grupo.getPermissoes(), PermissaoModelBasico.class);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('" + Permissoes.Grupo.GERENCIAR_PERMISSOES + "', '" + Permissoes.Grupo.EDITAR + "')")
    public ResponseEntity<Void> substituirPorChaves(
            @PathVariable UUID codigoGrupo, @RequestBody @Valid GrupoPermissoesChavesInput input) {
        syncGrupoPermissoesService.syncPermissoesPorChaves(codigoGrupo, input.getPermissoes());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{codigoPermissao}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('" + Permissoes.Grupo.GERENCIAR_PERMISSOES + "', '" + Permissoes.Grupo.EDITAR + "')")
    public ResponseEntity<Void> associar(@PathVariable UUID codigoGrupo, @PathVariable UUID codigoPermissao) {
        associaGrupoPermissaoService.associarGrupoPermissao(codigoGrupo, codigoPermissao);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{codigoPermissao}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyAuthority('" + Permissoes.Grupo.GERENCIAR_PERMISSOES + "', '" + Permissoes.Grupo.EDITAR + "')")
    public ResponseEntity<Void> desassociar(@PathVariable UUID codigoGrupo, @PathVariable UUID codigoPermissao) {
        desassociaGrupoPermissaoService.desassociarGrupoPermissao(codigoGrupo, codigoPermissao);

        return ResponseEntity.noContent().build();
    }

}
