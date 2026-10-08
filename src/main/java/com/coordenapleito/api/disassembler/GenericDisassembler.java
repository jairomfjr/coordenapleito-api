package com.coordenapleito.api.disassembler;

import com.coordenapleito.api.mapping.InputStringCaseNormalizer;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

@Component
public class GenericDisassembler {

	@Autowired
	private ModelMapper modelMapper;

	@PersistenceContext
	private EntityManager entityManager;

	public <T, U> U toDomainObject(T entity, Class<U> modelClass) {
		return modelMapper.map(entity, modelClass);
	}

	public <T, U> void copyToDomainObject(T input, U domainObject) {
		JpaAssociationMappingSupport.clearAssociationsBeforeInputMap(domainObject, input);
		InputStringCaseNormalizer.normalize(input);
		// Usa identidade de objeto para evitar hashCode()/equals() em proxies lazy (ex.: Bairro$HibernateProxy),
		// que pode disparar LazyInitializationException fora da sessão.
		Set<Object> visited = Collections.newSetFromMap(new IdentityHashMap<>());
		detachEntities(domainObject, input, visited);
		modelMapper.map(input, domainObject);
	}

	private void detachEntities(Object domainObject, Object inputObject, Set<Object> visited) {
		if (domainObject == null || inputObject == null) {
			return;
		}

		if (visited.contains(domainObject)) {
			return;
		}
		visited.add(domainObject);

		for (Field field : domainObject.getClass().getDeclaredFields()) {
			// Verificar se o campo é final e não tentar modificar
			if (Modifier.isFinal(field.getModifiers())) {
				continue; // Pular campos finais
			}

			try {
				field.setAccessible(true); // Configure a acessibilidade
			} catch (Exception e) {
				// Java 9+: reflexão em tipos do JDK (ex.: InaccessibleObjectException para BigDecimal) pode falhar
				continue;
			}

			try {
				Object fieldValue = field.get(domainObject);
				Object inputFieldValue = getInputFieldValue(inputObject, field.getName());

				if (fieldValue != null && isEntityField(field)) {
					if (inputFieldValue != null && hasDifferentFields(fieldValue, inputFieldValue)) {
						entityManager.detach(fieldValue);
					}					
					detachEntities(fieldValue, inputFieldValue, visited);
				} else if (isCollectionField(field)) {
					detachCollection((Collection<?>) fieldValue, (Collection<?>) inputFieldValue, visited);
				} else if (fieldValue != null && isValidObjectField(field)) {
					detachEntities(fieldValue, inputFieldValue, visited);
				}
			} catch (IllegalAccessException e) {
				throw new RuntimeException("Failed to detach entity field: " + field.getName(), e);
			}
		}
	}

	private boolean hasDifferentId(Object entity, Object inputEntity) {
		try {
			Field idField = entity.getClass().getDeclaredField("id");
			idField.setAccessible(true);
			Long entityId = (Long) idField.get(entity);

			Field inputIdField = inputEntity.getClass().getDeclaredField("id");
			inputIdField.setAccessible(true);
			Long inputEntityId = (Long) inputIdField.get(inputEntity);

			return entityId != null && !entityId.equals(inputEntityId);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return false;
		}
	}

	private boolean hasDifferentFields(Object entity, Object inputEntity) {
		try {
			// Verifica se as classes são do mesmo tipo
			if (!entity.getClass().equals(inputEntity.getClass())) {
				return true; // Retorna que são diferentes se os tipos forem diferentes
			}

			// Itera pelos campos da classe
			for (Field field : entity.getClass().getDeclaredFields()) {
				field.setAccessible(true);

				// Ignora o campo "id"
				if (field.getName().equals("id")) {
					continue;
				}

				Object entityFieldValue = field.get(entity);
				Object inputEntityFieldValue = field.get(inputEntity);

				// Verifica se os valores dos campos são diferentes
				if (!Objects.equals(entityFieldValue, inputEntityFieldValue)) {
					return true;
				}
			}

			return false; // Retorna falso se todos os campos (exceto "id") forem iguais
		} catch (IllegalAccessException e) {
			return true; // Retorna que são diferentes em caso de exceção
		}
	}

	private Object getInputFieldValue(Object input, String fieldName) {
		try {
			Field inputField = input.getClass().getDeclaredField(fieldName);
			AccessibleObject.setAccessible(new AccessibleObject[] { inputField }, true);
			return inputField.get(input);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			return null;
		}
	}

	private boolean isEntityField(Field field) {
		return field.getType().isAnnotationPresent(Entity.class) && !field.getType().isEnum();
	}

	private boolean isValidObjectField(Field field) {
		Class<?> type = field.getType();
		String name = type.getName();
		// Não percorrer tipos do JDK (BigDecimal, etc.) — reflexão em java.math falha no Java 9+ modular
		if (name.startsWith("java.") || name.startsWith("javax.")) {
			return false;
		}
		return !type.isPrimitive()
				&& !String.class.isAssignableFrom(type)
				&& !type.isAnnotationPresent(Entity.class)
				&& !type.isEnum()
				&& !Collection.class.isAssignableFrom(type)
				&& !Long.class.isAssignableFrom(type);
	}

	private boolean isCollectionField(Field field) {
		return Collection.class.isAssignableFrom(field.getType());
	}

	private void detachCollection(Collection<?> collection, Collection<?> inputCollection, Set<Object> visited) {
		if (collection != null && inputCollection != null) {
			Iterator<?> inputIterator = inputCollection.iterator();
			for (Object item : collection) {
				if (inputIterator.hasNext()) {
					detachEntities(item, inputIterator.next(), visited);
				}
			}
		}
	}
}