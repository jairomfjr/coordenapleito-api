package com.coordenapleito.api.exceptionhandler;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.Locale;

/**
 * Mensagens amigáveis para falhas de avaliação de {@code @PreAuthorize} / SpEL (ex.: EL1008E).
 */
final class SecurityExpressionMessageResolver {

    private static final String MSG_ACESSO_NEGADO =
            "Você não tem permissão para acessar este recurso.";

    private static final String MSG_ERRO_EXPRESSAO_SEGURANCA =
            "Não foi possível validar sua permissão para esta operação. "
                    + "Faça login novamente; se o problema continuar, contate o suporte.";

    private SecurityExpressionMessageResolver() {
    }

    static boolean isSecurityExpressionFailure(Throwable ex) {
        for (Throwable t = ex; t != null; t = t.getCause()) {
            String msg = t.getMessage();
            if (msg == null) {
                continue;
            }
            String lower = msg.toLowerCase(Locale.ROOT);
            if (msg.contains("EL1008E")
                    || lower.contains("methodsecurityexpressionroot")
                    || lower.contains("cannot be found on object of type")
                    || lower.contains("failed to evaluate expression")) {
                return true;
            }
            String className = t.getClass().getName();
            if (className.contains("spel")
                    || className.contains("ExpressionException")
                    || className.contains("EvaluationException")) {
                return true;
            }
        }
        return false;
    }

    static String resolveUserMessage(Throwable ex) {
        if (isSecurityExpressionFailure(ex)) {
            return MSG_ERRO_EXPRESSAO_SEGURANCA;
        }
        return MSG_ACESSO_NEGADO;
    }

    static String resolveDetail(Throwable ex) {
        Throwable root = ExceptionUtils.getRootCause(ex);
        if (root != null && root.getMessage() != null && !root.getMessage().isBlank()) {
            return root.getMessage().trim();
        }
        return ex.getMessage();
    }
}
