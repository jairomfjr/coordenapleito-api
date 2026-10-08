package com.coordenapleito.domain.service.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.repository.GrupoRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletaGrupoService {

    private final GrupoRepository grupoRepository;
    private final GetGrupoService getGrupoService;

    public void deletar(UUID codigo) {
        Grupo grupo = getGrupoService.findByCode(codigo);
        grupoRepository.delete(grupo);
    }
}