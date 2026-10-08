package com.coordenapleito.api.disassembler;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Evita que o ModelMapper escreva em proxies lazy ({@code open-in-view=false}) quando o input
 * envia FKs planas ({@code tipoServicoId}) ou listas de UUID ({@code grupos}).
 */
final class JpaAssociationMappingSupport {

    private JpaAssociationMappingSupport() {
    }

    static void clearAssociationsBeforeInputMap(Object domainObject, Object input) {
        if (domainObject == null || input == null) {
            return;
        }
        // Evita hashCode/equals em proxies lazy durante a varredura recursiva.
        Set<Object> visited = java.util.Collections.newSetFromMap(new IdentityHashMap<>());
        clearAssociations(domainObject, input.getClass(), visited);
    }

    private static void clearAssociations(Object domainObject, Class<?> inputClass, Set<Object> visited) {
        if (domainObject == null || visited.contains(domainObject)) {
            return;
        }
        visited.add(domainObject);

        for (Field field : allDeclaredFields(domainObject.getClass())) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            if (!isJpaAssociation(field)) {
                continue;
            }
            if (shouldClearAssociation(field, inputClass)) {
                setFieldValue(domainObject, field, collectionReplacementValue(field));
            }
        }
    }

    private static boolean shouldClearAssociation(Field domainField, Class<?> inputClass) {
        String assocName = domainField.getName();
        if (hasDeclaredField(inputClass, assocName + "Id") || hasDeclaredField(inputClass, assocName + "Codigo")) {
            return true;
        }
        if (Collection.class.isAssignableFrom(domainField.getType())) {
            Field inputField = findDeclaredField(inputClass, assocName);
            if (inputField != null && isUuidCollectionField(inputField)) {
                return true;
            }
            Field codigosField = findDeclaredField(inputClass, assocName + "Codigos");
            return codigosField != null && isUuidCollectionField(codigosField);
        }
        return false;
    }

    private static boolean isJpaAssociation(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class)
                || field.isAnnotationPresent(OneToOne.class)
                || field.isAnnotationPresent(ManyToMany.class)
                || field.isAnnotationPresent(OneToMany.class)) {
            return true;
        }
        Class<?> type = field.getType();
        return type.isAnnotationPresent(Entity.class);
    }

    private static boolean isUuidCollectionField(Field inputField) {
        if (!Collection.class.isAssignableFrom(inputField.getType())) {
            return false;
        }
        Type generic = inputField.getGenericType();
        if (!(generic instanceof ParameterizedType parameterized)) {
            return false;
        }
        Type[] args = parameterized.getActualTypeArguments();
        if (args.length != 1) {
            return false;
        }
        return args[0] == UUID.class;
    }

    private static Object collectionReplacementValue(Field field) {
        if (!Collection.class.isAssignableFrom(field.getType())) {
            return null;
        }
        if (Set.class.isAssignableFrom(field.getType())) {
            return new HashSet<>();
        }
        return new ArrayList<>();
    }

    private static boolean hasDeclaredField(Class<?> type, String name) {
        return findDeclaredField(type, name) != null;
    }

    private static Field findDeclaredField(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // próxima superclasse
            }
        }
        return null;
    }

    private static List<Field> allDeclaredFields(Class<?> type) {
        List<Field> fields = new ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                fields.add(f);
            }
        }
        return fields;
    }

    private static void setFieldValue(Object target, Field field, Object value) {
        try {
            field.setAccessible(true);
            field.set(target, value);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Falha ao limpar associação JPA: " + field.getName(), e);
        }
    }
}
