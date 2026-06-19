package com.master.inventario.infrastructure.persistence.adapter;

import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.persistence.repository.BatchJpaRepository;
import com.master.inventario.infrastructure.persistence.repository.ProductJpaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@SpringBootTest
class PersistenceAdapterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("inventario_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private ProductRepositoryAdapter productRepositoryAdapter;

    @Autowired
    private BatchRepositoryAdapter batchRepositoryAdapter;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BatchJpaRepository batchJpaRepository;

    @BeforeEach
    void setUp() {
        batchJpaRepository.deleteAllInBatch();
        productJpaRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("Should persist and paginate products using the real database")
    void shouldPersistAndPaginateProducts() {
        productRepositoryAdapter.save(new Product(null, "SKU-IT-1", "Milk", "Whole milk"));
        productRepositoryAdapter.save(new Product(null, "SKU-IT-2", "Bread", "Whole grain bread"));
        productRepositoryAdapter.save(new Product(null, "SKU-IT-3", "Eggs", "Free-range eggs"));

        var page = productRepositoryAdapter.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(3, page.getTotalElements());
        assertEquals("SKU-IT-1", page.getContent().get(0).getSku());
    }

    @Test
    @DisplayName("Should persist batch and read it back with the real database")
    void shouldPersistBatchAndReadItBack() {
        Product savedProduct = productRepositoryAdapter.save(new Product(null, "SKU-BATCH-1", "Milk", "Whole milk"));
        Batch savedBatch = batchRepositoryAdapter.save(new Batch(
                null,
                savedProduct,
                15,
                LocalDate.now().plusDays(30),
                LocalDate.now()
        ));

        assertNotNull(savedBatch.getId());
        assertEquals(15, savedBatch.getQuantity());
        assertEquals(savedProduct.getId(), savedBatch.getProduct().getId());

        var foundBatches = batchRepositoryAdapter.findByProductId(savedProduct.getId());
        var foundList = (List<Batch>) foundBatches;

        assertEquals(1, foundList.size());
        assertEquals(savedBatch.getId(), foundList.get(0).getId());
        assertEquals(15, foundList.get(0).getQuantity());
    }
}

