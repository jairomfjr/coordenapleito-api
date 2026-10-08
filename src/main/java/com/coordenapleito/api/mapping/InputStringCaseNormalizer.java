package com.coordenapleito.api.mapping;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Normaliza campos textuais de DTOs para UPPERCASE antes do mapeamento para entidades.
 * <p>
 * Regra com exceções por nome de campo para evitar alterar dados sensíveis/técnicos:
 * email, senha/password, token/hash, identificador, url/link/uri, login.
 */
public final class InputStringCaseNormalizer {

    private static final Set<String> CAMPOS_PRESERVAR_CASE = Set.of(
            "email", "senha", "password", "token", "hash", "identificador", "url", "link", "uri", "login",
            "geometria"
    );

    private InputStringCaseNormalizer() {
    }

    public static void normalize(Object root) {
        if (root == null) {
            return;
        }
        Map<Object, Boolean> visited = new IdentityHashMap<>();
        normalizeRec(root, visited);
    }

    private static void normalizeRec(Object value, Map<Object, Boolean> visited) {
        if (value == null || visited.containsKey(value)) {
            return;
        }
        visited.put(value, Boolean.TRUE);

        if (value instanceof Collection<?> collection) {
            for (Object item : collection) {
                normalizeRec(item, visited);
            }
            return;
        }

        Class<?> type = value.getClass();
        if (isJavaType(type) || type.isEnum()) {
            return;
        }

        for (Field field : allFields(type)) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isFinal(field.getModifiers())) {
                continue;
            }
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(value);
                if (fieldValue == null) {
                    continue;
                }

                if (field.getType() == String.class) {
                    String s = (String) fieldValue;
                    if (deveUppercase(field.getName()) && !s.isBlank()) {
                        field.set(value, s.toUpperCase(Locale.ROOT));
                    }
                    continue;
                }

                if (fieldValue instanceof Collection<?> nestedCollection) {
                    for (Object nested : nestedCollection) {
                        normalizeRec(nested, visited);
                    }
                    continue;
                }

                normalizeRec(fieldValue, visited);
            } catch (IllegalAccessException | RuntimeException ignored) {
                // Melhor esforço: não bloquear request por reflexão em campo não acessível.
            }
        }
    }

    private static boolean deveUppercase(String fieldName) {
        if (fieldName == null || fieldName.isBlank()) {
            return false;
        }
        String name = fieldName.toLowerCase(Locale.ROOT);
        for (String token : CAMPOS_PRESERVAR_CASE) {
            if (name.contains(token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isJavaType(Class<?> type) {
        String name = type.getName();
        return name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jakarta.");
    }

    private static java.util.List<Field> allFields(Class<?> type) {
        java.util.List<Field> fields = new java.util.ArrayList<>();
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            for (Field f : c.getDeclaredFields()) {
                fields.add(f);
            }
        }
        return fields;
    }
}
