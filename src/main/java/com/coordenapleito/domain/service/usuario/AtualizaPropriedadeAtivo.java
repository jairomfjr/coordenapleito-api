package com.coordenapleito.domain.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AtualizaPropriedadeAtivo {

    private final UsuarioRepository usuarioRepository;
    private final GetUsuarioService getUsuarioService;

    public void atualizaPropriedadeAtivo(UUID codigo, Boolean ativo) {
        Usuario usuario = getUsuarioService.findByCode(codigo);
        usuario.setAtivo(ativo);
        usuarioRepository.save(usuario);
    }

}
