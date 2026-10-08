package com.coordenapleito.domain.detach;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collection;

@Component
@RequiredArgsConstructor
public class DetachEntity {

    @PersistenceContext
    private final EntityManager entityManager;

    public void execute(Object entity) {
        DetachStrategy strategy = getDetachStrategy(entity);
        strategy.execute(entity);
    }

    private DetachStrategy getDetachStrategy(Object entity) {
        if (entity instanceof Collection<?>) {
            return new CollectionDetachStrategy(entityManager);
        } else if (isEntityWithAnnotations(entity.getClass())) {
            return new EntityDetachStrategy(entityManager);
        }
        throw new IllegalArgumentException("Unsupported entity type: " + entity.getClass());
    }

    private boolean isEntityWithAnnotations(Class<?> clazz) {
        return clazz.isAnnotationPresent(Entity.class);
    }
}