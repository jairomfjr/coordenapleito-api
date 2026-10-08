package com.coordenapleito.domain.detach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Collection;

public class CollectionDetachStrategy implements DetachStrategy {
    @PersistenceContext
    private final EntityManager entityManager;

    public CollectionDetachStrategy(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void execute(Object entity) {
        for (Object item : (Collection<?>) entity) {
            entityManager.detach(item);
        }
    }
}