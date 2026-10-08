# Deployment issues and resolutions

This file records the issues encountered while setting up Docker Compose and deploying Patient Service to the local Docker Desktop Kubernetes cluster.

## Issues encountered

### Docker Engine was unavailable

Docker CLI commands initially failed because the Docker Engine named pipe was missing. Starting Docker Desktop made the engine available; the image could then be built and the Compose services started.

### Kubernetes context was initially missing or unavailable

The first deployment attempt had no Kubernetes context configured. Docker Desktop Kubernetes was enabled later, and `kubectl config use-context docker-desktop` provided a reachable cluster.

The cluster was subsequently unavailable again after Docker Desktop's context changed to `desktop-linux`, and its earlier namespace and workloads no longer existed. Re-enabling/selecting the `docker-desktop` Kubernetes context restored access. The namespace, Secret, dependencies, and API were recreated.

### Compose needed local service dependencies

The original Compose configuration started only the API and expected PostgreSQL, Kafka, and Redis to be running elsewhere. No local services were listening on their expected ports. Compose was expanded to start PostgreSQL, Kafka, and Redis with the API, including health checks and a persistent PostgreSQL volume.

### Kubernetes dependency addresses differed from Compose

Compose services can resolve each other by their Compose service names. Kubernetes workloads instead need Kubernetes Service DNS names. The application ConfigMap was changed to use `postgres`, `kafka`, and `redis`, and Kubernetes manifests were added for those development services.

### Kubernetes rejected the image's named non-root user

The image declared its runtime user as `spring`. Kubernetes could not verify that this named user was non-root with `runAsNonRoot: true`, so the API containers did not start. The Docker image now uses numeric UID/GID `10001`, which is also specified in the Deployment security context.

### Kafka did not become ready in Kubernetes

The initial Kafka readiness check timed out while its single-node KRaft broker was starting. The Kubernetes Kafka Service was updated to publish its not-yet-ready pod address and expose the controller port as well as the broker port. Startup and liveness checks use the broker TCP port, while readiness checks verify the broker. Kafka then rolled out successfully.

### PostgreSQL login did not match the application

The API initially failed during Flyway startup because PostgreSQL did not have the `patientapp` role used by the application. A `patientapp` login role was created in the running database using the password from the ignored local `.env`. It was granted access to the `patientdb` database, the `public` schema, existing tables and sequences, and future tables and sequences. The Kubernetes `patient-db` Secret was synchronized to those credentials. This preserved the PostgreSQL data volume.

### The first SHA-tagged API pod failed authentication

The first API pod using `patient-service:5879dcf7582974be86ba5dc2cacd8ace8d7ae7c6` entered `CrashLoopBackOff` because of the PostgreSQL role mismatch above. After creating the application role and syncing the Secret, the new ReplicaSet became healthy and both API replicas were ready.

### Port-forward disconnected during pod replacement

`kubectl port-forward` lost its pod connection during the rolling update. Port-forwarding targets a selected pod, so replacing that pod can end the session. Restarting the port-forward after the rollout restored access. The script keeps the port-forward as its final foreground command.

### Fixed development tags did not identify source revisions

The original deployment used the mutable `patient-service:dev` tag. The deployment workflow was changed to build `patient-service:<full-git-sha>` and apply that exact tag to the Kubernetes Deployment. A clean worktree is required so the commit SHA identifies the source used to build the image. The ECR workflow also publishes a commit-SHA tag.

### Deployment script credential source

The command checklist initially contained a placeholder password, which could differ from the password used to initialize PostgreSQL. It was updated to load `DB_USERNAME` and `DB_PASSWORD` from the ignored `.env` file, matching the local Compose credentials. The file must contain both values.

## Current deployment and verification

At the time this note was written, the `docker-desktop` cluster reported PostgreSQL, Kafka, Redis, and both Patient Service replicas ready. The Deployment used image `patient-service:5879dcf7582974be86ba5dc2cacd8ace8d7ae7c6`. After the credential correction, the health endpoint and `GET /patients` returned HTTP 200 through port-forwarding at `http://localhost:8081`.

The Kubernetes dependencies in [k8s/dependencies.yaml](./k8s/dependencies.yaml) are single-node development instances, not a production architecture. PostgreSQL uses a persistent volume; Redis data is ephemeral. Local `.env` credentials are ignored by Git and must not be committed.

## Redeployment notes

Use [deploy-k8s.ps1](./deploy-k8s.ps1) from the repository root after committing or stashing all changes. It builds the current commit SHA image, applies the manifests and database Secret, waits for the workloads, then runs the port-forward command. If the cluster or a pod is recreated, verify the selected `kubectl` context and start port-forwarding again.
