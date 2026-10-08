package com.coordenapleito.domain.service.grupo;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.repository.GrupoRepository;

@Service
@RequiredArgsConstructor
public class AtualizaGrupoService {

    private final GrupoRepository grupoRepository;

    @Transactional
    public Grupo atualiza(Grupo grupo) {
        if (grupo.getCodigo() == null || grupo.getId() == null) {
            throw new RuntimeException("Grupo não encontrado");
        }

        return grupoRepository.save(grupo);
    }
}