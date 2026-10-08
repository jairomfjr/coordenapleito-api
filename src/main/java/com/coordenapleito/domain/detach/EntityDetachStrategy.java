package com.coordenapleito.domain.detach;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

class EntityDetachStrategy implements DetachStrategy {
   @PersistenceContext
    private final EntityManager entityManager;

    public EntityDetachStrategy(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public void execute(Object entity) {
        entityManager.detach(entity);
    }
}