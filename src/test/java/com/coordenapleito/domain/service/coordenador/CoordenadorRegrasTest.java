package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.model.LocalVotacao;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CoordenadorRegrasTest {

    @Test
    void aceitaDadosValidos() {
        assertDoesNotThrow(() -> CoordenadorRegras.validar(inputValido()));
    }

    @Test
    void rejeitaCpfIncompleto() {
        CoordenadorInput input = inputValido();
        input.setCpf("123");
        NegocioException ex = assertThrows(NegocioException.class, () -> CoordenadorRegras.validar(input));
        assertEquals("CPF inválido", ex.getMessage());
    }

    @Test
    void rejeitaLocalTrabalhoAusente() {
        CoordenadorInput input = inputValido();
        input.setLocalTrabalhoCodigo(null);
        NegocioException ex = assertThrows(NegocioException.class, () -> CoordenadorRegras.validar(input));
        assertEquals("Local de trabalho é obrigatório", ex.getMessage());
    }

    @Test
    void aplicaVinculosENormalizaCampos() {
        Coordenador entidade = new Coordenador();
        LocalVotacao trabalho = new LocalVotacao();
        trabalho.setLocalVotacao("TRABALHO");
        LocalVotacao votacao = new LocalVotacao();
        votacao.setLocalVotacao("VOTACAO");

        CoordenadorRegras.aplicar(entidade, inputValido(), trabalho, votacao);

        assertEquals("MARIA SILVA", entidade.getNome());
        assertEquals("52998224725", entidade.getCpf());
        assertEquals("85999998888", entidade.getTelefone());
        assertEquals("maria@email.com", entidade.getEmail());
        assertEquals(trabalho, entidade.getLocalTrabalho());
        assertEquals(votacao, entidade.getLocalVotacao());
    }

    private static CoordenadorInput inputValido() {
        CoordenadorInput input = new CoordenadorInput();
        input.setNome("Maria Silva");
        input.setCpf("529.982.247-25");
        input.setTelefone("(85) 99999-8888");
        input.setEmail("maria@email.com");
        input.setLocalTrabalhoCodigo(UUID.randomUUID());
        input.setLocalVotacaoCodigo(UUID.randomUUID());
        return input;
    }
}
