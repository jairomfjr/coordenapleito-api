package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.dto.CoordenadorVinculoResumoModel;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CoordenadorVinculoResumoService {

    private final LocalVotacaoRepository localVotacaoRepository;

    @Transactional(readOnly = true)
    public CoordenadorVinculoResumoModel resumir() {
        return CoordenadorVinculoResumoCalculator.de(localVotacaoRepository.resumirOcupacaoPorLocalTrabalho());
    }
}
