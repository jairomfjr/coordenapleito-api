package com.coordenapleito.domain.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeletaUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final GetUsuarioService getUsuarioService;

    public void deletar(UUID codigo) {
        Usuario usuario = getUsuarioService.findByCode(codigo);
        if (usuario.getGrupos() != null && !usuario.getGrupos().isEmpty()) {
            throw new NegocioException(
                "Não é possível excluir o usuário pois está vinculado a um ou mais grupos. Remova os vínculos do usuário nos grupos antes de excluir.");
        }
        usuarioRepository.delete(usuario);
    }

}
