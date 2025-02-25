package com.dataquadinc.model;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Component;

@Component
class EntityManagerFactoryListener {
    private static EntityManager entityManager;

    @PersistenceContext
    public void setEntityManager(EntityManager em) {
        EntityManagerFactoryListener.entityManager = em;
    }

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
            throw new IllegalStateException("EntityManager not initialized");
        }
        return entityManager;
    }
}