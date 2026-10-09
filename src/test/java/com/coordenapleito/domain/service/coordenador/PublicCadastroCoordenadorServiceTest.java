package com.coordenapleito.domain.service.coordenador;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PublicCadastroCoordenadorServiceTest {

    @Test
    void mascaraEmailSemExporTitular() {
        assertEquals("ma***@g***.com", PublicCadastroCoordenadorService.mascararEmail("maria@gmail.com"));
    }
}
