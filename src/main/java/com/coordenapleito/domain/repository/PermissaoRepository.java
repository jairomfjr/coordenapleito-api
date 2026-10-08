package com.coordenapleito.domain.repository;

import org.springframework.stereotype.Repository;

import com.coordenapleito.domain.model.Permissao;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface PermissaoRepository extends CustomJpaRepository<Permissao, Long> {
    Optional<Permissao> findByCodigo(UUID codigoPermissao);

    List<Permissao> findByCodigoIn(Set<UUID> codigos);

    Optional<Permissao> findByChave(String chave);

    List<Permissao> findByChaveIn(Set<String> chaves);

    List<Permissao> findByAtivoTrueOrderByModuloAscOrdemAsc();
}