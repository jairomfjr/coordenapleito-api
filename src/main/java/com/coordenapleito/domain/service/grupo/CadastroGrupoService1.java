package com.coordenapleito.domain.service.grupo;

import com.coordenapleito.api.input.GrupoInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Grupo;
import com.coordenapleito.domain.repository.GrupoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CadastroGrupoService1 {

    private final GrupoRepository grupoRepository;

    @Transactional
    public Grupo salvar(GrupoInput input) {
        if (input == null || input.getNome() == null || input.getNome().isBlank()) {
            throw new NegocioException("Nome do grupo é obrigatório");
        }
        Grupo grupo = new Grupo();
        grupo.setNome(input.getNome().trim());
        grupo.setAtivo(Boolean.TRUE);
        return grupoRepository.save(grupo);
    }

    @Transactional
    public Grupo salvar(Grupo grupo) {
        return grupoRepository.save(grupo);
    }
}