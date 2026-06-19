package com.master.inventario.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Entidad JPA: ProductEntity
 * Mapea un Producto a la tabla 'products' en la base de datos.
 * Esta clase pertenece a la capa de infraestructura y NO debe ser usada en la lógica de dominio.
 */
@Entity
@Table(name = "products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(name = "min_stock", nullable = false, columnDefinition = "integer default 0")
    private Integer minStock;
}

