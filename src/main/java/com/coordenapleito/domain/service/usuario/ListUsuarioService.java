package com.coordenapleito.domain.service.usuario;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.coordenapleito.domain.filter.UsuarioFilter;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.domain.repository.UsuarioRepository;
import com.coordenapleito.domain.spec.UsuarioSpec;
import com.coordenapleito.infrastructure.util.PageableUtil;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListUsuarioService {

    private final UsuarioRepository usuarioRepository;

    public List<Usuario> listarTodos(UsuarioFilter usuarioFilter) {
        return usuarioRepository.findAll(UsuarioSpec.usandoFiltro(usuarioFilter), PageableUtil.sortNomeAlfabetico());
    }

    public Page<Usuario> listar(UsuarioFilter usuarioFilter, Pageable pageable) {
        return usuarioRepository.findAllComGruposPaginated(
                UsuarioSpec.usandoFiltro(usuarioFilter),
                PageableUtil.comSortPadraoSeAusente(pageable, PageableUtil.sortNomeAlfabetico()));
    }
}
