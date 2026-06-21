import { ProductMapper } from './product.mapper';
import { ProductoDTO, LoteDTO, ErrorResponseDTO } from '../models/product.dto';
import { Producto, Lote, Error } from '../models/product.model';

describe('ProductMapper', () => {
  const mapper = ProductMapper;

  describe('mapProductoDtoToModel', () => {
    it('debe convertir ProductoDTO a Producto', () => {
      const productoDto: ProductoDTO = {
        id: 1,
        nombre: 'Laptop',
        descripcion: 'Laptop profesional',
        precio: 1500,
        cantidad: 10,
        sku: 'LAP-001',
        activo: true,
        lotes: [],
        fechaCreacion: '2026-06-21T10:30:00Z',
        fechaActualizacion: '2026-06-21T14:45:00Z'
      };

      const producto = mapper.mapProductoDtoToModel(productoDto);

      expect(producto.id).toBe(1);
      expect(producto.nombre).toBe('Laptop');
      expect(producto.precio).toBe(1500);
      expect(producto.cantidad).toBe(10);
      expect(producto.activo).toBe(true);
    });

    it('debe convertir fechas de string ISO a Date', () => {
      const productoDto: ProductoDTO = {
        id: 1,
        nombre: 'Test',
        descripcion: 'Test',
        precio: 100,
        cantidad: 5,
        sku: 'TEST-001',
        activo: true,
        lotes: [],
        fechaCreacion: '2026-06-21T10:30:00Z',
        fechaActualizacion: '2026-06-21T14:45:00Z'
      };

      const producto = mapper.mapProductoDtoToModel(productoDto);

      expect(producto.fechaCreacion).toBeInstanceOf(Date);
      expect(producto.fechaActualizacion).toBeInstanceOf(Date);
      expect(producto.fechaCreacion.toISOString()).toContain('2026-06-21');
    });

    it('debe mapear los lotes correctamente', () => {
      const loteDto: LoteDTO = {
        id: 1,
        numeroLote: 'LOTE-001',
        cantidad: 50,
        fechaFabricacion: '2026-01-01T00:00:00Z',
        fechaVencimiento: '2028-01-01T00:00:00Z',
        proveedor: 'Proveedor A',
        activo: true
      };

      const productoDto: ProductoDTO = {
        id: 1,
        nombre: 'Producto',
        descripcion: 'Test',
        precio: 100,
        cantidad: 50,
        sku: 'PROD-001',
        activo: true,
        lotes: [loteDto],
        fechaCreacion: '2026-06-21T10:30:00Z',
        fechaActualizacion: '2026-06-21T14:45:00Z'
      };

      const producto = mapper.mapProductoDtoToModel(productoDto);

      expect(producto.lotes.length).toBe(1);
      expect(producto.lotes[0].numeroLote).toBe('LOTE-001');
      expect(producto.lotes[0].cantidad).toBe(50);
    });

    it('debe manejar arrays vacíos de lotes', () => {
      const productoDto: ProductoDTO = {
        id: 1,
        nombre: 'Producto',
        descripcion: 'Test',
        precio: 100,
        cantidad: 0,
        sku: 'PROD-001',
        activo: true,
        lotes: [],
        fechaCreacion: '2026-06-21T10:30:00Z',
        fechaActualizacion: '2026-06-21T14:45:00Z'
      };

      const producto = mapper.mapProductoDtoToModel(productoDto);

      expect(producto.lotes.length).toBe(0);
      expect(Array.isArray(producto.lotes)).toBe(true);
    });
  });

  describe('mapLoteDtoToModel', () => {
    it('debe convertir LoteDTO a Lote', () => {
      const loteDto: LoteDTO = {
        id: 1,
        numeroLote: 'LOTE-001',
        cantidad: 100,
        fechaFabricacion: '2026-01-15T00:00:00Z',
        fechaVencimiento: '2028-01-15T00:00:00Z',
        proveedor: 'Proveedor Premium',
        activo: true
      };

      const lote = mapper.mapLoteDtoToModel(loteDto);

      expect(lote.id).toBe(1);
      expect(lote.numeroLote).toBe('LOTE-001');
      expect(lote.cantidad).toBe(100);
      expect(lote.proveedor).toBe('Proveedor Premium');
      expect(lote.activo).toBe(true);
    });

    it('debe convertir fechas de string ISO a Date', () => {
      const loteDto: LoteDTO = {
        id: 1,
        numeroLote: 'LOTE-001',
        cantidad: 50,
        fechaFabricacion: '2026-03-10T12:30:00Z',
        fechaVencimiento: '2027-03-10T12:30:00Z',
        proveedor: 'Test Provider',
        activo: true
      };

      const lote = mapper.mapLoteDtoToModel(loteDto);

      expect(lote.fechaFabricacion).toBeInstanceOf(Date);
      expect(lote.fechaVencimiento).toBeInstanceOf(Date);
    });

    it('debe detectar si un lote está vencido', () => {
      const hoy = new Date();
      const ayer = new Date(hoy.getTime() - 24 * 60 * 60 * 1000);
      const mañana = new Date(hoy.getTime() + 24 * 60 * 60 * 1000);

      const loteVencidoDto: LoteDTO = {
        id: 1,
        numeroLote: 'LOTE-VENCIDO',
        cantidad: 10,
        fechaFabricacion: ayer.toISOString(),
        fechaVencimiento: ayer.toISOString(),  // ← Vencido ayer
        proveedor: 'Provider',
        activo: true
      };

      const lote = mapper.mapLoteDtoToModel(loteVencidoDto);

      expect(lote.estaVencido).toBe(true);
    });

    it('debe detectar si un lote NO está vencido', () => {
      const hoy = new Date();
      const proximoAño = new Date(hoy.getFullYear() + 1, hoy.getMonth(), hoy.getDate());

      const loteVigenteDto: LoteDTO = {
        id: 1,
        numeroLote: 'LOTE-VIGENTE',
        cantidad: 50,
        fechaFabricacion: hoy.toISOString(),
        fechaVencimiento: proximoAño.toISOString(),  // ← Vence el próximo año
        proveedor: 'Provider',
        activo: true
      };

      const lote = mapper.mapLoteDtoToModel(loteVigenteDto);

      expect(lote.estaVencido).toBe(false);
    });
  });

  describe('mapErrorDtoToModel', () => {
    it('debe convertir ErrorResponseDTO a Error', () => {
      const errorDto: ErrorResponseDTO = {
        code: 'PRODUCTO_NO_ENCONTRADO',
        message: 'El producto con ID 999 no existe',
        timestamp: '2026-06-21T10:30:00Z',
        path: '/api/productos/999'
      };

      const error = mapper.mapErrorDtoToModel(errorDto);

      expect(error.code).toBe('PRODUCTO_NO_ENCONTRADO');
      expect(error.message).toBe('El producto con ID 999 no existe');
      expect(error.path).toBe('/api/productos/999');
    });

    it('debe convertir timestamp de string ISO a Date', () => {
      const errorDto: ErrorResponseDTO = {
        code: 'ERROR_TEST',
        message: 'Mensaje de error',
        timestamp: '2026-06-21T15:45:30Z',
        path: '/api/test'
      };

      const error = mapper.mapErrorDtoToModel(errorDto);

      expect(error.timestamp).toBeInstanceOf(Date);
      expect(error.timestamp.toISOString()).toContain('2026-06-21');
    });

    it('debe manejar códigos de error comunes', () => {
      const codigosComunes = [
        'PRODUCTO_NO_ENCONTRADO',
        'VALIDACION_ERROR',
        'ACCESO_DENEGADO',
        'ERROR_INTERNO'
      ];

      codigosComunes.forEach(codigo => {
        const errorDto: ErrorResponseDTO = {
          code: codigo,
          message: `Error: ${codigo}`,
          timestamp: '2026-06-21T10:30:00Z',
          path: '/api/test'
        };

        const error = mapper.mapErrorDtoToModel(errorDto);

        expect(error.code).toBe(codigo);
      });
    });
  });

  describe('Casos de uso completos', () => {
    it('debe mapear un producto completo con múltiples lotes', () => {
      const productoDtoCompleto: ProductoDTO = {
        id: 5,
        nombre: 'Monitor 27"',
        descripcion: 'Monitor profesional 4K',
        precio: 450,
        cantidad: 75,
        sku: 'MON-27-4K',
        activo: true,
        lotes: [
          {
            id: 101,
            numeroLote: 'LOTE-2024-01',
            cantidad: 25,
            fechaFabricacion: '2024-01-10T00:00:00Z',
            fechaVencimiento: '2029-01-10T00:00:00Z',
            proveedor: 'Samsung',
            activo: true
          },
          {
            id: 102,
            numeroLote: 'LOTE-2024-02',
            cantidad: 50,
            fechaFabricacion: '2024-03-15T00:00:00Z',
            fechaVencimiento: '2029-03-15T00:00:00Z',
            proveedor: 'LG',
            activo: true
          }
        ],
        fechaCreacion: '2024-01-10T08:00:00Z',
        fechaActualizacion: '2026-06-21T10:30:00Z'
      };

      const producto = mapper.mapProductoDtoToModel(productoDtoCompleto);

      expect(producto.id).toBe(5);
      expect(producto.nombre).toBe('Monitor 27"');
      expect(producto.lotes.length).toBe(2);
      expect(producto.lotes[0].proveedor).toBe('Samsung');
      expect(producto.lotes[1].proveedor).toBe('LG');
      expect(producto.fechaCreacion).toBeInstanceOf(Date);
    });
  });
});
