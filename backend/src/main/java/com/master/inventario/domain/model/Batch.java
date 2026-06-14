package com.master.inventario.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Entidad de dominio: Lote (Batch).
 * Representa un lote de producto con validación de reglas de negocio.
 * Pure entity sin anotaciones de Spring/JPA.
 */
public class Batch {

    private Long id;
    private Product product;
    private int quantity;
    private LocalDate expiryDate;
    private LocalDate receivedDate;

    public Batch(Long id, Product product, int quantity, LocalDate expiryDate, LocalDate receivedDate) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.receivedDate = receivedDate;
    }

    /**
     * Valida que la cantidad sea mayor a cero.
     * @return true si es válida, false en caso contrario
     */
    public boolean isValidQuantity() {
        return quantity > 0;
    }

    /**
     * Valida que la fecha de caducidad no sea pasada respecto a hoy.
     * @return true si es válida, false si está caducada
     */
    public boolean isNotExpired() {
        return expiryDate.isAfter(LocalDate.now()) || expiryDate.isEqual(LocalDate.now());
    }

    /**
     * Valida todas las reglas de negocio del lote.
     * @return true si el lote es válido
     */
    public boolean isValid() {
        return isValidQuantity() && isNotExpired();
    }

    // Getters
    public Long getId() {
        return id;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public LocalDate getReceivedDate() {
        return receivedDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Batch batch = (Batch) o;
        return Objects.equals(id, batch.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Batch{" +
                "id=" + id +
                ", product=" + product.getSku() +
                ", quantity=" + quantity +
                ", expiryDate=" + expiryDate +
                '}';
    }
}

