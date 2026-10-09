package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.api.input.LocalVotacaoInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.LocalVotacao;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocalVotacaoRegrasTest {

    @Test
    void aceitaDadosValidos() {
        assertDoesNotThrow(() -> LocalVotacaoRegras.validar(inputValido()));
    }

    @Test
    void rejeitaZonaInvalida() {
        LocalVotacaoInput input = inputValido();
        input.setZona(0);
        NegocioException ex = assertThrows(NegocioException.class, () -> LocalVotacaoRegras.validar(input));
        assertEquals("Zona deve ser maior que zero", ex.getMessage());
    }

    @Test
    void rejeitaQuantidadeNegativa() {
        LocalVotacaoInput input = inputValido();
        input.setQtdEleitores(-1);
        NegocioException ex = assertThrows(NegocioException.class, () -> LocalVotacaoRegras.validar(input));
        assertEquals("Quantidade de eleitores não pode ser negativa", ex.getMessage());
    }

    @Test
    void normalizaTextoParaMaiusculas() {
        assertEquals("ESCOLA CENTRAL", LocalVotacaoRegras.normalizarTexto("  escola central  "));
    }

    @Test
    void bloqueioAlteraSomenteCoordenadores() {
        LocalVotacao entidade = new LocalVotacao();
        entidade.setZona(1);
        entidade.setLocalVotacao("CRD");
        entidade.setEndereco("AV A");
        entidade.setBairro("MEIRELES");
        entidade.setQtdSecoes(3);
        entidade.setQtdEleitores(922);
        entidade.setQtdCoordenadores(1);

        LocalVotacaoInput input = inputValido();
        input.setZona(99);
        input.setLocalVotacao("OUTRO");
        input.setQtdCoordenadores(8);

        LocalVotacaoRegras.aplicarSomenteCoordenadores(entidade, input);

        assertEquals(1, entidade.getZona());
        assertEquals("CRD", entidade.getLocalVotacao());
        assertEquals(8, entidade.getQtdCoordenadores());
    }

    private static LocalVotacaoInput inputValido() {
        LocalVotacaoInput input = new LocalVotacaoInput();
        input.setZona(12);
        input.setLocalVotacao("Escola Central");
        input.setEndereco("Rua A, 100");
        input.setBairro("Centro");
        input.setQtdSecoes(4);
        input.setQtdEleitores(800);
        input.setQtdCoordenadores(2);
        return input;
    }
}
