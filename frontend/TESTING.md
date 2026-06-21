# Guía de Testing - Frontend Angular 19

## Configuración

El proyecto usa:
- **Framework:** Jasmine 5.6
- **Test Runner:** Karma 6.4
- **Cobertura:** Karma Coverage 2.2
- **Navegador:** Chrome/ChromiumHeadless

## Comandos de Test

```bash
# Ejecutar todos los tests una sola vez
npm test

# Ejecutar tests en modo watch (se re-ejecutan al cambiar archivos)
npm test -- --watch

# Ejecutar tests con reporte de cobertura
npm test -- --code-coverage

# Ejecutar un test específico
npm test -- --include='**/product.service.spec.ts'

# Ejecutar en modo headless (CI/CD)
npm test -- --watch=false --browsers=ChromeHeadless
```

## Estructura de Tests

Cada archivo de implementación tiene su correspondiente `.spec.ts`:

```
src/app/
├── core/services/api.service.ts
├── core/services/api.service.spec.ts    ← Test
│
├── features/products/services/product.service.ts
├── features/products/services/product.service.spec.ts    ← Test
```

## Conceptos Fundamentales de Jasmine

### 1. Estructura Básica

```typescript
describe('MiServicio', () => {
  // Este bloque agrupa todos los tests de MiServicio

  let servicio: MiServicio;

  beforeEach(() => {
    // Se ejecuta ANTES de cada test
    // Aquí configuramos el entorno
    TestBed.configureTestingModule({
      providers: [MiServicio]
    });
    servicio = TestBed.inject(MiServicio);
  });

  it('debe crear el servicio', () => {
    // "it" define un test individual
    // Este test verifica que el servicio se crea correctamente
    expect(servicio).toBeTruthy();
  });
});
```

**Traducción:**
- `describe`: Describe el comportamiento de una clase/componente
- `beforeEach`: Configuración que se repite antes de cada `it`
- `it`: Un test individual que verifica una característica
- `expect`: Aserción que verifica que algo sea verdadero

### 2. Jasmine Matchers (Comparadores)

```typescript
expect(valor).toBe(5);                           // Igualdad estricta (===)
expect(valor).toEqual({ id: 1 });              // Comparación profunda
expect(valor).toBeTruthy();                     // Es verdadero
expect(valor).toBeFalsy();                      // Es falso
expect(valor).toContain('texto');               // Contiene
expect(array).toHaveLength(3);                  // Longitud del array
expect(funcion).toHaveBeenCalled();             // Fue llamada (spy)
expect(funcion).toHaveBeenCalledWith(arg);      // Fue llamada con este argumento
expect(() => { throw new Error(); }).toThrowError('mensaje');
```

### 3. Spies (Espías) - Mockear Funciones

Un "spy" es como un vigilante que observa si una función fue llamada:

```typescript
describe('ProductService', () => {
  let service: ProductService;
  let apiService: jasmine.SpyObj<ApiService>;

  beforeEach(() => {
    // Crear un mock de ApiService
    const apiSpy = jasmine.createSpyObj('ApiService', ['get', 'post']);
    
    TestBed.configureTestingModule({
      providers: [
        ProductService,
        { provide: ApiService, useValue: apiSpy }  // Usar el mock
      ]
    });
    
    service = TestBed.inject(ProductService);
    apiService = TestBed.inject(ApiService) as jasmine.SpyObj<ApiService>;
  });

  it('debe llamar a ApiService.get al obtener productos', () => {
    // Definir lo que retorna el spy
    apiService.get.and.returnValue(of([{ id: 1, name: 'Producto' }]));

    service.obtenerProductos();

    // Verificar que fue llamado
    expect(apiService.get).toHaveBeenCalledWith('/productos');
  });
});
```

## Patrón TDD en este Proyecto

**TDD = Test-Driven Development:**

1. **RED:** Escribir un test que falla (porque no existe la implementación)
2. **GREEN:** Escribir el código mínimo para que el test pase
3. **REFACTOR:** Mejorar el código si es necesario
4. **COMMIT:** Hacer commit cuando todo funciona

```
┌─────────────────────────────────────┐
│  1. Escribir TEST (falla)           │ RED 🔴
├─────────────────────────────────────┤
│  2. Escribir CÓDIGO (test pasa)     │ GREEN 🟢
├─────────────────────────────────────┤
│  3. REFACTORIZAR código             │ REFACTOR 🔵
├─────────────────────────────────────┤
│  4. COMMIT (git)                    │ ✅
└─────────────────────────────────────┘
```

## Ejemplo Completo: Testing de ApiService

### Paso 1: Escribir el Test (ROJO 🔴)

