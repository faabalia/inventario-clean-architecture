package com.master.inventario.infrastructure.web;

import com.master.inventario.domain.exception.ProductValidationException;
import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.web.controller.ProductController;
import com.master.inventario.infrastructure.web.exception.GlobalExceptionHandler;
import com.master.inventario.infrastructure.web.mapper.ProductDtoMapper;
import com.master.inventario.infrastructure.web.mapper.StockEntryDtoMapper;
import com.master.inventario.infrastructure.config.WebConfig;
import com.master.inventario.usecase.CreateProductUseCase;
import com.master.inventario.usecase.GetProductByIdUseCase;
import com.master.inventario.usecase.ListProductStockEntriesUseCase;
import com.master.inventario.usecase.ListProductsUseCase;
import com.master.inventario.usecase.UpdateProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import org.mockito.ArgumentCaptor;
import org.springframework.data.domain.Pageable;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({ProductDtoMapper.class, StockEntryDtoMapper.class, GlobalExceptionHandler.class, WebConfig.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateProductUseCase createProductUseCase;

    @MockitoBean
    private UpdateProductUseCase updateProductUseCase;

    @MockitoBean
    private ListProductsUseCase listProductsUseCase;

    @MockitoBean
    private GetProductByIdUseCase getProductByIdUseCase;

    @MockitoBean
    private ListProductStockEntriesUseCase listProductStockEntriesUseCase;

    @Test
    @DisplayName("Should return 201 when product payload is valid")
    void shouldReturnCreatedWhenProductPayloadIsValid() throws Exception {
        Product saved = new Product(1L, "SKU-1", "Milk", "Whole milk");
        when(createProductUseCase.execute(any(Product.class))).thenReturn(saved);

        String requestJson = """
                {
                  "sku": "SKU-1",
                  "name": "Milk",
                  "description": "Whole milk"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-1"))
                .andExpect(jsonPath("$.name").value("Milk"));
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid")
    void shouldReturnBadRequestWhenRequestBodyIsInvalid() throws Exception {
        String requestJson = """
                {
                  "sku": "",
                  "name": "Milk"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 400 when use case validation fails")
    void shouldReturnBadRequestWhenUseCaseValidationFails() throws Exception {
        when(createProductUseCase.execute(any(Product.class)))
                .thenThrow(new ProductValidationException("SKU is required"));

        String requestJson = """
                {
                  "sku": "SKU-1",
                  "name": "Milk"
                }
                """;

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return paginated products")
    void shouldReturnPaginatedProducts() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(
                new Product(1L, "SKU-1", "Milk", "Whole milk")
        ), PageRequest.of(0, 10), 1);
        when(listProductsUseCase.execute(any())).thenReturn(page);

        mockMvc.perform(get("/api/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].sku").value("SKU-1"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.size").value(10));
    }

    @Test
    @DisplayName("Should update product and return 200")
    void shouldUpdateProductAndReturnOk() throws Exception {
        Product updated = new Product(1L, "SKU-1", "Milk Updated", "Whole milk updated", 7);
        when(updateProductUseCase.execute(1L, "Milk Updated", "Whole milk updated", 7)).thenReturn(updated);

        String requestJson = """
                {
                  "name": "Milk Updated",
                  "description": "Whole milk updated",
                  "minStock": 7
                }
                """;

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Milk Updated"))
                .andExpect(jsonPath("$.minStock").value(7));
    }

    @Test
    @DisplayName("Should return 400 when update payload is invalid")
    void shouldReturnBadRequestWhenUpdatePayloadIsInvalid() throws Exception {
        String requestJson = """
                {
                  "name": "",
                  "description": "Whole milk updated",
                  "minStock": -1
                }
                """;

        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return product by id")
    void shouldReturnProductById() throws Exception {
        when(getProductByIdUseCase.execute(1L)).thenReturn(new Product(1L, "SKU-1", "Milk", "Whole milk"));

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.sku").value("SKU-1"));
    }

    @Test
    @DisplayName("Should return product stock entries")
    void shouldReturnProductStockEntries() throws Exception {
        Product product = new Product(1L, "SKU-1", "Milk", "Whole milk");
        when(listProductStockEntriesUseCase.execute(1L)).thenReturn(List.of(
                new Batch(10L, product, 5, LocalDate.now().plusDays(10), LocalDate.now())
        ));

        mockMvc.perform(get("/api/products/1/stock-entries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].quantity").value(5));
    }

    @Test
    @DisplayName("Should return 404 when product is missing in detail endpoint")
    void shouldReturnNotFoundWhenProductIsMissingInDetailEndpoint() throws Exception {
        when(getProductByIdUseCase.execute(99L)).thenThrow(new com.master.inventario.domain.exception.ProductNotFoundException(99L));

        mockMvc.perform(get("/api/products/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"));
    }

    @Test
    @DisplayName("Should allow CORS preflight from Angular local dev")
    void shouldAllowCorsPreflightFromAngularLocalDev() throws Exception {
        mockMvc.perform(options("/api/products")
                        .header("Origin", "http://localhost:4200")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").doesNotExist());
    }

    // --- OWASP API3:2023 — oversized input rejected at DTO level ---

    @Test
    @DisplayName("OWASP API3: should return 400 when sku exceeds max length")
    void shouldReturn400WhenSkuExceedsMaxLength() throws Exception {
        String oversizedSku = "A".repeat(51);
        String requestJson = """
                {
                  "sku": "%s",
                  "name": "Milk",
                  "description": "Whole milk"
                }
                """.formatted(oversizedSku);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("OWASP API3: should return 400 when description exceeds max length")
    void shouldReturn400WhenDescriptionExceedsMaxLength() throws Exception {
        String oversizedDesc = "X".repeat(501);
        String requestJson = """
                {
                  "sku": "SKU-1",
                  "name": "Milk",
                  "description": "%s"
                }
                """.formatted(oversizedDesc);

        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"));
    }

    // --- OWASP API4:2023 — page size capped at 100 ---

    @Test
    @DisplayName("OWASP API4: should cap page size to 100 when request exceeds max")
    void shouldCapPageSizeWhenRequestExceedsMax() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(), PageRequest.of(0, 100), 0);
        when(listProductsUseCase.execute(any())).thenReturn(page);

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);

        mockMvc.perform(get("/api/products?page=0&size=1000"))
                .andExpect(status().isOk());

        verify(listProductsUseCase).execute(pageableCaptor.capture());
        assertTrue(pageableCaptor.getValue().getPageSize() <= 100,
                "Page size must be capped at 100, was: " + pageableCaptor.getValue().getPageSize());
    }
}

