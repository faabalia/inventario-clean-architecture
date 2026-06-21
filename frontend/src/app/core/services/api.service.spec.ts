import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let servicio: ApiService;
  let httpMock: HttpTestingController;
  const urlBase = 'http://localhost:8080/api';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });

    servicio = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    // Verificar que no haya requests HTTP pendientes sin respuesta
    httpMock.verify();
  });

  describe('Creación del servicio', () => {
    it('debe crear el servicio', () => {
      expect(servicio).toBeTruthy();
    });
  });

  describe('Método GET - obtener()', () => {
    it('debe hacer una petición GET a la URL correcta', () => {
      const rutaPrueba = '/productos';

      servicio.obtener(rutaPrueba).subscribe();

      const peticion = httpMock.expectOne(`${urlBase}${rutaPrueba}`);
      expect(peticion.request.method).toBe('GET');
    });

    it('debe retornar los datos obtenidos del servidor', () => {
      const rutaPrueba = '/productos';
      const datosEsperados = [
        { id: 1, nombre: 'Producto A', precio: 100 },
        { id: 2, nombre: 'Producto B', precio: 200 }
      ];

      servicio.obtener(rutaPrueba).subscribe(datos => {
        expect(datos).toEqual(datosEsperados);
      });

      const peticion = httpMock.expectOne(`${urlBase}${rutaPrueba}`);
      peticion.flush(datosEsperados);
    });

    it('debe manejar errores HTTP en GET', () => {
      const rutaPrueba = '/productos';
      let errorRecibido = false;

      servicio.obtener(rutaPrueba).subscribe(
        () => fail('no debería haber éxito'),
        (error) => {
          errorRecibido = true;
          expect(error.status).toBe(500);
        }
      );

      const peticion = httpMock.expectOne(`${urlBase}${rutaPrueba}`);
      peticion.flush('Error del servidor', { status: 500, statusText: 'Error' });
      expect(errorRecibido).toBe(true);
    });
  });

  describe('Método POST - crear()', () => {
    it('debe hacer una petición POST con los datos correctos', () => {
      const ruta = '/productos';
      const datosCrear = { nombre: 'Nuevo Producto', precio: 150 };

      servicio.crear(ruta, datosCrear).subscribe();

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      expect(peticion.request.method).toBe('POST');
      expect(peticion.request.body).toEqual(datosCrear);
    });

    it('debe retornar el objeto creado', () => {
      const ruta = '/productos';
      const datosCrear = { nombre: 'Nuevo Producto', precio: 150 };
      const datosCreados = { id: 99, ...datosCrear };

      servicio.crear(ruta, datosCrear).subscribe(datos => {
        expect(datos).toEqual(datosCreados);
      });

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      peticion.flush(datosCreados);
    });

    it('debe manejar errores en POST', () => {
      const ruta = '/productos';
      const datosCrear = { nombre: 'Producto' };

      servicio.crear(ruta, datosCrear).subscribe(
        () => fail('no debería haber éxito'),
        (error) => {
          expect(error.status).toBe(400);
        }
      );

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      peticion.flush('Datos inválidos', { status: 400, statusText: 'Bad Request' });
    });
  });

  describe('Método PUT - actualizar()', () => {
    it('debe hacer una petición PUT con los datos correctos', () => {
      const ruta = '/productos/1';
      const datosActualizar = { nombre: 'Producto Actualizado', precio: 200 };

      servicio.actualizar(ruta, datosActualizar).subscribe();

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      expect(peticion.request.method).toBe('PUT');
      expect(peticion.request.body).toEqual(datosActualizar);
    });

    it('debe retornar el objeto actualizado', () => {
      const ruta = '/productos/1';
      const datosActualizar = { nombre: 'Actualizado', precio: 250 };
      const datosActualizados = { id: 1, ...datosActualizar };

      servicio.actualizar(ruta, datosActualizar).subscribe(datos => {
        expect(datos).toEqual(datosActualizados);
      });

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      peticion.flush(datosActualizados);
    });
  });

  describe('Método DELETE - eliminar()', () => {
    it('debe hacer una petición DELETE a la URL correcta', () => {
      const ruta = '/productos/1';

      servicio.eliminar(ruta).subscribe();

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      expect(peticion.request.method).toBe('DELETE');
    });

    it('debe manejar respuesta vacía en DELETE', () => {
      const ruta = '/productos/1';
      let respuestaRecibida = false;

      servicio.eliminar(ruta).subscribe(() => {
        respuestaRecibida = true;
      });

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      peticion.flush(null);
      expect(respuestaRecibida).toBe(true);
    });

    it('debe manejar errores en DELETE', () => {
      const ruta = '/productos/1';

      servicio.eliminar(ruta).subscribe(
        () => fail('no debería haber éxito'),
        (error) => {
          expect(error.status).toBe(404);
        }
      );

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      peticion.flush('No encontrado', { status: 404, statusText: 'Not Found' });
    });
  });

  describe('Genéricos de tipo', () => {
    it('debe preservar el tipo genérico en GET', () => {
      interface Producto {
        id: number;
        nombre: string;
      }

      const ruta = '/productos';
      const datos: Producto[] = [{ id: 1, nombre: 'Test' }];

      servicio.obtener<Producto[]>(ruta).subscribe(resultado => {
        expect(resultado[0].nombre).toBe('Test');
      });

      const peticion = httpMock.expectOne(`${urlBase}${ruta}`);
      peticion.flush(datos);
    });
  });
});
