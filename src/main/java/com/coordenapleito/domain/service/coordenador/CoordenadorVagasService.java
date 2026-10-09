package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.domain.exception.EntidadeNaoEncontradaException;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CoordenadorVagasService {

    private final CoordenadorRepository coordenadorRepository;
    private final LocalVotacaoRepository localVotacaoRepository;

    public CoordenadorVagas consultar(LocalVotacao local, Long coordenadorIdAtual) {
        if (local == null || local.getId() == null) {
            return CoordenadorVagas.de(0, 0);
        }
        long ocupados = coordenadorIdAtual == null
                ? coordenadorRepository.countByLocalVotacaoId(local.getId())
                : coordenadorRepository.countByLocalVotacaoIdAndIdNot(local.getId(), coordenadorIdAtual);
        return CoordenadorVagas.de(local.getQtdCoordenadores(), ocupados);
    }

    public LocalVotacao bloquearEValidarVaga(UUID localCodigo, Long coordenadorIdAtual) {
        LocalVotacao local = localVotacaoRepository.findByCodigoForUpdate(localCodigo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(
                        "Não existe um local de votação com código " + localCodigo));
        CoordenadorVagas vagas = consultar(local, coordenadorIdAtual);
        if (vagas.esgotado()) {
            throw new NegocioException(
                    "O local de votação atingiu sua capacidade máxima de coordenadores");
        }
        return local;
    }
}
