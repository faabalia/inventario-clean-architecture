package com.master.inventario.usecase;

import com.master.inventario.domain.exception.BatchNotFoundException;
import com.master.inventario.domain.repository.BatchRepository;

public class DeleteBatchUseCase {

    private final BatchRepository batchRepository;

    public DeleteBatchUseCase(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }

    public void execute(Long batchId) {
        if (batchRepository.findById(batchId).isEmpty()) {
            throw new BatchNotFoundException(batchId);
        }
        batchRepository.deleteById(batchId);
    }
}

