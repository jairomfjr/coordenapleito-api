package com.coordenapleito.api.controller;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.dto.LocalVotacaoModelBasico;
import com.coordenapleito.api.input.LocalVotacaoInput;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.domain.filter.LocalVotacaoFilter;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.service.localvotacao.AtualizaLocalVotacaoService;
import com.coordenapleito.domain.service.localvotacao.CadastroLocalVotacaoService;
import com.coordenapleito.domain.service.localvotacao.DeletaLocalVotacaoService;
import com.coordenapleito.domain.service.localvotacao.GetLocalVotacaoService;
import com.coordenapleito.domain.service.localvotacao.ListLocalVotacaoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/locais-votacao")
@RequiredArgsConstructor
public class LocalVotacaoController {

    private final ListLocalVotacaoService listLocalVotacaoService;
    private final GetLocalVotacaoService getLocalVotacaoService;
    private final CadastroLocalVotacaoService cadastroLocalVotacaoService;
    private final AtualizaLocalVotacaoService atualizaLocalVotacaoService;
    private final DeletaLocalVotacaoService deletaLocalVotacaoService;
    private final GenericAssembler genericAssembler;

    @PreAuthorize("hasAuthority('" + Permissoes.LocalVotacao.LISTAR + "') or hasAuthority('"
            + Permissoes.Coordenador.CRIAR + "') or hasAuthority('"
            + Permissoes.Coordenador.EDITAR + "')")
    @GetMapping
    public Page<LocalVotacaoModelBasico> listar(
            LocalVotacaoFilter filtro,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<LocalVotacao> page = listLocalVotacaoService.listar(filtro, pageable);
        List<LocalVotacaoModelBasico> content = Objects.requireNonNull(
                genericAssembler.toCollectionModel(page.getContent(), LocalVotacaoModelBasico.class));
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @PreAuthorize("hasAuthority('" + Permissoes.LocalVotacao.VISUALIZAR + "')")
    @GetMapping("/{codigo}")
    public LocalVotacaoModelBasico buscar(@PathVariable UUID codigo) {
        return genericAssembler.toModel(getLocalVotacaoService.findByCode(codigo), LocalVotacaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.LocalVotacao.CRIAR + "')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LocalVotacaoModelBasico adicionar(@RequestBody @Valid LocalVotacaoInput input) {
        return genericAssembler.toModel(cadastroLocalVotacaoService.salvar(input), LocalVotacaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.LocalVotacao.EDITAR + "')")
    @PutMapping("/{codigo}")
    public LocalVotacaoModelBasico atualizar(
            @PathVariable UUID codigo,
            @RequestBody @Valid LocalVotacaoInput input) {
        return genericAssembler.toModel(atualizaLocalVotacaoService.atualizar(codigo, input), LocalVotacaoModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.LocalVotacao.EXCLUIR + "')")
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID codigo) {
        deletaLocalVotacaoService.deletar(codigo);
    }
}
