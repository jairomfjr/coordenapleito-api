package com.coordenapleito.domain.service.localvotacao;

import com.coordenapleito.api.input.LocalVotacaoInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.LocalVotacao;

import java.util.Locale;

final class LocalVotacaoRegras {

    private LocalVotacaoRegras() {}

    static void validar(LocalVotacaoInput input) {
        if (input == null) {
            throw new NegocioException("Dados do local de votação são obrigatórios");
        }
        validarCampos(
                input.getZona(),
                input.getLocalVotacao(),
                input.getEndereco(),
                input.getBairro(),
                input.getQtdSecoes(),
                input.getQtdEleitores(),
                input.getQtdCoordenadores());
    }

    static void validar(LocalVotacao entidade) {
        if (entidade == null) {
            throw new NegocioException("Dados do local de votação são obrigatórios");
        }
        validarCampos(
                entidade.getZona(),
                entidade.getLocalVotacao(),
                entidade.getEndereco(),
                entidade.getBairro(),
                entidade.getQtdSecoes(),
                entidade.getQtdEleitores(),
                entidade.getQtdCoordenadores());
    }

    static void aplicar(LocalVotacao destino, LocalVotacaoInput input) {
        destino.setZona(input.getZona());
        destino.setLocalVotacao(normalizarTexto(input.getLocalVotacao()));
        destino.setEndereco(normalizarTexto(input.getEndereco()));
        destino.setBairro(normalizarTexto(input.getBairro()));
        destino.setQtdSecoes(input.getQtdSecoes());
        destino.setQtdEleitores(input.getQtdEleitores());
        destino.setQtdCoordenadores(input.getQtdCoordenadores());
    }

    static void aplicarSomenteCoordenadores(LocalVotacao destino, LocalVotacaoInput input) {
        if (input == null) {
            throw new NegocioException("Dados do local de votação são obrigatórios");
        }
        if (input.getQtdCoordenadores() == null) {
            throw new NegocioException("Quantidade de coordenadores é obrigatória");
        }
        if (input.getQtdCoordenadores() < 0) {
            throw new NegocioException("Quantidade de coordenadores não pode ser negativa");
        }
        destino.setQtdCoordenadores(input.getQtdCoordenadores());
    }

    static String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? trimmed : trimmed.toUpperCase(Locale.ROOT);
    }

    private static void validarCampos(
            Integer zona,
            String localVotacao,
            String endereco,
            String bairro,
            Integer qtdSecoes,
            Integer qtdEleitores,
            Integer qtdCoordenadores) {
        if (zona == null) {
            throw new NegocioException("Zona é obrigatória");
        }
        if (zona < 1) {
            throw new NegocioException("Zona deve ser maior que zero");
        }
        if (localVotacao == null || localVotacao.isBlank()) {
            throw new NegocioException("Local de votação é obrigatório");
        }
        if (endereco == null || endereco.isBlank()) {
            throw new NegocioException("Endereço é obrigatório");
        }
        if (bairro == null || bairro.isBlank()) {
            throw new NegocioException("Bairro é obrigatório");
        }
        if (qtdSecoes == null) {
            throw new NegocioException("Quantidade de seções é obrigatória");
        }
        if (qtdSecoes < 0) {
            throw new NegocioException("Quantidade de seções não pode ser negativa");
        }
        if (qtdEleitores == null) {
            throw new NegocioException("Quantidade de eleitores é obrigatória");
        }
        if (qtdEleitores < 0) {
            throw new NegocioException("Quantidade de eleitores não pode ser negativa");
        }
        if (qtdCoordenadores == null) {
            throw new NegocioException("Quantidade de coordenadores é obrigatória");
        }
        if (qtdCoordenadores < 0) {
            throw new NegocioException("Quantidade de coordenadores não pode ser negativa");
        }
    }
}
