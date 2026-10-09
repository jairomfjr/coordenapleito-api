package com.coordenapleito.domain.spec;

import com.coordenapleito.domain.filter.LocalVotacaoFilter;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.infrastructure.util.PostgresTranslateBusca;
import com.coordenapleito.infrastructure.util.TextoBuscaUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class LocalVotacaoSpec {

    private static final char LIKE_ESCAPE = '\\';

    private LocalVotacaoSpec() {}

    public static Specification<LocalVotacao> usandoFiltro(LocalVotacaoFilter filtro) {
        return (root, query, builder) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (filtro == null) {
                return builder.conjunction();
            }

            if (filtro.getBusca() != null && !filtro.getBusca().isBlank()) {
                adicionarBuscaUnificada(builder, root, predicates, filtro.getBusca().trim());
            }
            if (filtro.getZona() != null) {
                predicates.add(builder.equal(root.get("zona"), filtro.getZona()));
            }
            if (filtro.getBairro() != null && !filtro.getBairro().isBlank()) {
                String pattern = "%" + TextoBuscaUtils.escapePadraoLike(
                        TextoBuscaUtils.normalizarParaBusca(filtro.getBairro())) + "%";
                predicates.add(builder.like(colunaDobrada(builder, root, "bairro"), pattern, LIKE_ESCAPE));
            }

            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static void adicionarBuscaUnificada(
            CriteriaBuilder builder,
            Root<LocalVotacao> root,
            List<Predicate> predicates,
            String raw) {
        List<Predicate> ou = new ArrayList<>();
        String needle = TextoBuscaUtils.normalizarParaBusca(raw);
        if (!needle.isEmpty()) {
            String pattern = "%" + TextoBuscaUtils.escapePadraoLike(needle) + "%";
            ou.add(builder.like(colunaDobrada(builder, root, "localVotacao"), pattern, LIKE_ESCAPE));
            ou.add(builder.like(colunaDobrada(builder, root, "endereco"), pattern, LIKE_ESCAPE));
            ou.add(builder.like(colunaDobrada(builder, root, "bairro"), pattern, LIKE_ESCAPE));
        }
        String somenteDigitos = raw.replaceAll("\\D", "");
        if (!somenteDigitos.isEmpty()) {
            try {
                ou.add(builder.equal(root.get("zona"), Integer.valueOf(somenteDigitos)));
            } catch (NumberFormatException ignored) {
                // termo numérico maior que Integer — ignora filtro por zona
            }
        }
        if (!ou.isEmpty()) {
            predicates.add(builder.or(ou.toArray(new Predicate[0])));
        }
    }

    private static Expression<String> colunaDobrada(
            CriteriaBuilder builder, Root<LocalVotacao> root, String atributo) {
        return builder.function(
                "translate",
                String.class,
                builder.lower(root.get(atributo)),
                builder.literal(PostgresTranslateBusca.FROM),
                builder.literal(PostgresTranslateBusca.TO));
    }
}
