# Ejemplos Prácticos de Clean Architecture

## Ejemplo 1: Guardar un Batch (Flujo Completo)

```java
// Cliente (simulado)
public class ExemploGuardarBatch {
    public static void main(String[] args) {
        // Supongamos que Spring Boot inyecta el use case
        RegisterStockEntryUseCase useCase = ...;
        
        // Crear un producto (dominio puro)
        Product producto = new Product(
            1L,
            "SKU-001",
            "Leche Entera",
            "Leche fresca de vaca"
        );
        
        // Crear un batch (dominio puro)
        Batch batch = new Batch(
            null,  // Sin ID porque es nuevo
            producto,
            100,   // 100 unidades
            LocalDate.of(2026, 12, 31),  // Caduca el 31/12/2026
            LocalDate.now()
        );
        
        // Ejecutar el caso de uso
        try {
            Batch batchGuardado = useCase.execute(batch);
            System.out.println("✅ Batch guardado con ID: " + batchGuardado.getId());
        } catch (InvalidBatchQuantityException e) {
            System.err.println("❌ Cantidad inválida: " + e.getMessage());
        } catch (ExpiredBatchException e) {
            System.err.println("❌ Batch caducado: " + e.getMessage());
        }
    }
}
```

## Ejemplo 2: Test Unitario del Use Case (Sin BD)

```java
@Test
@DisplayName("Debería guardar un batch válido")
void shouldSaveValidBatch() {
    // ARRANGE: Preparar datos y mocks
    Product product = new Product(1L, "SKU-001", "Producto", "Desc");
    Batch validBatch = new Batch(
        null,
        product,
        50,
        LocalDate.now().plusDays(30),
        LocalDate.now()
    );
    
    // Mock del repositorio
    BatchRepository mockRepo = mock(BatchRepository.class);
    when(mockRepo.save(any(Batch.class)))
        .thenReturn(new Batch(1L, product, 50, LocalDate.now().plusDays(30), LocalDate.now()));
    
    RegisterStockEntryUseCase useCase = new RegisterStockEntryUseCase(mockRepo);
    
    // ACT: Ejecutar el caso de uso
    Batch result = useCase.execute(validBatch);
    
    // ASSERT: Verificar resultados
    assertNotNull(result.getId());
    assertEquals(50, result.getQuantity());
    verify(mockRepo, times(1)).save(any(Batch.class));
}
```

**Ventajas:**
- ✅ No necesita base de datos real
- ✅ Rápido (< 100ms)
- ✅ Completamente aislado
- ✅ Prueba la lógica de negocio pura

## Ejemplo 3: Cómo Spring Inyecta las Dependencias

```java
// Configuración en UseCaseConfig.java
@Configuration
public class UseCaseConfig {
    @Bean
    public RegisterStockEntryUseCase registerStockEntryUseCase(
        BatchRepository batchRepository  // ← Spring inyecta aquí el adaptador
    ) {
        return new RegisterStockEntryUseCase(batchRepository);
    }
}

// Lo que Spring hace internamente:
// 1. Ve que RegisterStockEntryUseCase necesita BatchRepository
// 2. Busca una implementación de BatchRepository (encuentra BatchRepositoryAdapter)
// 3. Inyecta BatchRepositoryAdapter en el useCase
// 4. El useCase NO sabe que es un adaptador, solo sabe que tiene un BatchRepository

// El resultado: Inversión de dependencias perfecta ✅
```

## Ejemplo 4: Cómo el Mapper Aísla las Capas

```java
// Este es el flujo que sucede internamente:

// 1. UseCase recibe Batch del dominio
Batch batchDominio = new Batch(...);

// 2. El adaptador recibe el batch
BatchRepositoryAdapter.save(batchDominio);

// 3. El mapper transforma dominio → JPA
BatchEntity batchEntity = batchMapper.toEntity(batchDominio);
// Internamente:
// - Convierte Product (dominio) → ProductEntity (JPA)
// - Convierte Batch (dominio) → BatchEntity (JPA)
// - IMPORTANTE: No pierde información, solo cambia la representación

// 4. Spring Data persiste
BatchEntity savedEntity = batchJpaRepository.save(batchEntity);

// 5. El mapper transforma JPA → dominio
Batch batchGuardado = batchMapper.toDomain(savedEntity);

// 6. Se retorna el batch del dominio al useCase
return batchGuardado;
```

