# Patient Service — Design Notes

## Purpose

This project is a hands-on health-domain Spring Boot service for managing patients. It is also a learning vehicle for backend architecture: persistence, events, caching, observability, containerization, and Kubernetes.

## System overview

```text
Client
  -> Spring Boot Patient Service
       -> PostgreSQL (patient records, Flyway history, outbox, processed events)
       -> Redis (patient read cache)
       -> Kafka (patient-created events and DLT)
```

The service is stateless. Patient data and event-processing state live in PostgreSQL, and cached reads live in Redis. This allows multiple service Pods to run safely once scheduled work is coordinated.

## Application layers

```text
REST Controller
  -> PatientService
      -> PatientRepository
          -> PostgreSQL
```

- **Controller**: HTTP routing, request validation, and response status codes.
- **Service**: business operations and transaction boundaries.
- **Repository**: persistence abstraction. The first implementation used an in-memory map; the current direction is Spring Data JPA with PostgreSQL.
- **DTOs**: Java records are used at the API boundary. Entities remain mutable classes for persistence.

## Persistence and migrations

- PostgreSQL stores `patients` and supporting event tables.
- Flyway owns schema changes through ordered migrations such as `V1__create_patients.sql`.
- Hibernate/JPA maps Java entities to the existing schema; Flyway, rather than Hibernate auto-DDL, creates and evolves tables and constraints.
- The patient email should have a database unique constraint. The application translates duplicate-key failures into a suitable client error, typically HTTP `409 Conflict`.

## Patient API

Typical operations are:

- `GET /patients` — list patients.
- `GET /patients/{id}` — fetch one patient, returning `404 Not Found` if absent.
- `POST /patients` — create a patient and return `201 Created`.
- `PUT /patients/{id}` — replace a patient representation.
- `PATCH /patients/{id}` — partially update a patient representation.

Bean Validation (`@Valid`, `@NotBlank`, `@Email`, and related constraints) rejects invalid request bodies. A `@RestControllerAdvice` centralizes exception-to-HTTP-response mapping.

## Eventing: transactional outbox

Creating a patient writes two PostgreSQL rows in one transaction:

```text
patients row
outbox_events row (status PENDING, payload JSON snapshot)
```

The outbox payload represents the patient data snapshot, not a Kafka-specific event envelope. A scheduled publisher finds pending outbox rows, sends their JSON payload to the `patient.created` Kafka topic, and marks them `PUBLISHED` only after Kafka acknowledges the send.

This provides **at-least-once delivery**. A crash after Kafka accepts a message but before the database status update can publish it again later.

Kafka headers carry metadata such as the outbox `eventId` and event type. Consumers use that event ID as a deduplication key in `processed_events`:

```text
Kafka consumer
  -> INSERT processed event id (unique)
      -> already present: skip duplicate
      -> newly inserted: perform business processing
```

The consumer work and processed-event insert belong in one database transaction, so a failure rolls back the deduplication record as well.

### Kafka failure handling

- Retry transient consumer failures with a fixed backoff.
- Send messages that exhaust retries to `patient.created.DLT`.
- Treat malformed JSON as non-retryable: it goes directly to the DLT because retrying cannot make invalid bytes valid.

## Caching

Redis caches individual patient reads with a TTL (currently intended as 10 minutes).

- `@Cacheable` on reads avoids a database query for cache hits.
- `@CachePut` refreshes a cache entry after a successful create or update.
- Updates/deletes must evict or replace the affected cache entry so callers do not see stale patient data.

Redis is a performance optimization, not the source of truth. PostgreSQL remains authoritative.

## Observability

Spring Boot Actuator exposes operational endpoints under `/actuator`:

- `/actuator/health`
- `/actuator/health/liveness`
- `/actuator/health/readiness`
- `/actuator/metrics`
- `/actuator/prometheus`
- `/actuator/info`

Prometheus can scrape the Prometheus endpoint later. Kubernetes uses readiness to decide whether traffic may reach a Pod and liveness to decide whether a stuck process should be restarted.

## Container image

The Dockerfile is a multi-stage build:

1. A JDK image builds the Spring Boot JAR.
2. A smaller JRE image runs only that JAR as a non-root user.

The application image is versioned, for example `patient-service:0.2.0`. Do not reuse a release tag: a new application or packaged-configuration version gets a new image tag.

## Local Kubernetes deployment

Colima runs a local k3s cluster using the Docker runtime. Docker-built images are therefore available to the local Kubernetes cluster without publishing to a registry.

```text
Docker build -> local Colima Docker image store -> Kubernetes Deployment -> Pods
```

The first Kubernetes phase deliberately runs only the application in Kubernetes. PostgreSQL, Kafka, and Redis remain local host services and are reached through `host.docker.internal`. Moving those stateful systems into Kubernetes is a later milestone.

### Kubernetes resources

| Resource | Responsibility |
| --- | --- |
| `Namespace` | Isolates the `patient` resources. |
| `ConfigMap` | Non-sensitive configuration: DB URL, Kafka broker, Redis host and port. |
| `Secret` | Sensitive values: `DB_USERNAME` and `DB_PASSWORD`. |
| `Deployment` | Keeps the patient-service Pods running and manages releases. |
| `Service` | Stable in-cluster address for the Pods. |
| `PodDisruptionBudget` | Keeps at least one ready Pod during voluntary node maintenance. |

The Deployment runs two replicas and uses a rolling-update strategy:

```yaml
replicas: 2
strategy:
  type: RollingUpdate
  rollingUpdate:
    maxUnavailable: 0
    maxSurge: 1
```

During a version update, Kubernetes starts one additional new Pod, waits for its readiness probe to pass, and only then terminates an old Pod. This avoids intentional application downtime, assuming sufficient cluster capacity and healthy dependencies.

The PDB complements this setting for voluntary infrastructure disruptions such as a node drain. It does not protect against crashes, node loss, forced deletion, or Deployment rollouts.

### Configuration flow

```text
ConfigMap -> non-secret environment variables -> Spring properties
Secret    -> DB_USERNAME / DB_PASSWORD          -> Spring datasource properties
```

The application reads the database credentials using placeholders in `application.yaml`; the actual values stay outside Git in `.env.local` and the Kubernetes Secret.

## Current limitations and next milestones

1. **Outbox multi-Pod safety**: two application replicas can run the scheduler. Add database row claiming/locking (for example `FOR UPDATE SKIP LOCKED`) so only one Pod publishes each pending event.
2. **Kubernetes-native dependencies**: deploy Redis and PostgreSQL with persistent storage; use a managed Kafka offering or a Kafka operator rather than treating Kafka as a simple stateless Pod.
3. **Ingress**: replace local `kubectl port-forward` with an Ingress and hostname-based routing.
4. **CI/CD**: build, test, version, push an immutable image to a registry, then update the Deployment through an automated pipeline.
5. **Security**: use a dedicated Kubernetes ServiceAccount with least-privilege RBAC only if the application needs to call the Kubernetes API.
6. **Production resilience**: run replicas across nodes/zones, size resource requests and limits, and add dashboards/alerts around latency, errors, Kafka lag, cache health, and outbox backlog.
