package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.api.input.LocalVotacaoInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import com.coordenapleito.domain.service.coordenador.VinculosCoordenadorEventos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastroLocalVotacaoService {

    private final LocalVotacaoRepository localVotacaoRepository;
    private final VinculosCoordenadorEventos vinculosCoordenadorEventos;

    @Transactional
    public LocalVotacao salvar(LocalVotacaoInput input) {
        LocalVotacaoRegras.validar(input);
        LocalVotacao entidade = new LocalVotacao();
        LocalVotacaoRegras.aplicar(entidade, input);
        garantirUnicidade(entidade, null);
        LocalVotacao salvo = localVotacaoRepository.save(entidade);
        vinculosCoordenadorEventos.notificar();
        return salvo;
    }

    void garantirUnicidade(LocalVotacao entidade, Long idAtual) {
        boolean duplicado = idAtual == null
                ? localVotacaoRepository.existsByZonaAndLocalVotacao(entidade.getZona(), entidade.getLocalVotacao())
                : localVotacaoRepository.existsByZonaAndLocalVotacaoAndIdNot(
                        entidade.getZona(), entidade.getLocalVotacao(), idAtual);
        if (duplicado) {
            throw new NegocioException("Já existe um local de votação com este nome na zona informada");
        }
    }
}
