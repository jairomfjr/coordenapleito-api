package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import com.coordenapleito.domain.service.localvotacao.GetLocalVotacaoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastroCoordenadorService {

    private final CoordenadorRepository coordenadorRepository;
    private final GetLocalVotacaoService getLocalVotacaoService;
    private final CoordenadorVagasService coordenadorVagasService;

    @Transactional
    public Coordenador salvar(CoordenadorInput input) {
        CoordenadorRegras.validar(input);
        coordenadorVagasService.bloquearEValidarVaga(input.getLocalVotacaoCodigo(), null);
        Coordenador entidade = new Coordenador();
        aplicarVinculos(entidade, input);
        garantirCpfUnico(entidade.getCpf(), null);
        return coordenadorRepository.save(entidade);
    }

    void aplicarVinculos(Coordenador entidade, CoordenadorInput input) {
        LocalVotacao localTrabalho = getLocalVotacaoService.findByCode(input.getLocalTrabalhoCodigo());
        LocalVotacao localVotacao = getLocalVotacaoService.findByCode(input.getLocalVotacaoCodigo());
        CoordenadorRegras.aplicar(entidade, input, localTrabalho, localVotacao);
    }

    void garantirCpfUnico(String cpf, Long idAtual) {
        boolean duplicado = idAtual == null
                ? coordenadorRepository.existsByCpf(cpf)
                : coordenadorRepository.existsByCpfAndIdNot(cpf, idAtual);
        if (duplicado) {
            throw new NegocioException("Já existe um coordenador com este CPF");
        }
    }
}
