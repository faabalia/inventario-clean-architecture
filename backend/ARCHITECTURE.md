# 📦 Arquitectura de Persistencia - Clean Architecture

## Estructura de Capas

```
┌─────────────────────────────────────────────────────────────────┐
│                    DOMAIN (Núcleo Puro)                         │
│  • Product (entidad de dominio)                                 │
│  • Batch (entidad de dominio con validaciones)                  │
│  • ProductRepository (interfaz - contrato)                      │
│  • BatchRepository (interfaz - contrato)                        │
│  • DomainExceptions (lenguaje ubicuo)                           │
│  • RegisterStockEntryUseCase (orquestación)                     │
└─────────────────────────────────────────────────────────────────┘
                            ▲
                            │ Implementa
                            │
┌─────────────────────────────────────────────────────────────────┐
│              INFRASTRUCTURE (Detalle Técnico)                   │
│                                                                 │
│  Persistence Adapter Layer:                                     │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │ ProductRepositoryAdapter (Implements ProductRepository)   │  │
│  │ BatchRepositoryAdapter (Implements BatchRepository)       │  │
│  └───────────────────────────────────────────────────────────┘  │
│                            ▲                                    │
│                            │ Usa                                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │         Mapper Layer (Domain ↔ Infrastructure)            │  │
│  │ ProductMapper (Product ↔ ProductEntity)                   │  │
│  │ BatchMapper (Batch ↔ BatchEntity)                         │  │
│  └───────────────────────────────────────────────────────────┘  │
│                            ▲                                    │
│                            │ Convierte                          │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │         JPA Repository Layer (Spring Data)                │  │
│  │ ProductJpaRepository (JpaRepository<ProductEntity>)       │  │
│  │ BatchJpaRepository (JpaRepository<BatchEntity>)           │  │
│  └───────────────────────────────────────────────────────────┘  │
│                            ▲                                    │
│                            │ Usa                                │
│  ┌───────────────────────────────────────────────────────────┐  │
│  │            Entity Layer (Mapeado a BD)                    │  │
│  │ ProductEntity (→ tabla 'products')                        │  │
│  │ BatchEntity (→ tabla 'batches')                           │  │
│  └───────────────────────────────────────────────────────────┘  │
│                            ▲                                    │
│                            │ Persiste En                        │
│                            ▼                                    │
│                    PostgreSQL Database                          │
└─────────────────────────────────────────────────────────────────┘
```

## Flujo de Datos (Ejemplo: Guardar un Batch)

```
1. UseCase (RegisterStockEntryUseCase)
   └─> batch: Batch (objeto de dominio puro)
   
2. Adapter (BatchRepositoryAdapter)
   └─> Recibe batch (dominio)
   └─> Usa BatchMapper.toEntity(batch)
   
3. Mapper (BatchMapper)
   └─> Convierte Batch → BatchEntity
   └─> Convierte Product → ProductEntity
   
4. Spring Data (BatchJpaRepository)
   └─> save(batchEntity)
   
5. Database (PostgreSQL)
   └─> Persiste en tabla 'batches'
   
6. Response Path (Read)
   └─> BatchEntity (resultado BD)
   └─> BatchMapper.toDomain(entity)
   └─> Batch (objeto dominio puro)
```

## Ventajas de esta Arquitectura

✅ **Aislamiento del Dominio**
   - El dominio NO conoce de JPA, SQL, Spring Data
   - El dominio es testeable sin base de datos
   
✅ **Inversión de Dependencias**
   - El dominio define interfaces (ProductRepository, BatchRepository)
   - La infraestructura las implementa
   - Las dependencias apuntan HACIA el dominio
   
✅ **Mantenibilidad**
   - Cambiar de BD (PostgreSQL → MongoDB) = cambiar solo adaptadores
   - La lógica de negocio NO cambia
   
✅ **Testabilidad**
   - Test del dominio: mock del repositorio ✓
   - Test del adaptador: test con JPA ✓
   - Test de integración: test end-to-end ✓

## Responsabilidades por Capa

| Componente | Responsabilidad |
|-----------|-----------------|
| **Domain Model** | Validaciones de negocio, cálculos, reglas |
| **UseCase** | Orquestación de lógica de negocio |
| **Repository Adapter** | Traducir llamadas de dominio a infraestructura |
| **Mapper** | Convertir entre objetos de dominio y entidades JPA |
| **JPA Repository** | Acceso a datos técnico (Spring Data) |
| **Entity** | Mapeo a tabla de base de datos |

## Proximos Pasos

1. ✅ Dominio puro
2. ✅ Entidades JPA
3. ✅ Adaptadores + Mappers
4. ⏳ Controllers (REST API)
5. ⏳ Integration Tests
6. ⏳ Error Handling Global

