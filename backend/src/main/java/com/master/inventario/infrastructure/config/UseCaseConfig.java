package com.master.inventario.infrastructure.config;

import com.master.inventario.usecase.RegisterStockEntryUseCase;
import com.master.inventario.domain.repository.BatchRepository;
import com.master.inventario.domain.repository.ProductRepository;
import com.master.inventario.usecase.CreateProductUseCase;
import com.master.inventario.usecase.DeleteBatchUseCase;
import com.master.inventario.usecase.GetProductByIdUseCase;
import com.master.inventario.usecase.ListProductStockEntriesUseCase;
import com.master.inventario.usecase.ListProductsUseCase;
import com.master.inventario.usecase.UpdateProductUseCase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de Use Cases e inyección de dependencias.
 * Aquí se wiran los casos de uso con los adaptadores de repositorio.
 */
@Configuration
public class UseCaseConfig {

    /**
     * Bean para RegisterStockEntryUseCase.
     * Spring inyectará el adaptador BatchRepository (que implementa la interfaz del dominio).
     */
    @Bean
    public RegisterStockEntryUseCase registerStockEntryUseCase(BatchRepository batchRepository) {
        return new RegisterStockEntryUseCase(batchRepository);
    }

    @Bean
    public CreateProductUseCase createProductUseCase(ProductRepository productRepository) {
        return new CreateProductUseCase(productRepository);
    }

    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepository productRepository) {
        return new UpdateProductUseCase(productRepository);
    }

    @Bean
    public ListProductsUseCase listProductsUseCase(ProductRepository productRepository) {
        return new ListProductsUseCase(productRepository);
    }

    @Bean
    public GetProductByIdUseCase getProductByIdUseCase(ProductRepository productRepository) {
        return new GetProductByIdUseCase(productRepository);
    }

    @Bean
    public ListProductStockEntriesUseCase listProductStockEntriesUseCase(ProductRepository productRepository, BatchRepository batchRepository) {
        return new ListProductStockEntriesUseCase(productRepository, batchRepository);
    }

    @Bean
    public DeleteBatchUseCase deleteBatchUseCase(BatchRepository batchRepository) {
        return new DeleteBatchUseCase(batchRepository);
    }

    // Próximos casos de uso irían aquí...
}