```typescript
// core/services/api.service.spec.ts
import { TestBed } from '@angular/core/testing';
import { HttpClientTestingModule, HttpTestingController } from '@angular/common/http/testing';
import { ApiService } from './api.service';

describe('ApiService', () => {
  let service: ApiService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [ApiService]
    });

    service = TestBed.inject(ApiService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();  // Verificar que no haya requests pendientes
  });

  it('debe crear el servicio', () => {
    expect(service).toBeTruthy();
  });

  it('debe obtener productos con GET', () => {
    const mockProducts = [
      { id: 1, nombre: 'Producto 1' },
      { id: 2, nombre: 'Producto 2' }
    ];

    service.obtener('/productos').subscribe(datos => {
      expect(datos).toEqual(mockProducts);
    });

    const req = httpMock.expectOne('http://localhost:8080/api/productos');
    expect(req.request.method).toBe('GET');
    req.flush(mockProducts);
  });

  it('debe manejar errores HTTP', () => {
    service.obtener('/productos').subscribe(
      () => fail('debería haber fallado'),
      error => {
        expect(error.status).toBe(500);
      }
    );

    const req = httpMock.expectOne('http://localhost:8080/api/productos');
    req.flush('Error de servidor', { status: 500, statusText: 'Error' });
  });
});
```

### Paso 2: Ejecutar el Test (Falla)

```bash
cd frontend
npm test -- --watch
```

Verás errores porque `ApiService` no existe aún.

### Paso 3: Implementar el Servicio (VERDE 🟢)

```typescript
// core/services/api.service.ts
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class ApiService {
  private urlBase = 'http://localhost:8080/api';

  constructor(private http: HttpClient) {}

  obtener<T>(ruta: string): Observable<T> {
    return this.http.get<T>(`${this.urlBase}${ruta}`);
  }

  crear<T>(ruta: string, datos: any): Observable<T> {
    return this.http.post<T>(`${this.urlBase}${ruta}`, datos);
  }

  actualizar<T>(ruta: string, datos: any): Observable<T> {
    return this.http.put<T>(`${this.urlBase}${ruta}`, datos);
  }

  eliminar<T>(ruta: string): Observable<T> {
    return this.http.delete<T>(`${this.urlBase}${ruta}`);
  }
}
```

### Paso 4: El Test Pasa (VERDE 🟢)

Ejecuta de nuevo:
```bash
npm test -- --watch
```

Ahora los tests pasan ✅

### Paso 5: REFACTORIZAR (si es necesario)

En este caso, el código es simple y está bien. No necesita refactorización.

### Paso 6: COMMIT

```bash
git add frontend/src/app/core/services/api.service.ts
git add frontend/src/app/core/services/api.service.spec.ts
git commit -m "feat: implementar ApiService con tests de HttpClient"
```

## Mejores Prácticas

1. **Nombres descriptivos:**
   ```typescript
   // ✅ Bueno
   it('debe obtener una lista de productos desde el backend', () => {
   
   // ❌ Malo
   it('test de productos', () => {
   ```

2. **AAA Pattern (Arrange, Act, Assert):**
   ```typescript
   it('debe crear un producto', () => {
     // ARRANGE: Preparar datos
     const producto = { nombre: 'Test', precio: 10 };
     
     // ACT: Ejecutar la acción
     service.crear(producto).subscribe(resultado => {
       // ASSERT: Verificar el resultado
       expect(resultado.id).toBeTruthy();
     });
   });
   ```

3. **One Assertion Per Test:**
   ```typescript
   // ✅ Mejor: Un assertion por test
   it('debe tener un nombre', () => {
     expect(service.nombre).toBe('Test');
   });
   
   it('debe tener un precio positivo', () => {
     expect(service.precio).toBeGreaterThan(0);
   });
   ```

4. **No testear el Framework:**
   ```typescript
   // ❌ No hacer esto (Angular ya lo testea)
   it('debe inyectar HttpClient', () => {
     expect(service.http).toBeDefined();
   });
   
   // ✅ Hacer esto (testear la lógica)
   it('debe hacer una petición GET al servidor', () => {
     service.obtener('/productos');
     expect(httpMock.expectOne).toHaveBeenCalled();
   });
   ```

## Debugging Tests

1. Abre `http://localhost:9876/debug.html` cuando ejecutes `npm test`
2. Abre DevTools (F12)
3. Puedes poner breakpoints directamente en los tests

## Coverage (Cobertura)

```bash
npm test -- --code-coverage
```

Genera un reporte en `coverage/` con estadísticas de cuánto código está siendo testeado.

**Objetivo:** 80%+ de cobertura

## Ejecutar un Test Específico

```bash
npm test -- --include='**/api.service.spec.ts'
npm test -- --include='**/product*.spec.ts'  # Todos los product*
```

## Más Información

- [Documentación de Jasmine](https://jasmine.github.io/)
- [Angular Testing Guide](https://angular.io/guide/testing)
- [HttpClientTestingModule](https://angular.io/api/common/http/testing/HttpClientTestingModule)
