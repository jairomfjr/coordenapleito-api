package com.coordenapleito.domain.repository;

import com.coordenapleito.domain.model.LocalVotacao;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LocalVotacaoRepository extends CustomJpaRepository<LocalVotacao, Long> {

    Optional<LocalVotacao> findByCodigo(UUID codigo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LocalVotacao l where l.codigo = :codigo")
    Optional<LocalVotacao> findByCodigoForUpdate(@Param("codigo") UUID codigo);

    @Query(value = """
            select lv.zona as zona,
                   lv.local_votacao as localVotacao,
                   coalesce(lv.qtd_coordenadores, 0) as capacidade,
                   count(c.id) as ocupados
            from local_votacao lv
            left join coordenador c on c.local_trabalho_id = lv.id
            group by lv.id, lv.zona, lv.local_votacao, lv.qtd_coordenadores
            order by lv.zona, lv.local_votacao
            """, nativeQuery = true)
    List<LocalTrabalhoOcupacaoProjection> resumirOcupacaoPorLocalTrabalho();

    boolean existsByZonaAndLocalVotacao(Integer zona, String localVotacao);

    boolean existsByZonaAndLocalVotacaoAndIdNot(Integer zona, String localVotacao, Long id);
}
