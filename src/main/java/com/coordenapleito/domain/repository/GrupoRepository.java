package com.coordenapleito.domain.repository;

import org.springframework.stereotype.Repository;

import com.coordenapleito.domain.model.Grupo;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GrupoRepository extends CustomJpaRepository<Grupo, Long> {
    Optional<Grupo> findByCodigo(UUID codigoGrupo);
}