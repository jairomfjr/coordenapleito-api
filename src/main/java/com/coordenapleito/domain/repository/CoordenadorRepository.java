package com.coordenapleito.domain.repository;

import com.coordenapleito.domain.model.Coordenador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CoordenadorRepository extends CustomJpaRepository<Coordenador, Long> {

    @EntityGraph(attributePaths = {"localTrabalho", "localVotacao"})
    Optional<Coordenador> findByCodigo(UUID codigo);

    @EntityGraph(attributePaths = {"localTrabalho", "localVotacao"})
    Optional<Coordenador> findByCpf(String cpf);

    @Query("""
            select c from Coordenador c
            join fetch c.localTrabalho
            join fetch c.localVotacao
            where c.cpf = :cpf
            """)
    Optional<Coordenador> findByCpfComLocais(@Param("cpf") String cpf);

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    long countByLocalTrabalhoId(Long localTrabalhoId);

    long countByLocalTrabalhoIdAndIdNot(Long localTrabalhoId, Long id);

    @Override
    @EntityGraph(attributePaths = {"localTrabalho", "localVotacao"})
    Page<Coordenador> findAll(@Nullable Specification<Coordenador> spec, Pageable pageable);
}
