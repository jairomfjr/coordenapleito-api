package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.domain.filter.LocalVotacaoFilter;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import com.coordenapleito.domain.spec.LocalVotacaoSpec;
import com.coordenapleito.infrastructure.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListLocalVotacaoService {

    private final LocalVotacaoRepository localVotacaoRepository;

    @Transactional(readOnly = true)
    public Page<LocalVotacao> listar(LocalVotacaoFilter filtro, Pageable pageable) {
        return localVotacaoRepository.findAll(
                LocalVotacaoSpec.usandoFiltro(filtro),
                PageableUtil.comSortPadraoSeAusente(pageable, PageableUtil.sortLocalVotacaoPadrao()));
    }
}
