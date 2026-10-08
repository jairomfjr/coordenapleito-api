package com.coordenapleito.api.assembler;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import org.hibernate.Hibernate;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Set;

/**
 * Materializa associações lazy já carregadas via {@code EntityGraph} / {@code JOIN FETCH}
 * antes do ModelMapper mapear entidades detached ({@code open-in-view=false}).
 */
final class DtoMappingLazyInit {

    private DtoMappingLazyInit() {
    }

    static void initializeForMapping(Object entity) {
        if (entity == null) {
            return;
        }
        // Evita hashCode/equals em proxies lazy (HibernateProxy) durante o tracking.
        Set<Object> visited = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        initializeDeep(entity, visited);
    }

    private static void initializeDeep(Object entity, Set<Object> visited) {
        if (entity == null || visited.contains(entity)) {
            return;
        }
        visited.add(entity);
        Hibernate.initialize(entity);

        for (Field field : allDeclaredFields(entity.getClass())) {
            if (Modifier.isStatic(field.getModifiers())
                    || Modifier.isFinal(field.getModifiers())
                    || field.isAnnotationPresent(Transient.class)) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object value = field.get(entity);
                if (value == null) {
                    continue;
                }
                if (value instanceof Collection<?> collection) {
                    if (!Hibernate.isInitialized(collection)) {
                        continue;
                    }
                    for (Object item : collection) {
                        initializeDeep(item, visited);
                    }
                } else if (isJpaAssociation(field, value)) {
                    if (!Hibernate.isInitialized(value)) {
                        try {
                            Hibernate.initialize(value);
                        } catch (Exception ignored) {
                            continue;
                        }
                    }
                    if (Hibernate.isInitialized(value)) {
                        initializeDeep(value, visited);
                    }
                }
            } catch (IllegalAccessException ignored) {
                // ignora campos inacessíveis
            }
        }
    }

    private static boolean isJpaAssociation(Field field, Object value) {
        if (field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(OneToMany.class)) {
            return true;
        }
        return value.getClass().isAnnotationPresent(Entity.class);
    }

    private static Iterable<Field> allDeclaredFields(Class<?> type) {
        var fields = new java.util.ArrayList<Field>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                fields.add(f);
            }
        }
        return fields;
    }
}
