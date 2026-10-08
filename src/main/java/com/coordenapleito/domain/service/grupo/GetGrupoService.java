package com.coordenapleito.domain.service.grupo;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.repository.GrupoRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetGrupoService {

    private final GrupoRepository grupoRepository;

    private static final String MSG_GRUPO_NAO_ENCONTRADO = "Não existe um cadastro de Grupo com código %s";

    private static final String MSG_GRUPO_NAO_ENCONTRADO_POR_ID = "Não existe um cadastro de Grupo com código %s";

    public Grupo findByCode(UUID codigo) {
        return grupoRepository.findByCodigo(codigo).orElseThrow(
                () -> new EntityNotFoundException(String.format(MSG_GRUPO_NAO_ENCONTRADO, codigo)));
    }

    public Grupo findById(Long id) {
        return grupoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException(String.format(MSG_GRUPO_NAO_ENCONTRADO_POR_ID, id)));
    }

    public List<Grupo> findAllByUUID(List<UUID> grupos) {

        List<Grupo> list = new ArrayList<>();
        for (UUID g: grupos) {
            Grupo byCode = findByCode(g);
            list.add(byCode);
        }

        return list;
    }
}