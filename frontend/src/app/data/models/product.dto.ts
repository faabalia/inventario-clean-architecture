/**
 * DTO (Data Transfer Object) - Producto del Backend
 *
 * Representa la estructura exacta retornada por la API REST.
 * Mapea directamente con la respuesta JSON del backend (Spring Boot).
 */
export interface ProductoDTO {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  cantidad: number;
  sku: string;
  activo: boolean;
  lotes: LoteDTO[];
  fechaCreacion: string;      // ISO 8601 (ej: "2026-06-21T10:30:00Z")
  fechaActualizacion: string;
}

/**
 * DTO - Lote de un Producto
 *
 * Representa un lote/batch de stock de un producto.
 */
export interface LoteDTO {
  id: number;
  numeroLote: string;
  cantidad: number;
  fechaFabricacion: string;   // ISO 8601
  fechaVencimiento: string;   // ISO 8601
  proveedor: string;
  activo: boolean;
}

/**
 * DTO - Respuesta Paginada del Backend
 *
 * Envuelve cualquier lista con información de paginación.
 * Usado por endpoints como GET /api/productos?page=0&size=10
 */
export interface PaginatedResponseDTO<T> {
  content: T[];               // Elementos de la página actual
  totalElements: number;      // Total de elementos en la BD
  totalPages: number;         // Total de páginas
  currentPage: number;        // Página actual (0-based)
  pageSize: number;           // Elementos por página
  hasNext: boolean;           // Hay página siguiente
  hasPrevious: boolean;       // Hay página anterior
}

/**
 * DTO - Respuesta de Error del Backend
 *
 * Mapea la estructura de ErrorResponse del backend.
 * Todos los errores HTTP retornan este formato.
 */
export interface ErrorResponseDTO {
  code: string;               // Código de error (ej: "PRODUCT_NOT_FOUND")
  message: string;            // Mensaje legible (ej: "El producto no existe")
  timestamp: string;          // ISO 8601 del momento del error
  path: string;               // Ruta que causó el error (ej: "/api/productos/999")
}

/**
 * DTO - Solicitud de Creación de Producto
 *
 * Estructura para POST /api/productos
 */
export interface CreateProductoDTO {
  nombre: string;
  descripcion: string;
  precio: number;
  cantidad: number;
  sku: string;
}

/**
 * DTO - Solicitud de Actualización de Producto
 *
 * Estructura para PUT /api/productos/{id}
 */
export interface UpdateProductoDTO {
  nombre?: string;
  descripcion?: string;
  precio?: number;
  cantidad?: number;
  sku?: string;
  activo?: boolean;
}

/**
 * DTO - Solicitud de Entrada de Stock
 *
 * Estructura para POST /api/stock/entradas
 */
export interface StockEntryDTO {
  productoId: number;
  numeroLote: string;
  cantidad: number;
  fechaFabricacion: string;
  fechaVencimiento: string;
  proveedor: string;
}

/**
 * DTO - Respuesta de Entrada de Stock
 *
 * Retornado por POST /api/stock/entradas
 */
export interface StockEntryResponseDTO {
  id: number;
  productoId: number;
  loteId: number;
  cantidad: number;
  fechaEntrada: string;       // ISO 8601
  usuario: string;            // Usuario que hizo la entrada
}
