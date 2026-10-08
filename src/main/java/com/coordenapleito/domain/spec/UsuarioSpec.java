package com.coordenapleito.domain.spec;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import com.coordenapleito.domain.filter.UsuarioFilter;
import com.coordenapleito.domain.model.Usuario;
import com.coordenapleito.infrastructure.util.PostgresTranslateBusca;
import com.coordenapleito.infrastructure.util.TextoBuscaUtils;

import java.util.ArrayList;
import java.util.List;

public class UsuarioSpec {

    private static final char LIKE_ESCAPE = '\\';

    public static Specification<Usuario> usandoFiltro(UsuarioFilter filtro) {
        return (root, query, builder) -> {
            var predicates = new ArrayList<Predicate>();

            if (filtro.getBusca() != null && !filtro.getBusca().isBlank()) {
                boolean adicionou = adicionarPredicadosBuscaUnificada(builder, root, predicates, filtro.getBusca().trim());
                if (!adicionou) {
                    predicates.add(builder.disjunction());
                }
            } else {
                if (filtro.getNome() != null && !filtro.getNome().isBlank()) {
                    adicionarPredicadoNome(builder, root, predicates, filtro.getNome());
                }
                if (filtro.getCpf() != null && !filtro.getCpf().isBlank()) {
                    adicionarPredicadoCpfParcial(builder, root, predicates, filtro.getCpf());
                }
                if (filtro.getEmail() != null && !filtro.getEmail().isBlank()) {
                    adicionarPredicadoEmail(builder, root, predicates, filtro.getEmail());
                }
            }

            if (filtro.getAtivo() != null) {
                if (filtro.getAtivo()) {
                    predicates.add(builder.isTrue(root.get("ativo")));
                } else {
                    predicates.add(builder.isFalse(root.get("ativo")));
                }
            }

            if (predicates.isEmpty()) {
                return builder.conjunction();
            }
            return builder.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static boolean adicionarPredicadosBuscaUnificada(
            CriteriaBuilder builder,
            Root<Usuario> root,
            List<Predicate> predicates,
            String raw) {
        String somenteDigitos = extrairDigitos(raw);
        String textoSemDigitos = raw.replaceAll("\\d", "").trim().replaceAll("\\s+", " ");

        List<Predicate> ou = new ArrayList<>();

        if (!textoSemDigitos.isEmpty() && contemLetra(textoSemDigitos)) {
            String needle = TextoBuscaUtils.normalizarParaBusca(textoSemDigitos);
            if (!needle.isEmpty()) {
                String pattern = "%" + TextoBuscaUtils.escapePadraoLike(needle) + "%";
                ou.add(builder.like(colunaDobradaParaBusca(builder, root, "nome"), pattern, LIKE_ESCAPE));
                ou.add(builder.like(colunaDobradaParaBuscaContatoEmail(builder, root), pattern, LIKE_ESCAPE));
            }
        }
        if (!somenteDigitos.isEmpty()) {
            ou.add(builder.like(cpfSomenteDigitos(builder, root), "%" + somenteDigitos + "%"));
        }

        if (ou.isEmpty()) {
            return false;
        }
        if (ou.size() == 1) {
            predicates.add(ou.get(0));
        } else {
            predicates.add(builder.or(ou.toArray(new Predicate[0])));
        }
        return true;
    }

    private static String extrairDigitos(String raw) {
        if (raw == null || raw.isBlank()) {
            return "";
        }
        return raw.replaceAll("\\D", "");
    }

    private static void adicionarPredicadoCpfParcial(
            CriteriaBuilder builder,
            Root<Usuario> root,
            List<Predicate> predicates,
            String cpfParcial) {
        String somenteDigitos = extrairDigitos(cpfParcial);
        if (somenteDigitos.isEmpty()) {
            return;
        }
        predicates.add(builder.like(cpfSomenteDigitos(builder, root), "%" + somenteDigitos + "%"));
    }

    private static Expression<String> cpfSomenteDigitos(CriteriaBuilder builder, Root<Usuario> root) {
        return builder.function(
                "regexp_replace",
                String.class,
                root.get("cpf"),
                builder.literal("[^0-9]"),
                builder.literal(""),
                builder.literal("g"));
    }

    private static boolean contemLetra(String texto) {
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isLetter(texto.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    private static void adicionarPredicadoNome(
            CriteriaBuilder builder,
            Root<Usuario> root,
            List<Predicate> predicates,
            String nomeFiltro) {
        String needle = TextoBuscaUtils.normalizarParaBusca(nomeFiltro);
        if (needle.isEmpty()) {
            return;
        }
        String pattern = "%" + TextoBuscaUtils.escapePadraoLike(needle) + "%";
        predicates.add(builder.like(colunaDobradaParaBusca(builder, root, "nome"), pattern, LIKE_ESCAPE));
    }

    private static void adicionarPredicadoEmail(
            CriteriaBuilder builder,
            Root<Usuario> root,
            List<Predicate> predicates,
            String emailFiltro) {
        String needleEmail = TextoBuscaUtils.normalizarParaBusca(emailFiltro);
        if (needleEmail.isEmpty()) {
            return;
        }
        String patternEmail = "%" + TextoBuscaUtils.escapePadraoLike(needleEmail) + "%";
        predicates.add(builder.like(colunaDobradaParaBuscaContatoEmail(builder, root), patternEmail, LIKE_ESCAPE));
    }

    private static Expression<String> colunaDobradaParaBusca(
            CriteriaBuilder builder,
            Root<Usuario> root,
            String atributo) {
        return builder.function(
                "translate",
                String.class,
                builder.lower(root.get(atributo)),
                builder.literal(PostgresTranslateBusca.FROM),
                builder.literal(PostgresTranslateBusca.TO));
    }

    private static Expression<String> colunaDobradaParaBuscaContatoEmail(
            CriteriaBuilder builder,
            Root<Usuario> root) {
        var contato = root.join("contato", JoinType.LEFT);
        return builder.function(
                "translate",
                String.class,
                builder.lower(contato.get("email")),
                builder.literal(PostgresTranslateBusca.FROM),
                builder.literal(PostgresTranslateBusca.TO));
    }
}
