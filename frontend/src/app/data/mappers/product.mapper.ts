import { ProductoDTO, LoteDTO, ErrorResponseDTO } from '../models/product.dto';
import { Producto, Lote, Error } from '../models/product.model';

/**
 * Mapper para conversión de ProductoDTO → Producto
 * y otros DTOs relacionados.
 *
 * Responsabilidades:
 * - Convertir strings ISO a Date
 * - Mapear lotes
 * - Agregar lógica de negocio (ej: detectar vencimiento)
 */
export class ProductMapper {
  /**
   * Convierte ProductoDTO (del backend) a Producto (modelo del frontend).
   */
  static mapProductoDtoToModel(dto: ProductoDTO): Producto {
    return {
      id: dto.id,
      nombre: dto.nombre,
      descripcion: dto.descripcion,
      precio: dto.precio,
      cantidad: dto.cantidad,
      sku: dto.sku,
      activo: dto.activo,
      lotes: dto.lotes.map(loteDto => this.mapLoteDtoToModel(loteDto)),
      fechaCreacion: new Date(dto.fechaCreacion),
      fechaActualizacion: new Date(dto.fechaActualizacion)
    };
  }

  /**
   * Convierte LoteDTO a Lote.
   * Calcula si está vencido comparando fechaVencimiento con hoy.
   */
  static mapLoteDtoToModel(dto: LoteDTO): Lote {
    const fechaVencimiento = new Date(dto.fechaVencimiento);
    const hoy = new Date();

    return {
      id: dto.id,
      numeroLote: dto.numeroLote,
      cantidad: dto.cantidad,
      fechaFabricacion: new Date(dto.fechaFabricacion),
      fechaVencimiento: fechaVencimiento,
      proveedor: dto.proveedor,
      activo: dto.activo,
      estaVencido: fechaVencimiento < hoy  // ← Lógica de negocio
    };
  }

  /**
   * Convierte ErrorResponseDTO a Error.
   */
  static mapErrorDtoToModel(dto: ErrorResponseDTO): Error {
    return {
      code: dto.code,
      message: dto.message,
      timestamp: new Date(dto.timestamp),
      path: dto.path
    };
  }

  /**
   * Convierte un array de ProductoDTO a Producto[].
   */
  static mapProductosDtoToModels(dtos: ProductoDTO[]): Producto[] {
    return dtos.map(dto => this.mapProductoDtoToModel(dto));
  }
}
