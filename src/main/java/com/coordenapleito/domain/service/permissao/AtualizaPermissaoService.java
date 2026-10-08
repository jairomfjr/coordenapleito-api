package com.coordenapleito.domain.service.permissao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.PermissaoRepository;

@Service
@RequiredArgsConstructor
public class AtualizaPermissaoService {

    private final PermissaoRepository permissaoRepository;

    @Transactional
    public Permissao atualiza(Permissao permissao) {
        if (permissao.getCodigo() == null || permissao.getId() == null) {
            throw new RuntimeException("Permissão não encontrada");
        }
        return permissaoRepository.save(permissao);
    }
}