## Ejemplo 5: Qué Sucede Cuando Hay un Error

```java
// ESCENARIO: Intentar guardar un batch caducado

Product product = new Product(1L, "SKU-001", "Producto", "Desc");
Batch batchCaducado = new Batch(
    null,
    product,
    100,
    LocalDate.of(2020, 1, 1),  // ← Caducado en 2020!
    LocalDate.now()
);

// El useCase lo detecta:
try {
    RegisterStockEntryUseCase useCase = new RegisterStockEntryUseCase(mockRepo);
    useCase.execute(batchCaducado);
} catch (ExpiredBatchException e) {
    // ✅ Se lanza la excepción del DOMINIO
    // ✅ El repositorio NUNCA es llamado
    // ✅ No hay I/O a la BD
    // ✅ La validación fue RÁPIDA (< 1ms)
    System.out.println("Error: " + e.getMessage());
    // Output: Error: El lote ya está caducado. Fecha de caducidad: 2020-01-01
}
```

## Ejemplo 6: Estructura de Paquetes Explicada

```
📦 com.master.inventario

├── 📁 domain                    ← AISLADO, TESTEABLE SIN BD
│   ├── 📁 model
│   │   ├── Product.java        ← Objeto puro Java
│   │   └── Batch.java          ← Con validaciones de negocio
│   │
│   ├── 📁 repository
│   │   ├── ProductRepository   ← "Contrato: alguien debe guardar productos"
│   │   └── BatchRepository     ← "Contrato: alguien debe guardar batches"
│   │                              (El dominio NO conoce HOW, solo WHAT)
│   │
│   └── 📁 exception
│       ├── InvalidBatchQuantityException
│       └── ExpiredBatchException
│
├── 📁 usecase                   ← ORQUESTACIÓN
│   └── RegisterStockEntryUseCase.java  ← "Pasos del caso de uso"
│
└── 📁 infrastructure            ← DETALLES TÉCNICOS
    ├── 📁 config
    │   ├── PersistenceConfig   ← "Configura JPA"
    │   └── UseCaseConfig       ← "Wire-up de dependencias"
    │
    └── 📁 persistence
        ├── 📁 entity
        │   ├── ProductEntity   ← "Mapeo a tabla SQL"
        │   └── BatchEntity     ← "@Entity, @Column, relaciones JPA"
        │
        ├── 📁 repository
        │   ├── ProductJpaRepository  ← "¿Cómo acceder? → JDBC/SQL"
        │   └── BatchJpaRepository    ← "Interface técnica de Spring Data"
        │
        ├── 📁 mapper
        │   ├── ProductMapper   ← "Product ↔ ProductEntity"
        │   └── BatchMapper     ← "Batch ↔ BatchEntity"
        │
        └── 📁 adapter
            ├── ProductRepositoryAdapter  ← "Implementa ProductRepository"
            └── BatchRepositoryAdapter    ← "Implementa BatchRepository"
```

## Ejemplo 7: Por Qué Necesitamos Mappers

### ❌ SIN Mapper (MALO):
```java
// Product (dominio) y ProductEntity (JPA) son la MISMA clase
@Entity
public class Product {
    @Id
    private Long id;
    
    @Column
    private String sku;
    
    @ManyToOne  // ← JPA en el dominio! ❌
    @JoinColumn(name = "category_id")
    private Category category;
}

// PROBLEMAS:
// 1. El dominio importa @Entity (acoplamiento a Hibernate)
// 2. Si cambias JPA → MongoDB, cambias el dominio ❌
// 3. No puedes testear el dominio sin JPA ❌
// 4. Las validaciones de JPA mezclan negocio con persistencia ❌
```

