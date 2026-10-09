package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.domain.exception.EntidadeEmUsoException;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletaLocalVotacaoService {

    private final LocalVotacaoRepository localVotacaoRepository;
    private final GetLocalVotacaoService getLocalVotacaoService;

    @Transactional
    public void deletar(UUID codigo) {
        LocalVotacao entidade = getLocalVotacaoService.findByCode(codigo);
        try {
            localVotacaoRepository.delete(entidade);
            localVotacaoRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException(
                    "Não é possível excluir o local de votação pois ele está em uso");
        }
    }
}
