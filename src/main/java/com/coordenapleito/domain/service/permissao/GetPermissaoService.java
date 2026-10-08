package com.coordenapleito.domain.service.permissao;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Permissao;
import com.coordenapleito.domain.repository.PermissaoRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetPermissaoService {

    private final PermissaoRepository permissaoRepository;

    public Permissao findByCode(UUID codigo) {
        return permissaoRepository.findByCodigo(codigo).orElseThrow(
                () -> new EntityNotFoundException("Permissão não encontrada"));
    }

    public Permissao findById(Long id) {
        return permissaoRepository.findById(id).orElseThrow(
                () -> new EntityNotFoundException("Permissão não encontrada"));
    }
}