### ✅ CON Mapper (BUENO):
```java
// Dominio (puro, sin librerías técnicas)
public class Product {
    private Long id;
    private String sku;
    private String name;
    // Sin imports de Spring, JPA, etc.
}

// Infraestructura (detalles técnicos)
@Entity
@Table(name = "products")
public class ProductEntity {
    @Id
    @GeneratedValue
    private Long id;
    
    @Column(unique = true)
    private String sku;
    
    @ManyToOne
    @JoinColumn(name = "category_id")
    private CategoryEntity category;
}

// Mapper (traductor)
public class ProductMapper {
    public Product toDomain(ProductEntity entity) {
        return new Product(entity.getId(), entity.getSku(), ...);
    }
    
    public ProductEntity toEntity(Product domain) {
        return ProductEntity.builder()
            .id(domain.getId())
            .sku(domain.getSku())
            .build();
    }
}

// VENTAJAS:
// ✅ Dominio puro, independiente
// ✅ Fácil de testear
// ✅ Fácil de cambiar de BD
// ✅ Responsabilidades claras
```

## Ejemplo 8: Cómo Extender con Nuevos Casos de Uso

```java
// Ya tenemos RegisterStockEntryUseCase
// Queremos agregar: QueryProductBatchesUseCase

// PASO 1: Crear el use case (dominio)
public class QueryProductBatchesUseCase {
    private final BatchRepository batchRepository;
    
    public QueryProductBatchesUseCase(BatchRepository batchRepository) {
        this.batchRepository = batchRepository;
    }
    
    public List<Batch> execute(Long productId) {
        // Lógica: buscar todos los batches de un producto
        return (List<Batch>) batchRepository.findByProductId(productId);
    }
}

// PASO 2: Registrar el bean
@Configuration
public class UseCaseConfig {
    @Bean
    public RegisterStockEntryUseCase registerStockEntryUseCase(...) { }
    
    @Bean
    public QueryProductBatchesUseCase queryProductBatchesUseCase(
        BatchRepository batchRepository
    ) {
        return new QueryProductBatchesUseCase(batchRepository);
    }
}

// PASO 3: Los adaptadores y mappers se reutilizan ✅
// ¡No necesitas nuevas clases técnicas!
```

## Ejemplo 9: Validación en Dos Niveles

```java
// NIVEL 1: Validación de DOMINIO (siempre)
Batch batch = new Batch(null, product, -5, LocalDate.now(), LocalDate.now());
if (!batch.isValidQuantity()) {  // ← Lógica de negocio pura
    throw new InvalidBatchQuantityException("...");
}

// NIVEL 2: Validación de ENTRADA (en Controller, próximo)
@PostMapping
public ResponseEntity<BatchResponse> create(
    @Valid @RequestBody BatchCreateRequest request  // ← Jakarta Validation
) {
    // Spring automáticamente valida el DTO
    // Luego el caso de uso valida la lógica de negocio
}

// ¿Por qué dos niveles?
// Nivel 1 (dominio): Protege la integridad de la lógica de negocio
// Nivel 2 (controller): Rechaza basura antes de llegar a la lógica
```

---

## 🎯 Resumen de Patrones Usados

| Patrón | Dónde | Por Qué |
|--------|-------|--------|
| **Domain-Driven Design** | Domain layer | Lenguaje ubicuo, validaciones de negocio |
| **Repository Pattern** | domain/repository | Abstraer la persistencia |
| **Adapter Pattern** | infrastructure/adapter | Implementar interfaces del dominio |
| **Mapper Pattern** | infrastructure/mapper | Traducir entre capas |
| **Dependency Injection** | config/ | Desacoplamiento, testabilidad |
| **Value Objects** | domain/model | Encapsular validaciones |
| **Use Case Pattern** | usecase/ | Orquestar flujos de negocio |


