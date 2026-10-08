package com.coordenapleito.domain.service.usuario;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GetUsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional(readOnly = true)
    public Usuario findByCode(UUID codigo) {
        return usuarioRepository.findByCodigoForDto(codigo).orElseThrow(
                () -> new EntityNotFoundException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public Usuario findById(Long id) {
        return usuarioRepository.findByIdForDto(id).orElseThrow(
                () -> new EntityNotFoundException("Usuário não encontrado"));
    }
}
