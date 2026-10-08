package com.coordenapleito.domain.service.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.GrupoRepository;
import com.coordenapleito.domain.service.permissao.GetPermissaoService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DesassociaGrupoPermissaoService {

    private final GetGrupoService getGrupoService;
    private final GetPermissaoService getPermissaoService;

    private final GrupoRepository grupoRepository;

    @Transactional
    public ResponseEntity<Void> desassociarGrupoPermissao(UUID grupoCodigo, UUID permissaoCodigo) {
        Grupo grupo = getGrupoService.findByCode(grupoCodigo);
        Permissao permissao = getPermissaoService.findByCode(permissaoCodigo);

        grupo.getPermissoes().remove(permissao);
        grupoRepository.save(grupo);

        return ResponseEntity.noContent().build();
    }
}