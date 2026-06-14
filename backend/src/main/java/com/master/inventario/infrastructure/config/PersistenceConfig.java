package com.master.inventario.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Configuración de JPA y Repositorios.
 * Escanea las entidades y repositorios de infraestructura.
 */
@Configuration
@EntityScan(basePackages = "com.master.inventario.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.master.inventario.infrastructure.persistence.repository")
public class PersistenceConfig {
    // La configuración se hace mediante anotaciones en las entidades y repositorios
}

