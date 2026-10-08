package com.coordenapleito.api.exceptionhandler;

import java.util.Locale;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Traduz mensagens técnicas de JDBC/PostgreSQL para texto compreensível ao usuário da API.
 */
final class SqlErrorMessageResolver {

    private SqlErrorMessageResolver() {}

    static String resolve(Throwable ex) {
        Throwable root = ExceptionUtils.getRootCause(ex);
        String msg = root != null ? root.getMessage() : ex.getMessage();
        if (msg == null || msg.isBlank()) {
            return null;
        }
        String lower = msg.toLowerCase(Locale.ROOT);

        if (lower.contains("casa_cidadao_atendimento_aud") && lower.contains("does not exist")) {
            return "Não foi possível registrar o atendimento: a estrutura de auditoria do Casa Cidadão "
                    + "ainda não foi aplicada no banco de dados. Solicite ao suporte técnico a execução da migração V16.";
        }

        if (lower.contains("uq_casa_cidadao_atendimento")
                || (lower.contains("casa_cidadao_atendimento") && lower.contains("unique constraint"))) {
            return "Este cidadão já possui atendimento deste serviço na data informada.";
        }

        if (lower.contains("caminhao_cidadao_atendimento_aud") && lower.contains("does not exist")) {
            return "Não foi possível registrar o atendimento: a estrutura de auditoria do Caminhão do Cidadão "
                    + "ainda não foi aplicada no banco de dados. Solicite ao suporte técnico a execução da migração V21.";
        }

        if (lower.contains("uq_caminhao_cidadao_atendimento")
                || (lower.contains("caminhao_cidadao_atendimento") && lower.contains("unique constraint"))) {
            return "Este cidadão já possui atendimento deste serviço na data informada.";
        }

        if (lower.contains("42p01") && lower.contains("does not exist")) {
            return "Erro de configuração do banco de dados: estrutura ausente. Contate o suporte técnico.";
        }

        return null;
    }
}
