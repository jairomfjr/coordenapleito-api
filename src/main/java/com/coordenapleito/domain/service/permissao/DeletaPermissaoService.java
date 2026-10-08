package com.coordenapleito.domain.service.permissao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.PermissaoRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletaPermissaoService {

    private final PermissaoRepository permissaoRepository;
    private final GetPermissaoService getPermissaoService;

    @Transactional
    public void deletar(UUID codigo) {
        Permissao permissao = getPermissaoService.findByCode(codigo);
        permissaoRepository.delete(permissao);
    }
}
