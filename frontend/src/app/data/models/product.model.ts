/**
 * Modelo de Dominio - Producto
 *
 * Versión enriquecida del ProductoDTO.
 * Puede contener métodos, propiedades calculadas, etc.
 */
export interface Producto {
  id: number;
  nombre: string;
  descripcion: string;
  precio: number;
  cantidad: number;
  sku: string;
  activo: boolean;
  lotes: Lote[];
  fechaCreacion: Date;        // ← Convertido a Date (no string)
  fechaActualizacion: Date;
}

/**
 * Modelo de Dominio - Lote
 */
export interface Lote {
  id: number;
  numeroLote: string;
  cantidad: number;
  fechaFabricacion: Date;
  fechaVencimiento: Date;
  proveedor: string;
  activo: boolean;
  estaVencido?: boolean;      // ← Propiedad calculada
}

/**
 * Modelo de Error de Dominio
 */
export interface Error {
  code: string;
  message: string;
  timestamp: Date;
  path: string;
}
