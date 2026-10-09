package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.domain.exception.EntidadeNaoEncontradaException;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetLocalVotacaoService {

    private static final String MSG_NAO_ENCONTRADO = "Não existe um local de votação com código %s";

    private final LocalVotacaoRepository localVotacaoRepository;

    @Transactional(readOnly = true)
    public LocalVotacao findByCode(UUID codigo) {
        return localVotacaoRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(String.format(MSG_NAO_ENCONTRADO, codigo)));
    }
}
