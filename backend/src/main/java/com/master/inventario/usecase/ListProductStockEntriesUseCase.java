package com.master.inventario.usecase;

import com.master.inventario.domain.exception.ProductNotFoundException;
import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.repository.BatchRepository;
import com.master.inventario.domain.repository.ProductRepository;

import java.util.List;
import java.util.stream.StreamSupport;

public class ListProductStockEntriesUseCase {

    private final ProductRepository productRepository;
    private final BatchRepository batchRepository;

    public ListProductStockEntriesUseCase(ProductRepository productRepository, BatchRepository batchRepository) {
        this.productRepository = productRepository;
        this.batchRepository = batchRepository;
    }

    public List<Batch> execute(Long productId) {
        productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));

        return StreamSupport.stream(batchRepository.findByProductId(productId).spliterator(), false)
                .toList();
    }
}

