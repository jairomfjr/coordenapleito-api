package com.coordenapleito.infrastructure.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Normalização de texto para buscas (listagens), alinhada a {@code translate(lower(...), ...)} no PostgreSQL
 * ({@link PostgresTranslateBusca}).
 */
public final class TextoBuscaUtils {

    private static final Pattern MARKS = Pattern.compile("\\p{M}+");

    private TextoBuscaUtils() {
    }

    /**
     * Minúsculas e sem marcas diacríticas (acentos), para comparar com {@code translate(lower(coluna), ...)}.
     * Usa NFC antes de NFD para reduzir diferenças entre composições Unicode equivalentes.
     */
    public static String normalizarParaBusca(String texto) {
        if (texto == null || texto.isBlank()) {
            return "";
        }
        String trimmed = texto.trim();
        String nfc = Normalizer.normalize(trimmed, Normalizer.Form.NFC);
        String nfd = Normalizer.normalize(nfc, Normalizer.Form.NFD);
        String semMarcas = MARKS.matcher(nfd).replaceAll("");
        return semMarcas.toLowerCase(Locale.ROOT);
    }

    /** Escapa {@code %}, {@code _} e {@code \} para uso em LIKE com escape {@code \\}. */
    public static String escapePadraoLike(String texto) {
        if (texto == null || texto.isEmpty()) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }
}
