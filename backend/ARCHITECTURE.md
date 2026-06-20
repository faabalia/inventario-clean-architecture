# 📦 Arquitectura Completa del Proyecto - Clean Architecture

## Estructura de Capas y Flujo de Datos

Este diagrama ilustra cómo fluye una petición a través de las distintas capas, desde la API REST hasta la base de datos.

```
      CLIENTE (Navegador, App Móvil, etc.)
                   │
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                INFRASTRUCTURE - Capa Web (API REST)             │
│  • ProductController, StockEntryController                      │
│  • DTOs (CreateProductRequest, ProductResponse)                 │
│  • Web Mappers (ProductDtoMapper)                               │
└─────────────────────────────────────────────────────────────────┘
                   │ Pasa DTOs, Llama a Casos de Uso
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                    USE CASES (Lógica de Aplicación)             │
│  • CreateProductUseCase, RegisterStockEntryUseCase              │
│  • Orquesta la lógica de negocio.                               │
│  • Depende de INTERFACES de Repositorio (del Dominio).          │
└─────────────────────────────────────────────────────────────────┘
                   │ Pasa Objetos de Dominio, Llama a Repositorios
                   ▼
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN (Núcleo Puro del Negocio)             │
│  • Product, Batch (Entidades de dominio con reglas)             │
│  • ProductRepository, BatchRepository (Interfaces - Contratos)  │
│  • DomainExceptions (Lenguaje Ubicuo)                           │
└─────────────────────────────────────────────────────────────────┘
                   ▲
                   │ Implementa la Interfaz del Dominio
                   │
┌─────────────────────────────────────────────────────────────────┐
│            INFRASTRUCTURE - Capa de Persistencia                │
│                                                                 │
│  • Adapters: ProductRepositoryAdapter, BatchRepositoryAdapter   │
│  • Mappers de Persistencia: ProductMapper (Dominio ↔ Entidad)   │
│  • Repositorios JPA: ProductJpaRepository (Spring Data)         │
│  • Entidades JPA: ProductEntity, BatchEntity (@Entity)          │
└─────────────────────────────────────────────────────────────────┘
                   │ Persiste/Consulta
                   ▼
           ┌───────────────────┐
           │ PostgreSQL (BD)   │
           └───────────────────┘
```

## Ventajas de esta Arquitectura

✅ **Aislamiento del Dominio**: El núcleo del negocio no conoce la web, ni la base de datos, ni el framework. Es puro, reutilizable y fácil de probar.

✅ **Inversión de Dependencias**: Las flechas de dependencia apuntan hacia el centro (hacia el DOMAIN). La infraestructura depende del dominio, no al revés.

✅ **Mantenibilidad y Flexibilidad**:
   - Cambiar de `PostgreSQL` a `MongoDB` solo requiere cambiar la capa de persistencia. El dominio y los casos de uso no se tocan.
   - Exponer una API `GraphQL` en lugar de `REST` solo implicaría cambiar la capa web.

✅ **Testabilidad Superior**: Cada capa puede ser probada de forma independiente.
   - **Capa Web**: Pruebas con `MockMvc` para verificar controladores y DTOs.
   - **Casos de Uso**: Pruebas unitarias rápidas "mockeando" los repositorios.
   - **Capa de Persistencia**: Pruebas de integración con **Testcontainers**, que validan todo el stack de persistencia contra una base de datos real.

## Responsabilidades por Capa

| Componente | Responsabilidad |
|---|---|
| **Controller (Web)** | Recibir peticiones HTTP, validar DTOs, orquestar la llamada al caso de uso y devolver una respuesta HTTP. Cero lógica de negocio. |
| **DTO / Web Mapper** | Transferir datos hacia y desde la capa web. Traducir entre el mundo HTTP (JSON) y los objetos de los casos de uso. |
| **Use Case** | Orquestar los pasos para cumplir una funcionalidad. Contiene la lógica de la aplicación (ej: "para crear un producto, primero valida esto, luego llama al repositorio y finalmente notifica a otro sistema"). |
| **Domain Model** | Representar los conceptos centrales del negocio, sus reglas y su estado. Es el corazón de la aplicación. |
| **Repository Interface (Domain)** | Definir un contrato (`save`, `findById`, etc.) para la persistencia, sin conocer la implementación. |
| **Repository Adapter (Infra)** | Implementar la interfaz del repositorio del dominio, actuando como un puente. |
| **Persistence Mapper (Infra)** | Convertir objetos del dominio a entidades JPA (y viceversa). |
| **JPA Repository (Infra)** | Interfaz de Spring Data para el acceso técnico a la base de datos. |
| **JPA Entity (Infra)** | Mapeo 1:1 con una tabla de la base de datos. Anotada con `@Entity`. |

## Estado del Proyecto

1.  ✅ **Dominio Puro**: Implementado y validado.
2.  ✅ **Casos de Uso**: Implementados para las operaciones principales.
3.  ✅ **Capa de Persistencia**: Implementada con adaptadores, mappers y repositorios JPA.
4.  ✅ **Capa Web (API REST)**: Controladores funcionales para productos y stock.
5.  ✅ **Pruebas de Integración**: Robustas, utilizando Testcontainers para la capa de persistencia.
6.  ✅ **Manejo de Excepciones**: Estructura básica para manejar errores y devolver estados HTTP correctos.
