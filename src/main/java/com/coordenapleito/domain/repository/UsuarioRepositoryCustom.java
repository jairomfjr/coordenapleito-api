package com.coordenapleito.domain.repository;

import com.coordenapleito.domain.model.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

/**
 * Listagem paginada com {@code grupos} carregados em consulta separada (evita
 * {@code HHH90003004}: paginação em memória com fetch de coleção).
 */
public interface UsuarioRepositoryCustom {

    Page<Usuario> findAllComGruposPaginated(Specification<Usuario> spec, Pageable pageable);
}
