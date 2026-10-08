package com.coordenapleito.api.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.disassembler.GenericDisassembler;
import com.coordenapleito.api.dto.PermissaoArvoreNodeModel;
import com.coordenapleito.api.dto.PermissaoModelBasico;
import com.coordenapleito.api.input.PermissaoInput;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.service.permissao.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping(value = "/permissoes", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PermissaoController {

    private final GenericAssembler genericAssembler;
    private final GenericDisassembler genericDisassembler;
    private final ListPermissaoService listPermissaoService;
    private final GetPermissaoService getPermissaoService;
    private final CadastroPermissaoService cadastroPermissaoService;
    private final AtualizaPermissaoService atualizaPermissaoService;
    private final DeletaPermissaoService deletaPermissaoService;
    private final ListPermissaoArvoreService listPermissaoArvoreService;

    @GetMapping("/arvore")
    public List<PermissaoArvoreNodeModel> arvore() {
        return listPermissaoArvoreService.montarArvore();
    }

    @GetMapping
    public Set<PermissaoModelBasico> listarAll() {
        Set<Permissao> permissoes = listPermissaoService.listar();
        return genericAssembler.toCollectionModelSet(permissoes, PermissaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Permissao.VISUALIZAR + "')")
    @GetMapping("/{codigo}")
    public PermissaoModelBasico buscar(@PathVariable UUID codigo) {
        Permissao permissao = getPermissaoService.findByCode(codigo);
        return genericAssembler.toModel(permissao, PermissaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.EDITAR + "')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PermissaoModelBasico adicionar(@RequestBody @Valid PermissaoInput permissaoInput) {
        Permissao permissaoSalva = cadastroPermissaoService.salvar(permissaoInput);
        return genericAssembler.toModel(permissaoSalva, PermissaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.EDITAR + "')")
    @PutMapping("/{codigo}")
    public PermissaoModelBasico atualizar(@PathVariable UUID codigo, @RequestBody @Valid PermissaoInput permissaoInput) {
        Permissao permissao = getPermissaoService.findByCode(codigo);
        genericDisassembler.copyToDomainObject(permissaoInput, permissao);
        Permissao permissaoAtualizada = atualizaPermissaoService.atualiza(permissao);
        return genericAssembler.toModel(permissaoAtualizada, PermissaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Grupo.EDITAR + "')")
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID codigo) {
        deletaPermissaoService.deletar(codigo);
    }
}
