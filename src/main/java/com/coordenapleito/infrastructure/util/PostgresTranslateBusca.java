package com.coordenapleito.infrastructure.util;

/**
 * Mapas para {@code translate(lower(coluna), from, to)} no PostgreSQL, aproximando a remoção de
 * acentos feita em Java por {@link TextoBuscaUtils#normalizarParaBusca(String)} — sem exigir a
 * extensão {@code unaccent}.
 * <p>
 * Cada caractere em {@link #FROM} substitui-se pelo correspondente em {@link #TO} (mesmo comprimento).
 */
public final class PostgresTranslateBusca {

    /**
     * Caracteres acentuados (após {@code lower}) mapeados para ASCII; cobre português e latin estendido comum em nomes.
     */
    public static final String FROM;

    public static final String TO;

    static {
        String[][] pares = new String[][] {
                {"á", "a"}, {"à", "a"}, {"â", "a"}, {"ã", "a"}, {"ä", "a"}, {"å", "a"}, {"ā", "a"}, {"ă", "a"}, {"ą", "a"},
                {"é", "e"}, {"è", "e"}, {"ê", "e"}, {"ë", "e"}, {"ē", "e"}, {"ĕ", "e"}, {"ė", "e"}, {"ę", "e"}, {"ě", "e"},
                {"í", "i"}, {"ì", "i"}, {"î", "i"}, {"ï", "i"}, {"ĩ", "i"}, {"ī", "i"}, {"į", "i"}, {"ı", "i"},
                {"ó", "o"}, {"ò", "o"}, {"ô", "o"}, {"õ", "o"}, {"ö", "o"}, {"ø", "o"}, {"ō", "o"}, {"ŏ", "o"}, {"ő", "o"},
                {"ú", "u"}, {"ù", "u"}, {"û", "u"}, {"ü", "u"}, {"ů", "u"}, {"ū", "u"}, {"ŭ", "u"}, {"ű", "u"},
                {"ý", "y"}, {"ÿ", "y"},
                {"ç", "c"}, {"ć", "c"}, {"č", "c"},
                {"ñ", "n"}, {"ń", "n"}, {"ň", "n"},
                {"ß", "s"},
        };
        StringBuilder f = new StringBuilder();
        StringBuilder t = new StringBuilder();
        for (String[] p : pares) {
            f.append(p[0]);
            t.append(p[1]);
        }
        FROM = f.toString();
        TO = t.toString();
        if (FROM.length() != TO.length()) {
            throw new IllegalStateException("PostgresTranslateBusca: FROM e TO devem ter o mesmo tamanho");
        }
    }

    private PostgresTranslateBusca() {
    }
}
