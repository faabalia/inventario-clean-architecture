# Inventario - Backend

Backend de un sistema de gestión de inventario, desarrollado como parte del Trabajo de Fin de Máster (TFM) del Máster de Desarrollo con Inteligencia Artificial de **BIG School**.

La aplicación permite gestionar productos y sus lotes (*batches*) de stock, aplicando **Clean Architecture** para separar completamente la lógica de negocio de los detalles técnicos (base de datos, framework web, etc.).

## 📋 Tabla de contenidos

- [Descripción general](#-descripción-general)
- [Stack tecnológico](#-stack-tecnológico)
- [Instalación y ejecución](#-instalación-y-ejecución)
- [Estructura del proyecto](#-estructura-del-proyecto)
- [Funcionalidades principales](#-funcionalidades-principales)
- [Endpoints de la API](#-endpoints-de-la-api)
- [Tests](#-tests)
- [Seguridad](#-seguridad)

## 📖 Descripción general

Este backend expone una **API REST** para:

- Gestionar el catálogo de **productos** (crear, actualizar, listar paginado, consultar por id).
- Registrar **entradas de stock** (lotes/*batches*) asociadas a un producto, con validaciones de negocio (cantidad válida, fecha de caducidad no pasada).
- Consultar y eliminar lotes de stock.

El proyecto sigue los principios de **Clean Architecture**, separando:

- **Dominio**: entidades puras (`Product`, `Batch`), reglas de negocio y contratos de repositorio, sin ninguna dependencia de Spring o JPA.
- **Casos de uso**: orquestan la lógica de negocio (crear producto, registrar entrada de stock, etc.).
- **Infraestructura**: controladores REST, DTOs, entidades JPA, mappers y adaptadores que implementan los contratos del dominio.

Más detalle de la arquitectura en [`ARCHITECTURE.md`](./ARCHITECTURE.md) y ejemplos prácticos en [`EJEMPLOS_PRACTICOS.md`](./EJEMPLOS_PRACTICOS.md).

> Este proyecto no dispone de sistema de autenticación/login, por lo que no aplica usuario/contraseña de prueba.

## 🛠 Stack tecnológico

| Categoría | Tecnología |
|---|---|
| Lenguaje | Java 17 |
| Framework | Spring Boot 3.5.15 |
| Persistencia | Spring Data JPA / Hibernate |
| Base de datos | PostgreSQL 15 |
| Build tool | Maven (con Maven Wrapper) |
| Validación | Jakarta Validation (Bean Validation) |
| Reducción de boilerplate | Lombok |
| Tests | JUnit 5, Mockito, MockMvc |
| Tests de integración | Testcontainers (PostgreSQL) |
| Contenedores | Docker / Docker Compose |

## 🚀 Instalación y ejecución

### Requisitos previos

- [JDK 17](https://adoptium.net/) o superior
- [Docker](https://www.docker.com/) y Docker Compose
- (Opcional) Maven, aunque el proyecto incluye Maven Wrapper (`./mvnw`)

### 1. Clonar el repositorio

```bash
git clone <URL_DEL_REPOSITORIO>
cd <nombre_del_repositorio>/backend
```

### 2. Levantar la base de datos con Docker

Desde la raíz del proyecto (donde está el `docker-compose.yml`):

```bash
docker compose up -d
```

Esto levanta un contenedor de PostgreSQL con:

| Parámetro | Valor |
|---|---|
| Host | `localhost` |
| Puerto | `5432` |
| Base de datos | `inventario_db` |
| Usuario | `inventario_user` |
| Contraseña | `inventario_password` |

> Estas credenciales son únicamente de entorno de desarrollo local y están definidas en `docker-compose.yml` y `application.properties`.

### 3. Ejecutar la aplicación

Desde la carpeta `backend/`:

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

La API quedará disponible en:

```
http://localhost:8080
```

Hibernate creará/actualizará automáticamente las tablas (`ddl-auto=update`) en el primer arranque.

### 4. Ejecutar los tests

```bash
./mvnw test
```

Incluye tests unitarios (casos de uso, mappers, adaptadores) y tests de integración con **Testcontainers**, que levantan un contenedor PostgreSQL real de forma automática durante la ejecución (requiere Docker en ejecución).

## 📂 Estructura del proyecto

```
backend/
├── src/main/java/com/master/inventario/
│   ├── domain/                          # Núcleo de negocio (sin dependencias técnicas)
│   │   ├── model/                       # Product, Batch
│   │   ├── repository/                  # Interfaces (contratos) de repositorio
│   │   └── exception/                   # Excepciones de dominio
│   │
│   ├── usecase/                         # Casos de uso (orquestación de negocio)
│   │   ├── CreateProductUseCase.java
│   │   ├── UpdateProductUseCase.java
│   │   ├── ListProductsUseCase.java
│   │   ├── GetProductByIdUseCase.java
│   │   ├── ListProductStockEntriesUseCase.java
│   │   ├── RegisterStockEntryUseCase.java
│   │   └── DeleteBatchUseCase.java
│   │
│   └── infrastructure/                  # Detalles técnicos
│       ├── config/                      # Configuración Spring (JPA, CORS, casos de uso, seguridad)
│       ├── persistence/
│       │   ├── entity/                  # Entidades JPA (ProductEntity, BatchEntity)
│       │   ├── repository/              # Repositorios Spring Data JPA
│       │   ├── mapper/                  # Mappers dominio ↔ entidad JPA
│       │   └── adapter/                 # Adaptadores que implementan los repos del dominio
│       └── web/
│           ├── controller/              # Controladores REST
│           ├── dto/                     # DTOs de petición/respuesta
│           ├── mapper/                  # Mappers dominio ↔ DTO
│           └── exception/               # Manejo global de errores
│
├── src/test/java/...                    # Tests unitarios y de integración
├── src/main/resources/application.properties
├── pom.xml
├── ARCHITECTURE.md                       # Detalle de la arquitectura de persistencia
└── EJEMPLOS_PRACTICOS.md                 # Ejemplos prácticos de uso de la arquitectura

docker-compose.yml                        # PostgreSQL para entorno local
```

## ✨ Funcionalidades principales

### Gestión de productos
- Crear producto (`SKU`, nombre, descripción).
- Actualizar producto (nombre, descripción, stock mínimo).
- Listar productos con **paginación** (tamaño de página por defecto: 10, máximo: 100).
- Consultar un producto por ID.

### Gestión de stock (lotes)
- Registrar una entrada de stock (lote) para un producto existente, validando:
  - **Cantidad > 0**.
  - **Fecha de caducidad** no puede estar en el pasado.
  - La fecha de recepción la asigna siempre el servidor (no es manipulable por el cliente).
- Listar los lotes de stock de un producto.
- Eliminar un lote de stock por ID.

### Manejo de errores centralizado
Todas las excepciones (de dominio, de validación de petición, de integridad de datos, recursos no encontrados) se traducen a una respuesta JSON uniforme:

```json
{
  "code": "DOMAIN_VALIDATION_ERROR",
  "message": "La cantidad del lote debe ser mayor a cero. Cantidad recibida: 0",
  "timestamp": "2026-06-19T10:00:00Z",
  "path": "/api/stock-entries"
}
```

## 🔌 Endpoints de la API

| Método | Endpoint | Descripción |
|---|---|---|
| `POST` | `/api/products` | Crear un producto |
| `GET` | `/api/products` | Listar productos (paginado) |
| `GET` | `/api/products/{id}` | Obtener producto por ID |
| `PUT` | `/api/products/{id}` | Actualizar producto |
| `GET` | `/api/products/{id}/stock-entries` | Listar lotes de stock de un producto |
| `POST` | `/api/stock-entries` | Registrar una entrada de stock (lote) |
| `DELETE` | `/api/stock-entries/{id}` | Eliminar un lote de stock |

## ✅ Tests

El proyecto cuenta con una suite de tests que cubre:

- **Casos de uso** (lógica de negocio aislada, con mocks de repositorio).
- **Mappers y adaptadores de persistencia** (unitarios con mocks).
- **Tests de integración** con base de datos real mediante **Testcontainers**.
- **Controladores REST** con `@WebMvcTest` y `MockMvc`, incluyendo validación de payloads, CORS y cabeceras de seguridad.

```bash
./mvnw test
```

## 🔒 Seguridad

El backend implementa varias medidas alineadas con **OWASP API Security Top 10**:

- **Validación de tamaño de campos** (DTOs) para evitar payloads que violen restricciones de la base de datos.
- **Límite y capado del tamaño de página** (`size` máximo 100) para evitar consumo excesivo de recursos.
- **Asignación de `receivedDate` en servidor**, evitando que el cliente pueda manipular fechas de recepción de stock (mitigación de *mass assignment*).
- **CORS restringido** a los orígenes configurados (por defecto, `http://localhost:4200` para el frontend Angular).
- **Cabeceras de seguridad HTTP** (`X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy`, `Referrer-Policy`, `Permissions-Policy`).
- **Manejo uniforme de errores**, sin exponer detalles internos de la aplicación.

---

📌 *Proyecto desarrollado como Trabajo de Fin de Máster — Máster de Desarrollo con Inteligencia Artificial, BIG School.*
