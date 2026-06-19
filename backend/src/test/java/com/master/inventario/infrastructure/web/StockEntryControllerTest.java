package com.master.inventario.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.master.inventario.domain.exception.InvalidBatchQuantityException;
import com.master.inventario.domain.exception.ProductNotFoundException;
import com.master.inventario.domain.model.Batch;
import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.web.controller.StockEntryController;
import com.master.inventario.infrastructure.web.exception.GlobalExceptionHandler;
import com.master.inventario.infrastructure.web.mapper.StockEntryDtoMapper;
import com.master.inventario.usecase.GetProductByIdUseCase;
import com.master.inventario.usecase.RegisterStockEntryUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = StockEntryController.class)
@Import({StockEntryDtoMapper.class, GlobalExceptionHandler.class})
class StockEntryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RegisterStockEntryUseCase registerStockEntryUseCase;

    @MockBean
    private GetProductByIdUseCase getProductByIdUseCase;

    @Test
    @DisplayName("Should return 201 when stock entry is valid")
    void shouldReturnCreatedWhenStockEntryIsValid() throws Exception {
        Product product = new Product(1L, "SKU-API-1", "Milk", "Whole milk");
        Batch savedBatch = new Batch(10L, product, 20, LocalDate.now().plusDays(20), LocalDate.now());

        when(getProductByIdUseCase.execute(1L)).thenReturn(product);
        when(registerStockEntryUseCase.execute(any(Batch.class))).thenReturn(savedBatch);

        String requestJson = """
                {
                  "productId": 1,
                  "quantity": 20,
                  "expiryDate": "%s"
                }
                """.formatted(LocalDate.now().plusDays(20));

        mockMvc.perform(post("/api/stock-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(10))
                .andExpect(jsonPath("$.productId").value(1))
                .andExpect(jsonPath("$.quantity").value(20));
    }

    @Test
    @DisplayName("Should return 400 when use case raises domain validation error")
    void shouldReturnBadRequestWhenDomainValidationFails() throws Exception {
        Product product = new Product(1L, "SKU-API-1", "Milk", "Whole milk");

        when(getProductByIdUseCase.execute(1L)).thenReturn(product);
        when(registerStockEntryUseCase.execute(any(Batch.class)))
                .thenThrow(new InvalidBatchQuantityException("Quantity must be greater than zero"));

        String requestJson = """
                {
                  "productId": 1,
                  "quantity": 20,
                  "expiryDate": "%s"
                }
                """.formatted(LocalDate.now().plusDays(20));

        mockMvc.perform(post("/api/stock-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("DOMAIN_VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("Should return 400 when request body is invalid")
    void shouldReturnBadRequestWhenRequestValidationFails() throws Exception {
        String requestJson = """
                {
                  "productId": 1,
                  "quantity": 0,
                  "expiryDate": "%s"
                }
                """.formatted(LocalDate.now().plusDays(20));

        mockMvc.perform(post("/api/stock-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("REQUEST_VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("Should return 404 when product does not exist")
    void shouldReturnNotFoundWhenProductDoesNotExist() throws Exception {
        when(getProductByIdUseCase.execute(999L)).thenThrow(new ProductNotFoundException(999L));

        String requestJson = objectMapper.writeValueAsString(new java.util.HashMap<>() {{
            put("productId", 999);
            put("quantity", 20);
            put("expiryDate", LocalDate.now().plusDays(10).toString());
        }});

        mockMvc.perform(post("/api/stock-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return unified JSON error when product is missing")
    void shouldReturnUnifiedJsonErrorWhenProductIsMissing() throws Exception {
        when(getProductByIdUseCase.execute(999L)).thenThrow(new ProductNotFoundException(999L));

        String requestJson = """
                {
                  "productId": 999,
                  "quantity": 20,
                  "expiryDate": "%s"
                }
                """.formatted(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/stock-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.path").value("/api/stock-entries"));
    }

    // --- OWASP API6:2023 — receivedDate assigned by server, never from client ---

    @Test
    @DisplayName("OWASP API6: should ignore receivedDate sent by client and use server date")
    void shouldIgnoreClientReceivedDateAndUseServerDate() throws Exception {
        Product product = new Product(1L, "SKU-API-1", "Milk", "Whole milk");
        when(getProductByIdUseCase.execute(1L)).thenReturn(product);

        Batch savedBatch = new Batch(5L, product, 10, LocalDate.now().plusDays(10), LocalDate.now());
        when(registerStockEntryUseCase.execute(any())).thenReturn(savedBatch);

        ArgumentCaptor<Batch> batchCaptor = ArgumentCaptor.forClass(Batch.class);

        // Client attempts to send a backdated receivedDate — field is absent in DTO so it must be ignored
        String requestJson = """
                {
                  "productId": 1,
                  "quantity": 10,
                  "expiryDate": "%s",
                  "receivedDate": "2000-01-01"
                }
                """.formatted(LocalDate.now().plusDays(10));

        mockMvc.perform(post("/api/stock-entries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated());

        verify(registerStockEntryUseCase).execute(batchCaptor.capture());
        LocalDate capturedReceivedDate = batchCaptor.getValue().getReceivedDate();
        assertNotNull(capturedReceivedDate);
        assertNotEquals(LocalDate.of(2000, 1, 1), capturedReceivedDate,
                "Client-supplied receivedDate must be ignored");
        assertEquals(LocalDate.now(), capturedReceivedDate,
                "receivedDate must be today (assigned by server)");
    }
}

