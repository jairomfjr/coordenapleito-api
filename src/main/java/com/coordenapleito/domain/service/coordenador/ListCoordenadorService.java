package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.domain.filter.CoordenadorFilter;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import com.coordenapleito.domain.spec.CoordenadorSpec;
import com.coordenapleito.infrastructure.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListCoordenadorService {

    private final CoordenadorRepository coordenadorRepository;

    @Transactional(readOnly = true)
    public Page<Coordenador> listar(CoordenadorFilter filtro, Pageable pageable) {
        return coordenadorRepository.findAll(
                CoordenadorSpec.usandoFiltro(filtro),
                PageableUtil.comSortPadraoSeAusente(pageable, PageableUtil.sortNomeAlfabetico()));
    }
}
