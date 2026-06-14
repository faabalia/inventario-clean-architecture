package com.master.inventario.domain.exception;

/**
 * Excepción de dominio: Cantidad inválida.
 * Se lanza cuando el lote tiene una cantidad menor o igual a cero.
 */
public class InvalidBatchQuantityException extends DomainException {

    public InvalidBatchQuantityException(String message) {
        super(message);
    }

    public InvalidBatchQuantityException(String message, Throwable cause) {
        super(message, cause);
    }
}

