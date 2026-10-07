# Patient Service

A Spring Boot REST API for patient records.

## Technology

- Java 25
- Spring Boot
- Maven
- PostgreSQL, Kafka, and Redis

## API

- `POST /patients`
- `GET /patients`
- `GET /patients/{id}`

## Docker

The application image is built with a multi-stage Dockerfile and runs as a non-root user. Compose starts the API together with PostgreSQL, Kafka, and Redis for local development.

1. Copy `.env.example` to `.env` and set the database credentials to match your PostgreSQL instance.
2. Start the stack:

   ```powershell
   docker compose up --build
   ```

3. Open `http://localhost:8080/actuator/health`. Stop the stack with `Ctrl+C`; remove its containers with `docker compose down`. The PostgreSQL data volume is retained unless you also pass `--volumes`.

To build the image without Compose:

```powershell
docker build -t patient-service:dev .
```

The Compose file requires `DB_USERNAME` and `DB_PASSWORD`. `.env` is ignored by Git and excluded from the Docker build context.

## Kubernetes

The Kubernetes manifests include single-node development instances of PostgreSQL, Kafka, and Redis, as well as the API. They are intended for local development, not production; PostgreSQL uses a persistent volume and Redis data is ephemeral.

Run the deployment commands from the repository root in PowerShell:

```powershell
.\deploy-k8s.ps1
```

The script is a commented command checklist. Before running it, replace the `DB_PASSWORD` placeholder with a local development password and commit or stash all changes so the Git SHA identifies the exact source being built. It builds `patient-service:<full-git-sha>`, deploys that image to the currently selected `kubectl` context, creates or updates the database Secret, applies the Kubernetes manifests, and waits for each workload rollout. For a remote cluster, set `$imageRepository` in the script to the registry-qualified repository, log in to that registry, and uncomment `docker push $image` before deployment.

The GitHub Actions workflow also publishes each manually triggered build to ECR with the triggering commit SHA as its image tag. An optional version input can publish a second tag for the same image.

The final command keeps port-forwarding active in that terminal. Check `http://localhost:8081/actuator/health` in a browser; press `Ctrl+C` to stop forwarding. Delete the `patient` namespace to remove the deployment and its development dependencies; this also deletes the database Secret and its persistent volume claim.