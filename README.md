# spring-loadbalancer-registry

Standalone **Spring Boot starter library** + **demo REST API** that implements a small “instance registry + load balancer”:

- Register service instances by **unique address**
- Enforce **max 10** registered instances (configurable)
- Pick **next instance** using **Round-Robin**
- Persist data in **PostgreSQL** using **JPA**
- Manage schema with **Flyway**
- Full **integration tests** with **Testcontainers** (Postgres)

Repository contains two Gradle modules:

- `loadbalancer-lib` — the reusable library (Spring Boot auto-configuration)
- `loadbalancer-demo` — a runnable demo API that uses the library

---

## Tech stack

- Java 21
- Spring Boot 3.x
- Spring Data JPA (Hibernate)
- Flyway
- PostgreSQL
- Gradle (Groovy DSL)
- Lombok
- JUnit 5 + Testcontainers

---

## Project structure

```text
spring-loadbalancer-registry/
├─ loadbalancer-lib/                 # starter/library
│  ├─ src/main/java/...              # auto-config + domain + service + repo
│  ├─ src/main/resources/db/migration
│  └─ src/test/java/...              # Testcontainers-based integration tests
└─ loadbalancer-demo/                # demo REST API
   ├─ src/main/java/...              # controller + DTO + exception handler
   └─ src/test/java/...              # E2E test (optional)
```

---

## Library API (high level)

The library exposes a simple interface:

- `register(address)` – registers a new instance (address must be unique)
- `unregister(address)` – removes an instance by address
- `listAddresses()` – returns all registered addresses (stable order)
- `nextAddress()` – chooses the next address using the configured strategy (Round-Robin by default)

### Uniqueness and capacity
- Uniqueness is enforced **twice**:
  - application-level check
  - database unique constraint on `address`
- Capacity (`maxInstances`) is enforced transactionally.

### Configuration
In the demo (or any consumer app), you can configure:

```yaml
loadbalancer:
  max-instances: 10
```

---

## Demo REST API

Base path: `/api/lb`

### Endpoints

| Method | Path | Description |
|---|---|---|
| POST | `/api/lb/instances` | Register an instance |
| GET | `/api/lb/instances` | List all instances |
| GET | `/api/lb/instances/next` | Get next instance (round-robin) |
| DELETE | `/api/lb/instances?address=...` | Unregister by address |

> Note: `DELETE` uses `@RequestParam("address")` explicitly to avoid the Java `-parameters` requirement.

### Example requests

Register:
```bash
curl -X POST "http://localhost:8080/api/lb/instances" \
  -H "Content-Type: application/json" \
  -d '{ "address": "http://10.0.0.1:8080" }'
```

Next:
```bash
curl "http://localhost:8080/api/lb/instances/next"
```

List:
```bash
curl "http://localhost:8080/api/lb/instances"
```

Unregister:
```bash
curl -X DELETE "http://localhost:8080/api/lb/instances?address=http://10.0.0.1:8080"
```

---

## How to build and run

### Prerequisites
- Java 21 installed
- Docker Desktop / Docker Engine running (required for tests & local Postgres)
- Gradle Wrapper `./gradlew`

### 1) Start PostgreSQL locally (for the demo)

Use the repository `docker-compose.yml` (or your own Postgres). Ensure the demo’s `application.yml` points to the same host/port.

```bash
docker compose up -d
```

### 2) Build + run tests

```bash
./gradlew clean test
```

> Integration tests use **Testcontainers** and will start a Postgres container automatically.

### 3) Run the demo application

```bash
./gradlew :loadbalancer-demo:bootRun
```

Then open:
- `http://localhost:8080/api/lb/instances`

---

## Postman collection

A ready Postman collection is included in this repo:

- `spring-loadbalancer-registry.postman_collection.json`

Import it into Postman and set:
- `baseUrl` = `http://localhost:8080`

---

## Useful Spring feature used: `@Lookup`

This project includes an optional feature where the service can create a **fresh per-call “decision context”** via Spring’s `@Lookup` (method injection).
This is useful when:
- the service bean is **singleton**
- but you need a **new object per request/call** (prototype scope)

**Important:** `@Lookup` does **not** work reliably if the bean is created via `@Bean` factory method returning `new ...`.
The service must be created by the container (component/import), so Spring can generate the runtime subclass that overrides the lookup method.

---

## Troubleshooting

### Demo returns 400: “parameter name information not available”
Fix by explicitly naming the request param:

```java
@DeleteMapping("/instances")
public ResponseEntity<Void> unregister(@RequestParam("address") String address) { ... }
```

(or enable `-parameters` compiler flag, but explicit annotation is simpler).

### Tests don’t start Postgres container / DataSource replacement issues
For `@DataJpaTest` + Testcontainers ensure:

- `@AutoConfigureTestDatabase(replace = Replace.NONE)`
- `@DynamicPropertySource` is declared on the **test class or a superclass**, not only in an imported config.

### “Failed to determine a suitable driver class”
Ensure `org.postgresql:postgresql` is on the **test runtime classpath** of `loadbalancer-lib`.

---

## License

MIT (or choose your preferred license).
