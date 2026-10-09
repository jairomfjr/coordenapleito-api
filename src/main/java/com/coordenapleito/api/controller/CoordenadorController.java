package com.coordenapleito.api.controller;

import com.coordenapleito.api.assembler.GenericAssembler;
import com.coordenapleito.api.dto.CoordenadorModelBasico;
import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.domain.filter.CoordenadorFilter;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.service.coordenador.AtualizaCoordenadorService;
import com.coordenapleito.domain.service.coordenador.CadastroCoordenadorService;
import com.coordenapleito.domain.service.coordenador.DeletaCoordenadorService;
import com.coordenapleito.domain.service.coordenador.GetCoordenadorService;
import com.coordenapleito.domain.service.coordenador.ListCoordenadorService;
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
@RequestMapping("/coordenadores")
@RequiredArgsConstructor
public class CoordenadorController {

    private final ListCoordenadorService listCoordenadorService;
    private final GetCoordenadorService getCoordenadorService;
    private final CadastroCoordenadorService cadastroCoordenadorService;
    private final AtualizaCoordenadorService atualizaCoordenadorService;
    private final DeletaCoordenadorService deletaCoordenadorService;
    private final GenericAssembler genericAssembler;

    @PreAuthorize("hasAuthority('" + Permissoes.Coordenador.LISTAR + "')")
    @GetMapping
    public Page<CoordenadorModelBasico> listar(
            CoordenadorFilter filtro,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<Coordenador> page = listCoordenadorService.listar(filtro, pageable);
        List<CoordenadorModelBasico> content = Objects.requireNonNull(
                genericAssembler.toCollectionModel(page.getContent(), CoordenadorModelBasico.class));
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Coordenador.VISUALIZAR + "')")
    @GetMapping("/{codigo}")
    public CoordenadorModelBasico buscar(@PathVariable UUID codigo) {
        return genericAssembler.toModel(getCoordenadorService.findByCode(codigo), CoordenadorModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Coordenador.CRIAR + "')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CoordenadorModelBasico adicionar(@RequestBody @Valid CoordenadorInput input) {
        return genericAssembler.toModel(cadastroCoordenadorService.salvar(input), CoordenadorModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Coordenador.EDITAR + "')")
    @PutMapping("/{codigo}")
    public CoordenadorModelBasico atualizar(
            @PathVariable UUID codigo,
            @RequestBody @Valid CoordenadorInput input) {
        return genericAssembler.toModel(atualizaCoordenadorService.atualizar(codigo, input), CoordenadorModelBasico.class);
    }

    @PreAuthorize("hasAuthority('" + Permissoes.Coordenador.EXCLUIR + "')")
    @DeleteMapping("/{codigo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remover(@PathVariable UUID codigo) {
        deletaCoordenadorService.deletar(codigo);
    }
}
