# Mendel-java-challenge
# Transactions API (In-Memory)

API RESTful para almacenar transacciones en memoria y permitir:

- Obtener todos los IDs de transacciones por tipo
- Calcular la suma total de una transacción y todos sus descendientes (vinculados por `parent_id`)

---

## Stack

- Java 21
- Spring Boot 3.5.11
- Spring Web
- Spring Validation
- Spring Boot Actuator
- JUnit 5 + Spring Boot Test
- Docker

---

## Endpoints

### Crear o Actualizar Transacción

**PUT** `/transactions/{transaction_id}`

#### Request Body

```json
{
  "amount": 5000,
  "type": "cars",
  "parent_id": 10
}
```

- `amount` → obligatorio (double)
- `type` → obligatorio (string no vacío)
- `parent_id` → opcional (debe referenciar una transacción existente)

#### Response

```json
{
  "status": "ok"
}
```

#### Reglas de validación

- `amount` es obligatorio
- `type` no puede estar vacío
- `parent_id` debe existir si se envía
- No se permiten relaciones cíclicas entre transacciones

---

### Obtener IDs por Tipo

**GET** `/transactions/types/{type}`

#### Response

```json
[10, 11, 12]
```

Devuelve un arreglo JSON con todos los IDs asociados al tipo indicado.

---

### Obtener Suma de una Transacción

**GET** `/transactions/sum/{transaction_id}`

#### Response

```json
{
  "sum": 20000.0
}
```

La suma incluye:

- La transacción solicitada
- Todos sus hijos directos
- Todos los descendientes de manera recursiva

Si la transacción no existe → devuelve 404.

---

## Manejo de Errores

### 400 Bad Request

- Errores de validación
- JSON inválido
- `parent_id` inexistente
- Intento de crear una relación cíclica

### 404 Not Found

- Solicitud de suma para un ID inexistente

---

## Almacenamiento en Memoria

- No se utiliza base de datos (según lo requerido).
- Las transacciones se almacenan en memoria.
- Reiniciar la aplicación elimina todos los datos.

El repositorio mantiene índices internos para:

- Búsqueda por ID
- Búsqueda por tipo
- Relación padre → hijos

Las operaciones del repositorio están sincronizadas para garantizar atomicidad y consistencia.

---

## Health Check

Se utiliza Spring Boot Actuator:

GET /actuator/health

Respuesta esperada:

```json
{
  "status": "UP"
}
```

## Ejecutar Tests

Los tests de integración cubren:

- Casos exitosos
- Validaciones
- Relaciones padre-hijo
- Prevención de ciclos
- Actualización de tipo
- Actualización de parent
- JSON inválido

Ejecutar:

```bash
./mvnw test
```

---
## Docker

### Construir imagen

```bash
docker build -t transactions-api .
```

### Ejecutar contenedor

```bash
docker run -p 8080:8080 transactions-api
```

La API estará disponible en:

http://localhost:8080

---

## Notas

- Separación por capas (application, domain, infrastructure)
- Repositorio definido mediante interfaz (port)
- Implementación en memoria sincronizada (no SQL)
- Recorrido DFS (Depth-First Search) iterativo para el cálculo de suma
- Desarrollo guiado por TDD 
- Se priorizó claridad, corrección y mantenibilidad.
