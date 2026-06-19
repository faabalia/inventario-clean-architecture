package com.master.inventario.domain.model;

import java.util.Objects;

/**
 * Entidad de dominio: Producto.
 * Representa un producto sin dependencias técnicas.
 * Pure entity sin anotaciones de Spring/JPA.
 */
public class Product {

    private Long id;
    private String sku;
    private String name;
    private String description;
    private int minStock;

    public Product(Long id, String sku, String name, String description) {
        this(id, sku, name, description, 0);
    }

    public Product(Long id, String sku, String name, String description, int minStock) {
        this.id = id;
        this.sku = sku;
        this.name = name;
        this.description = description;
        this.minStock = minStock;
    }

    public Long getId() {
        return id;
    }

    public String getSku() {
        return sku;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getMinStock() {
        return minStock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", name='" + name + '\'' +
                ", minStock=" + minStock +
                '}';
    }
}

