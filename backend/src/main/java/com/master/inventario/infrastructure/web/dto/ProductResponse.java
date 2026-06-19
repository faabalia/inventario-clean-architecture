package com.master.inventario.infrastructure.web.dto;

public record ProductResponse(
		Long id,
		String sku,
		String name,
		String description,
		Integer minStock
) {
}

