package com.coordenapleito.infrastructure.util;

public final class CpfValidador {

    private CpfValidador() {}

    public static boolean isValido(String cpf) {
        String digitos = CpfUtils.normalizar(cpf);
        if (digitos == null || digitos.length() != 11 || digitos.chars().distinct().count() == 1) {
            return false;
        }
        return dv(digitos, 9) == digitos.charAt(9) - '0' && dv(digitos, 10) == digitos.charAt(10) - '0';
    }

    private static int dv(String digitos, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += (digitos.charAt(i) - '0') * ((tamanho + 1) - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }
}
