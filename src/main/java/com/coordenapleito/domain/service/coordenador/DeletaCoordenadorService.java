package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.domain.exception.EntidadeEmUsoException;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.repository.CoordenadorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletaCoordenadorService {

    private final CoordenadorRepository coordenadorRepository;
    private final GetCoordenadorService getCoordenadorService;

    @Transactional
    public void deletar(UUID codigo) {
        Coordenador entidade = getCoordenadorService.findByCode(codigo);
        try {
            coordenadorRepository.delete(entidade);
            coordenadorRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException(
                    "Não é possível excluir o coordenador pois ele está em uso");
        }
    }
}
