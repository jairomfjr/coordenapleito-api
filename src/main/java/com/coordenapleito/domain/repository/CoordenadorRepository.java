package com.coordenapleito.domain.repository;

import com.coordenapleito.domain.model.Coordenador;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
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

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    long countByLocalVotacaoId(Long localVotacaoId);

    long countByLocalVotacaoIdAndIdNot(Long localVotacaoId, Long id);

    @Override
    @EntityGraph(attributePaths = {"localTrabalho", "localVotacao"})
    Page<Coordenador> findAll(@Nullable Specification<Coordenador> spec, Pageable pageable);
}
