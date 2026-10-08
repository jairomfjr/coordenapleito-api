package com.coordenapleito.domain.service.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.repository.GrupoRepository;
import com.coordenapleito.infrastructure.util.PageableUtil;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListGrupoService {

    private final GrupoRepository grupoRepository;

    public Set<Grupo> listarTodos() {
        List<Grupo> lista = grupoRepository.findAll(PageableUtil.sortNomeAlfabetico());
        return Set.copyOf(lista);
    }

    public Page<Grupo> listar(Pageable pageable) {
        return grupoRepository.findAll(PageableUtil.comSortPadraoSeAusente(pageable, PageableUtil.sortNomeAlfabetico()));
    }
}