package com.coordenapleito.domain.service.permissao;

import com.coordenapleito.api.input.PermissaoInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.PermissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastroPermissaoService {

    private final PermissaoRepository permissaoRepository;

    @Transactional
    public Permissao salvar(PermissaoInput input) {
        if (input == null || input.getNome() == null || input.getNome().isBlank()) {
            throw new NegocioException("Nome da permissão é obrigatório");
        }
        Permissao permissao = new Permissao();
        permissao.setNome(input.getNome().trim());
        permissao.setDescricao(input.getDescricao() != null ? input.getDescricao().trim() : null);
        permissao.setAtivo(Boolean.TRUE);
        return permissaoRepository.save(permissao);
    }

    @Transactional
    public Permissao salvar(Permissao permissao) {
        return permissaoRepository.save(permissao);
    }
}
