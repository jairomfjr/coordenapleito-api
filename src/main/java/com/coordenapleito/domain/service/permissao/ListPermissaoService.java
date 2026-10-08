package com.coordenapleito.domain.service.permissao;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.PermissaoRepository;
import com.coordenapleito.infrastructure.util.PageableUtil;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ListPermissaoService {

    private final PermissaoRepository permissaoRepository;

    public Set<Permissao> listar() {
        List<Permissao> lista = permissaoRepository.findAll(PageableUtil.sortNomeAlfabetico());
        return Set.copyOf(lista);
    }
}