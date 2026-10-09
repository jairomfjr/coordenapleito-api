package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.api.input.LocalVotacaoInput;
import com.coordenapleito.core.security.Permissoes;
import com.coordenapleito.core.security.SecurityUtil;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.domain.repository.LocalVotacaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizaLocalVotacaoService {

    private final LocalVotacaoRepository localVotacaoRepository;
    private final GetLocalVotacaoService getLocalVotacaoService;
    private final CadastroLocalVotacaoService cadastroLocalVotacaoService;
    private final SecurityUtil securityUtil;

    @Transactional
    public LocalVotacao atualizar(UUID codigo, LocalVotacaoInput input) {
        LocalVotacao entidade = getLocalVotacaoService.findByCode(codigo);
        if (securityUtil.temAutoridade(Permissoes.LocalVotacao.BLOQUEAR_CAMPOS)) {
            LocalVotacaoRegras.aplicarSomenteCoordenadores(entidade, input);
            return localVotacaoRepository.save(entidade);
        }
        LocalVotacaoRegras.validar(input);
        LocalVotacaoRegras.aplicar(entidade, input);
        cadastroLocalVotacaoService.garantirUnicidade(entidade, entidade.getId());
        return localVotacaoRepository.save(entidade);
    }
}
