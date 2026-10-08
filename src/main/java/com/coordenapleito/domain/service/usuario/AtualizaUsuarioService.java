package com.coordenapleito.domain.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.coordenapleito.api.input.UsuarioInput;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.domain.service.grupo.GetGrupoService;
import com.coordenapleito.infrastructure.util.CpfUtils;

import java.time.ZoneOffset;

@Service
@RequiredArgsConstructor
public class AtualizaUsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final GetGrupoService getGrupoService;

    @Transactional
    public Usuario atualiza(Usuario usuario, UsuarioInput usuarioInput) {
        if (usuario.getCodigo() == null || usuario.getId() == null) {
            throw new RuntimeException("Usuário não encontrado");
        }

        // Data de nascimento vem do front como LocalDateTime (sem timezone). Persistimos como OffsetDateTime em UTC.
        // Fazemos isso explicitamente aqui para não depender de conversões implícitas do ModelMapper.
        if (usuarioInput.getDataNascimento() != null) {
            usuario.setDataNascimento(usuarioInput.getDataNascimento().atOffset(ZoneOffset.UTC));
        } else {
            usuario.setDataNascimento(null);
        }

        if (usuario.getCpf() != null && !usuario.getCpf().isBlank()) {
            usuario.setCpf(CpfUtils.normalizar(usuario.getCpf()));
        }

        if (usuarioInput.getGrupos() != null) {
            usuario.setGrupos(getGrupoService.findAllByUUID(usuarioInput.getGrupos()));
        }

        UsuarioEscopoCadastroValidator.validar(usuario);

        return usuarioRepository.save(usuario);
    }
}
