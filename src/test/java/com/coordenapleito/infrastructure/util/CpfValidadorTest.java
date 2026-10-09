package com.coordenapleito.infrastructure.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CpfValidadorTest {

    @Test
    void aceitaCpfValido() {
        assertTrue(CpfValidador.isValido("529.982.247-25"));
    }

    @Test
    void rejeitaCpfInvalido() {
        assertFalse(CpfValidador.isValido("123"));
        assertFalse(CpfValidador.isValido("11111111111"));
        assertFalse(CpfValidador.isValido("52998224726"));
    }
}
