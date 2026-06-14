package com.master.inventario.infrastructure.web;

import com.master.inventario.domain.exception.ProductValidationException;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.web.controller.ProductController;
import com.master.inventario.infrastructure.web.exception.GlobalExceptionHandler;
import com.master.inventario.infrastructure.web.mapper.ProductDtoMapper;
import com.master.inventario.usecase.CreateProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ProductController.class)
@Import({ProductDtoMapper.class, GlobalExceptionHandler.class})
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreateProductUseCase createProductUseCase;

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
}

