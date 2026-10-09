package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizaCoordenadorService {

    private final CoordenadorRepository coordenadorRepository;
    private final GetCoordenadorService getCoordenadorService;
    private final CadastroCoordenadorService cadastroCoordenadorService;
    private final CoordenadorVagasService coordenadorVagasService;

    @Transactional
    public Coordenador atualizar(UUID codigo, CoordenadorInput input) {
        CoordenadorRegras.validar(input);
        Coordenador entidade = getCoordenadorService.findByCode(codigo);
        boolean mudouLocal = entidade.getLocalVotacao() == null
                || !entidade.getLocalVotacao().getCodigo().equals(input.getLocalVotacaoCodigo());
        if (mudouLocal) {
            coordenadorVagasService.bloquearEValidarVaga(input.getLocalVotacaoCodigo(), entidade.getId());
        }
        cadastroCoordenadorService.aplicarVinculos(entidade, input);
        cadastroCoordenadorService.garantirCpfUnico(entidade.getCpf(), entidade.getId());
        return coordenadorRepository.save(entidade);
    }
}
