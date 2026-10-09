package com.coordenapleito.domain.spec;

import com.coordenapleito.domain.filter.CoordenadorFilter;
import com.coordenapleito.domain.model.Coordenador;
import com.coordenapleito.domain.model.LocalVotacao;
import com.coordenapleito.infrastructure.util.PostgresTranslateBusca;
import com.coordenapleito.infrastructure.util.TextoBuscaUtils;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public final class CoordenadorSpec {

    private static final char LIKE_ESCAPE = '\\';

    private CoordenadorSpec() {}

    public static Specification<Coordenador> usandoFiltro(CoordenadorFilter filtro) {
        return (root, query, builder) -> {
            if (filtro == null || filtro.getBusca() == null || filtro.getBusca().isBlank()) {
                return builder.conjunction();
            }
            if (query != null) {
                query.distinct(true);
            }
            List<Predicate> ou = new ArrayList<>();
            String raw = filtro.getBusca().trim();
            String needle = TextoBuscaUtils.normalizarParaBusca(raw);
            Join<Coordenador, LocalVotacao> trabalho = root.join("localTrabalho", JoinType.LEFT);
            Join<Coordenador, LocalVotacao> votacao = root.join("localVotacao", JoinType.LEFT);
            if (!needle.isEmpty()) {
                String pattern = "%" + TextoBuscaUtils.escapePadraoLike(needle) + "%";
                ou.add(builder.like(colunaDobrada(builder, root, "nome"), pattern, LIKE_ESCAPE));
                ou.add(builder.like(colunaDobrada(builder, root, "email"), pattern, LIKE_ESCAPE));
                ou.add(builder.like(colunaDobrada(builder, trabalho, "localVotacao"), pattern, LIKE_ESCAPE));
                ou.add(builder.like(colunaDobrada(builder, votacao, "localVotacao"), pattern, LIKE_ESCAPE));
            }
            String digitos = raw.replaceAll("\\D", "");
            if (!digitos.isEmpty()) {
                String patternDigitos = "%" + TextoBuscaUtils.escapePadraoLike(digitos) + "%";
                ou.add(builder.like(root.get("cpf"), patternDigitos, LIKE_ESCAPE));
                ou.add(builder.like(root.get("telefone"), patternDigitos, LIKE_ESCAPE));
            }
            if (ou.isEmpty()) {
                return builder.conjunction();
            }
            return builder.or(ou.toArray(new Predicate[0]));
        };
    }

    private static Expression<String> colunaDobrada(
            CriteriaBuilder builder, Root<?> root, String atributo) {
        return builder.function(
                "translate",
                String.class,
                builder.lower(root.get(atributo)),
                builder.literal(PostgresTranslateBusca.FROM),
                builder.literal(PostgresTranslateBusca.TO));
    }

    private static Expression<String> colunaDobrada(
            CriteriaBuilder builder, Join<?, ?> join, String atributo) {
        return builder.function(
                "translate",
                String.class,
                builder.lower(join.get(atributo)),
                builder.literal(PostgresTranslateBusca.FROM),
                builder.literal(PostgresTranslateBusca.TO));
    }
}
