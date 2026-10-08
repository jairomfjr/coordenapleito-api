package com.coordenapleito.infrastructure.util;

/**
 * Utilitário para normalização de CPF.
 * Remove pontuação (máscara) e mantém apenas dígitos para armazenamento e comparação.
 */
public final class CpfUtils {

    private CpfUtils() {
    }

    /**
     * Remove pontuação do CPF, retornando apenas os dígitos.
     * Aceita formatos como 123.456.789-00 ou 12345678900.
     *
     * @param cpf CPF com ou sem máscara; pode ser null ou em branco
     * @return CPF apenas com dígitos, ou o próprio valor se null/blank
     */
    public static String normalizar(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return cpf;
        }
        return cpf.replaceAll("\\D", "");
    }
}
