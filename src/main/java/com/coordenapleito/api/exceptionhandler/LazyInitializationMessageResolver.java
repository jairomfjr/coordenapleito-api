package com.coordenapleito.api.exceptionhandler;

import org.hibernate.LazyInitializationException;

import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Mensagens amigáveis para {@link LazyInitializationException} ({@code open-in-view=false}).
 */
final class LazyInitializationMessageResolver {

    private static final Pattern PROXY_ENTITY =
            Pattern.compile("could not initialize proxy \\[([^#]+)#");

    private static final Map<String, String> MENSAGENS_POR_ENTIDADE = Map.ofEntries(
            Map.entry("Organograma", "Não foi possível montar a resposta do organograma: o vínculo com o nó pai não foi carregado. "
                    + "Atualize a página e tente novamente; se persistir, contate o suporte."),
            Map.entry("Servico", "Não foi possível montar a resposta do serviço: o tipo de serviço não foi carregado. "
                    + "Atualize a página e tente novamente."),
            Map.entry("Usuario", "Não foi possível montar a resposta do usuário: vínculos (grupos, equipamentos ou coordenação) "
                    + "não foram carregados. Atualize a página e tente novamente."),
            Map.entry("Acolhimento", "Não foi possível montar a resposta do acolhimento: equipamento, cidadão ou tipo de acolhimento "
                    + "não foi carregado. Atualize a página e tente novamente."),
            Map.entry("Equipamento", "Não foi possível montar a resposta do equipamento: dados relacionados não foram carregados. "
                    + "Atualize a página e tente novamente."),
            Map.entry("Cidadao", "Não foi possível montar a resposta do cidadão: dados relacionados não foram carregados. "
                    + "Atualize a página e tente novamente."),
            Map.entry("TipoServico", "Não foi possível montar a resposta do tipo de serviço: a categoria não foi carregada. "
                    + "Atualize a página e tente novamente."),
            Map.entry("FuncionarioEquipamento", "Não foi possível montar a resposta do funcionário: dados de gênero, orientação ou etnia "
                    + "não foram carregados. Atualize a página e tente novamente.")
    );

    private LazyInitializationMessageResolver() {
    }

    static String resolve(LazyInitializationException ex) {
        String entity = extrairNomeEntidade(ex.getMessage());
        String especifica = MENSAGENS_POR_ENTIDADE.get(entity);
        if (especifica != null) {
            return especifica;
        }
        return "Não foi possível carregar dados relacionados ("
                + entity
                + ") para montar a resposta. Atualize a página e tente novamente; se persistir, contate o suporte.";
    }

    static String resolveFromThrowable(Throwable ex) {
        Throwable root = org.apache.commons.lang3.exception.ExceptionUtils.getRootCause(ex);
        if (root instanceof LazyInitializationException lazy) {
            return resolve(lazy);
        }
        String msg = root != null ? root.getMessage() : ex.getMessage();
        if (msg != null && msg.toLowerCase(Locale.ROOT).contains("could not initialize proxy")) {
            String entity = extrairNomeEntidade(msg);
            String especifica = MENSAGENS_POR_ENTIDADE.get(entity);
            if (especifica != null) {
                return especifica;
            }
            return "Não foi possível carregar dados relacionados ("
                    + entity
                    + ") para montar a resposta. Atualize a página e tente novamente.";
        }
        return null;
    }

    private static String extrairNomeEntidade(String message) {
        if (message == null) {
            return "entidade";
        }
        Matcher matcher = PROXY_ENTITY.matcher(message);
        if (matcher.find()) {
            String fqcn = matcher.group(1).trim();
            int dot = fqcn.lastIndexOf('.');
            return dot >= 0 ? fqcn.substring(dot + 1) : fqcn;
        }
        return "entidade";
    }
}
