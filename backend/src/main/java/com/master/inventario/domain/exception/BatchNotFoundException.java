package com.master.inventario.domain.exception;

public class BatchNotFoundException extends DomainException {

    public BatchNotFoundException(Long batchId) {
        super("Batch not found with id: " + batchId);
    }
}

