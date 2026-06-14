package com.master.inventario.infrastructure.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateProductRequest(
		@NotBlank(message = "sku is required")
		String sku,

		@NotBlank(message = "name is required")
		String name,

		String description
) {
}

