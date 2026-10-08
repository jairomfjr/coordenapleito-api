package com.coordenapleito.domain.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.coordenapleito.domain.model.Usuario;

@Repository
public interface UsuarioRepository extends CustomJpaRepository<Usuario, Long>, UsuarioRepositoryCustom {

	Optional<Usuario> findByCodigo(UUID codigoUsuario);

	@Query("""
			SELECT DISTINCT u FROM Usuario u
			LEFT JOIN FETCH u.grupos g
			LEFT JOIN FETCH g.permissoes
			WHERE u.codigo = :codigo
			""")
	Optional<Usuario> findByCodigoForDto(@Param("codigo") UUID codigo);

	@Query("""
			SELECT DISTINCT u FROM Usuario u
			LEFT JOIN FETCH u.grupos g
			LEFT JOIN FETCH g.permissoes
			WHERE u.id = :id
			""")
	Optional<Usuario> findByIdForDto(@Param("id") Long id);

	Optional<Usuario> findByCpf(String cpf);

	@Query("SELECT COALESCE(u.ativo, true) FROM Usuario u WHERE u.cpf = :cpf")
	Optional<Boolean> findAtivoByCpf(@Param("cpf") String cpf);

	/** Carrega usuário com grupos e permissões para montar authorities do JWT. */
	@Query("SELECT DISTINCT u FROM Usuario u LEFT JOIN FETCH u.grupos g LEFT JOIN FETCH g.permissoes WHERE u.cpf = :cpf")
	Optional<Usuario> findByCpfWithGruposAndPermissoes(@Param("cpf") String cpf);

	Boolean existsByCodigo(UUID codigo);
	Optional<Usuario> deleteByCodigo(UUID codigo);
}