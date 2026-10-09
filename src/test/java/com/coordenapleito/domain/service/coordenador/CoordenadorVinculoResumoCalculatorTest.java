package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.dto.CoordenadorVinculoResumoModel;
import com.coordenapleito.domain.repository.LocalTrabalhoOcupacaoProjection;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CoordenadorVinculoResumoCalculatorTest {

    @Test
    void agregaVagasZonasEDestaques() {
        CoordenadorVinculoResumoModel resumo = CoordenadorVinculoResumoCalculator.de(List.of(
                ocupacao(1, "A", 5, 3),
                ocupacao(1, "B", 2, 2),
                ocupacao(2, "C", 4, 1)));

        assertEquals(11, resumo.getCapacidadeTotal());
        assertEquals(6, resumo.getVinculados());
        assertEquals(5, resumo.getVagasDisponiveis());
        assertEquals(1, resumo.getLocaisEsgotados());
        assertEquals(2, resumo.getLocaisComVaga());
        assertEquals(2, resumo.getTotalZonas());
        assertEquals(1, resumo.getZonaMaisVinculos().getZona());
        assertEquals(5, resumo.getZonaMaisVinculos().getVinculados());
        assertEquals(2, resumo.getZonaMenosVinculos().getZona());
        assertEquals(1, resumo.getZonaMenosVinculos().getVinculados());
        assertEquals(54.5, resumo.getPercentualOcupacao());
        assertEquals(2, resumo.getLocaisComMaisVagas().size());
        assertEquals("C", resumo.getLocaisComMaisVagas().get(0).getNome());
        assertEquals(3, resumo.getLocaisComMaisVagas().get(0).getVagasDisponiveis());
    }

    private static LocalTrabalhoOcupacaoProjection ocupacao(int zona, String nome, int capacidade, long ocupados) {
        return new LocalTrabalhoOcupacaoProjection() {
            @Override
            public Integer getZona() {
                return zona;
            }

            @Override
            public String getLocalVotacao() {
                return nome;
            }

            @Override
            public Integer getCapacidade() {
                return capacidade;
            }

            @Override
            public Long getOcupados() {
                return ocupados;
            }
        };
    }
}
