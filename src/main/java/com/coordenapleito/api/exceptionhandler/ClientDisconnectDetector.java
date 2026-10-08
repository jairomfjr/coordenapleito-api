package com.coordenapleito.api.exceptionhandler;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.web.context.request.async.AsyncRequestNotUsableException;

import java.io.IOException;
import java.util.Locale;

/**
 * Identifica quando o cliente fechou a conexão antes da resposta terminar
 * (navegação, aba fechada, SSE desconectado). Não é erro de negócio nem de sistema.
 */
public final class ClientDisconnectDetector {

    private ClientDisconnectDetector() {
    }

    public static boolean isClientDisconnected(Throwable ex) {
        if (ex == null) {
            return false;
        }
        for (Throwable t : ExceptionUtils.getThrowableList(ex)) {
            if (t instanceof AsyncRequestNotUsableException) {
                return true;
            }
            String className = t.getClass().getName();
            if (className.endsWith("ClientAbortException")) {
                return true;
            }
            if (t instanceof IOException io && isBrokenPipeMessage(io.getMessage())) {
                return true;
            }
        }
        return false;
    }

    private static boolean isBrokenPipeMessage(String message) {
        if (message == null || message.isBlank()) {
            return false;
        }
        String lower = message.toLowerCase(Locale.ROOT);
        return lower.contains("broken pipe")
                || lower.contains("pipe quebrado")
                || lower.contains("connection reset")
                || lower.contains("conexão fechada");
    }
}
