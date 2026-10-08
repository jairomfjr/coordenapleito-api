package com.coordenapleito.domain.repository;

import com.coordenapleito.domain.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Selection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UsuarioRepositoryImpl implements UsuarioRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    @Transactional(readOnly = true)
    public Page<Usuario> findAllComGruposPaginated(Specification<Usuario> spec, Pageable pageable) {
        Objects.requireNonNull(pageable, "pageable");
        Specification<Usuario> effectiveSpec = spec != null ? spec : (root, q, cb) -> cb.conjunction();

        CriteriaBuilder cb = em.getCriteriaBuilder();

        long total = countDistinct(effectiveSpec, cb);
        if (total == 0 || pageable.getPageSize() == 0) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Order.asc("nome").ignoreCase());

        List<Long> orderedIds = fetchPageIds(effectiveSpec, cb, sort, pageable);
        if (orderedIds.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, total);
        }

        List<Usuario> content = fetchUsuariosComGrupos(orderedIds);
        sortByIdOrder(content, orderedIds);

        return new PageImpl<>(content, pageable, total);
    }

    private long countDistinct(Specification<Usuario> spec, CriteriaBuilder cb) {
        CriteriaQuery<Long> cq = cb.createQuery(Long.class);
        Root<Usuario> root = cq.from(Usuario.class);
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }
        cq.select(cb.countDistinct(root.get("id")));
        return em.createQuery(cq).getSingleResult();
    }

    /** Tupla DISTINCT (id + colunas ordenadas); pagina só no SQL dos pais {@link Usuario}. */
    private List<Long> fetchPageIds(Specification<Usuario> spec, CriteriaBuilder cb, Sort sort, Pageable pageable) {
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Usuario> root = cq.from(Usuario.class);
        Predicate predicate = spec.toPredicate(root, cq, cb);
        if (predicate != null) {
            cq.where(predicate);
        }

        List<ProjectionOrder> proj = projectionsForUniqueSortColumns(root, cb, sort);
        if (proj.isEmpty()) {
            proj = List.of(defaultNomeProjection(root, cb));
        }

        List<Selection<?>> selections = new ArrayList<>();
        selections.add(root.get("id"));

        List<Order> orders = new ArrayList<>();
        LinkedHashSet<String> addedToTuple = new LinkedHashSet<>();

        addedToTuple.add("id");

        for (ProjectionOrder po : proj) {
            orders.add(po.order().isAscending() ? cb.asc(po.expr()) : cb.desc(po.expr()));

            if (addedToTuple.add(po.pathKey())) {
                selections.add(po.expr());
            }
        }

        cq.multiselect(selections.toArray(Selection<?>[]::new));
        cq.distinct(true);
        cq.orderBy(orders);

        TypedQuery<Object[]> q = em.createQuery(cq);
        q.setFirstResult((int) pageable.getOffset());
        q.setMaxResults(pageable.getPageSize());

        List<Long> ids = new ArrayList<>();
        for (Object[] row : q.getResultList()) {
            if (row[0] instanceof Long lid) {
                ids.add(lid);
            }
        }
        return ids;
    }

    private record ProjectionOrder(String pathKey, Expression<?> expr, Sort.Order order) {}

    private List<ProjectionOrder> projectionsForUniqueSortColumns(Root<?> root, CriteriaBuilder cb, Sort sort) {
        LinkedHashSet<String> seen = new LinkedHashSet<>();
        List<ProjectionOrder> list = new ArrayList<>();

        for (Sort.Order o : sort) {
            if (!seen.add(o.getProperty())) {
                continue;
            }
            Path<Object> path = navigate(root, o.getProperty());

            Expression<?> expr;
            boolean stringProp = path.getJavaType() != null && String.class.equals(path.getJavaType());
            if (stringProp && o.isIgnoreCase()) {
                Expression<String> s = path.as(String.class);
                expr = cb.lower(s);
            } else {
                expr = path;
            }
            list.add(new ProjectionOrder(o.getProperty(), expr, o));
        }
        return list;
    }

    private ProjectionOrder defaultNomeProjection(Root<?> root, CriteriaBuilder cb) {
        Path<Object> nome = navigate(root, "nome");
        Expression<?> expr =
                nome.getJavaType() != null && String.class.equals(nome.getJavaType())
                        ? cb.lower(nome.as(String.class))
                        : nome;
        return new ProjectionOrder("nome", expr, Sort.Order.asc("nome").ignoreCase());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private Path<Object> navigate(Root<?> root, String propertyPath) {
        String[] parts = propertyPath.split("\\.");
        Path<?> p = root.get(parts[0]);
        for (int i = 1; i < parts.length; i++) {
            p = p.get(parts[i]);
        }
        return (Path<Object>) p;
    }

    private List<Usuario> fetchUsuariosComGrupos(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return List.of();
        }
        TypedQuery<Usuario> q =
                em.createQuery(
                        """
                                SELECT DISTINCT u FROM Usuario u
                                LEFT JOIN FETCH u.grupos g
                                WHERE u.id IN :ids
                                """,
                        Usuario.class);
        q.setParameter("ids", ids);
        return q.getResultList();
    }

    private void sortByIdOrder(List<Usuario> usuarios, List<Long> orderedIds) {
        Map<Long, Integer> index = HashMap.newHashMap(orderedIds.size());
        for (int i = 0; i < orderedIds.size(); i++) {
            index.put(orderedIds.get(i), i);
        }
        usuarios.sort(Comparator.comparingInt(u -> index.getOrDefault(u.getId(), Integer.MAX_VALUE)));
    }
}
