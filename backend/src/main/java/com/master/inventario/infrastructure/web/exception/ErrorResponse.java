package com.master.inventario.infrastructure.web.exception;

import java.time.Instant;

public record ErrorResponse(
        String code,
        String message,
        Instant timestamp,
        String path
) {
}

