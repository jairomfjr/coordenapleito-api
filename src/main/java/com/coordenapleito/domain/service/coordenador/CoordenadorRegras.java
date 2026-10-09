package com.coordenapleito.domain.service.coordenador;

import com.coordenapleito.api.input.CoordenadorInput;
import com.coordenapleito.domain.exception.NegocioException;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.infrastructure.util.CpfUtils;
import com.coordenapleito.infrastructure.util.CpfValidador;

import java.util.Locale;

final class CoordenadorRegras {

    private CoordenadorRegras() {}

    static void validar(CoordenadorInput input) {
        if (input == null) {
            throw new NegocioException("Dados do coordenador são obrigatórios");
        }
        if (input.getNome() == null || input.getNome().isBlank()) {
            throw new NegocioException("Nome é obrigatório");
        }
        String cpf = CpfUtils.normalizar(input.getCpf());
        if (cpf == null || cpf.isBlank()) {
            throw new NegocioException("CPF é obrigatório");
        }
        if (!CpfValidador.isValido(cpf)) {
            throw new NegocioException("CPF inválido");
        }
        String telefone = input.getTelefone() == null ? "" : input.getTelefone().replaceAll("\\D", "");
        if (telefone.isBlank()) {
            throw new NegocioException("Telefone é obrigatório");
        }
        if (telefone.length() < 10) {
            throw new NegocioException("Telefone deve conter pelo menos 10 dígitos");
        }
        if (input.getEmail() == null || input.getEmail().isBlank()) {
            throw new NegocioException("E-mail é obrigatório");
        }
        if (!input.getEmail().contains("@")) {
            throw new NegocioException("E-mail inválido");
        }
        if (input.getLocalTrabalhoCodigo() == null) {
            throw new NegocioException("Local de trabalho é obrigatório");
        }
        if (input.getLocalVotacaoCodigo() == null) {
            throw new NegocioException("Local de votação é obrigatório");
        }
    }

    static void aplicar(
            Coordenador destino,
            CoordenadorInput input,
            LocalVotacao localTrabalho,
            LocalVotacao localVotacao) {
        destino.setNome(normalizarTexto(input.getNome()));
        destino.setCpf(CpfUtils.normalizar(input.getCpf()));
        destino.setTelefone(input.getTelefone() == null ? null : input.getTelefone().replaceAll("\\D", ""));
        destino.setEmail(input.getEmail() == null ? null : input.getEmail().trim().toLowerCase(Locale.ROOT));
        destino.setLocalTrabalho(localTrabalho);
        destino.setLocalVotacao(localVotacao);
    }

    static String normalizarTexto(String valor) {
        if (valor == null) {
            return null;
        }
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? trimmed : trimmed.toUpperCase(Locale.ROOT);
    }
}
