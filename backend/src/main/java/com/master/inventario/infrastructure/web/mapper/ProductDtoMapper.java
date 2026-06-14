package com.master.inventario.infrastructure.web.mapper;

import com.master.inventario.domain.model.Product;
import com.master.inventario.infrastructure.web.dto.CreateProductRequest;
import com.master.inventario.infrastructure.web.dto.ProductResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductDtoMapper {

	public Product toDomain(CreateProductRequest request) {
		return new Product(null, request.sku(), request.name(), request.description());
	}

	public ProductResponse toResponse(Product product) {
		return new ProductResponse(product.getId(), product.getSku(), product.getName(), product.getDescription());
	}
}

