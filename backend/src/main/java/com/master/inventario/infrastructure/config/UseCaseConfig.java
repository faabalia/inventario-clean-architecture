package com.master.inventario.infrastructure.config;

import com.master.inventario.usecase.RegisterStockEntryUseCase;
import com.master.inventario.domain.repository.BatchRepository;
import com.master.inventario.domain.repository.ProductRepository;
import com.master.inventario.usecase.CreateProductUseCase;
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

    // Próximos casos de uso irían aquí...
}

