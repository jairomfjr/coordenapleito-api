package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.domain.exception.EntidadeNaoEncontradaException;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetCoordenadorService {

    private static final String MSG_NAO_ENCONTRADO = "Não existe um coordenador com código %s";

    private final CoordenadorRepository coordenadorRepository;

    @Transactional(readOnly = true)
    public Coordenador findByCode(UUID codigo) {
        return coordenadorRepository.findByCodigo(codigo)
                .orElseThrow(() -> new EntidadeNaoEncontradaException(String.format(MSG_NAO_ENCONTRADO, codigo)));
    }
}
