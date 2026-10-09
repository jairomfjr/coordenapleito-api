package com.coordenapleito.domain.service.coordenador;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoordenadorVagasTest {

    @Test
    void calculaVagasDisponiveis() {
        CoordenadorVagas vagas = CoordenadorVagas.de(5, 3);
        assertEquals(2, vagas.disponiveis());
        assertFalse(vagas.esgotado());
    }

    @Test
    void marcaEsgotadoQuandoAtingeCapacidade() {
        CoordenadorVagas vagas = CoordenadorVagas.de(5, 5);
        assertEquals(0, vagas.disponiveis());
        assertTrue(vagas.esgotado());
    }
}
