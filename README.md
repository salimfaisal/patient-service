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

The manifests in `k8s/` run the API with development PostgreSQL, Kafka, and Redis in the cluster. PostgreSQL uses a persistent volume claim; Redis data is ephemeral. This setup is for local development, not production.

### Build and deploy to a local Kubernetes cluster

The following instructions assume Docker Desktop Kubernetes, whose cluster can use images built by the local Docker Desktop engine. For kind, Minikube, or a remote cluster, load or push the image using that cluster's image workflow instead. These commands create Kubernetes resources on the selected cluster, but do not create AWS resources.

Run the commands from the repository root in PowerShell.

1. **Select and verify the local cluster.** Check the context before applying anything; do not continue if it points to a remote cluster.

   ```powershell
   kubectl config current-context
   kubectl get nodes
   kubectl get storageclass
   ```

   Confirm the context and node are your local cluster, and that a default StorageClass is available for PostgreSQL's volume claim.

2. **Build the image with the tag used by the Kubernetes Deployment.**

   ```powershell
   docker build -t patient-service:dev .
   ```

   The Deployment in `k8s/deployment.yaml` references `patient-service:dev` with `imagePullPolicy: IfNotPresent`.

3. **Create or update the database Secret from `.env`.** If needed, copy `.env.example` to `.env` and replace the example password with a local development password. `.env` is ignored by Git. The commands below load the values into the current PowerShell process and apply the Secret without putting the password in a manifest:

   ```powershell
   $localCredentials = Get-Content .env -Raw | ConvertFrom-StringData
   $env:DB_USERNAME = $localCredentials["DB_USERNAME"]
   $env:DB_PASSWORD = $localCredentials["DB_PASSWORD"]
   if (-not $env:DB_USERNAME -or -not $env:DB_PASSWORD) {
     throw "Set DB_USERNAME and DB_PASSWORD in .env before deploying."
   }

   kubectl apply -f k8s/namespace.yaml
   kubectl create secret generic patient-db --namespace patient `
     --from-literal="DB_USERNAME=$env:DB_USERNAME" `
     --from-literal="DB_PASSWORD=$env:DB_PASSWORD" `
     --dry-run=client -o yaml | kubectl apply -f -
   ```

   Use credentials that match the PostgreSQL instance. The secret is created in the `patient` namespace.

4. **Apply the application configuration, dependencies, and API.**

   ```powershell
   kubectl apply -f k8s/configmap.yaml
   kubectl apply -f k8s/dependencies.yaml
   kubectl apply -f k8s/deployment.yaml
   kubectl apply -f k8s/service.yaml -f k8s/pdb.yaml
   ```

5. **Wait for the dependencies and API to become ready.**

   ```powershell
   kubectl rollout status statefulset/postgres --namespace patient --timeout=240s
   kubectl rollout status deployment/kafka --namespace patient --timeout=240s
   kubectl rollout status deployment/redis --namespace patient --timeout=240s
   kubectl rollout status deployment/patient-service --namespace patient --timeout=240s
   kubectl get pods,pvc --namespace patient
   ```

   Confirm all pods are ready and the PostgreSQL claim is `Bound`. If the PVC remains pending, check that the local cluster has a default StorageClass.

6. **Forward the API to localhost and test it.** Keep this command running in its own terminal:

   ```powershell
   kubectl port-forward --address 127.0.0.1 service/patient-service 8081:80 --namespace patient
   ```

   Open `http://localhost:8081/actuator/health` or call the API at `http://localhost:8081/patients`. Press `Ctrl+C` in the port-forward terminal when finished.

7. **Remove the local deployment when no longer needed.**

   ```powershell
   kubectl delete namespace patient
   ```

   Check `kubectl get pv` and your cluster's storage behavior afterward: deleting the namespace removes the PVC, but whether the backing disk is also deleted depends on the StorageClass reclaim policy.

### Deployment script

Alternatively, `deploy-k8s.ps1` automates the local build, Secret creation from `.env`, manifest application, rollout checks, and port-forward. Run it from the repository root after reviewing its selected `kubectl` context. It refuses to run if the Git worktree has changes, because it tags the image with the current commit SHA; commit or stash changes first if you use the script.

### Publishing images to ECR

The GitHub Actions workflow publishes builds to ECR with the triggering commit SHA as the image tag. Pushing a version tag matching `v*` (for example, `v1.0.0`) also publishes the same image with that version tag. A manually triggered build can use the optional version input to add another tag.

To publish a release from the current commit:

```powershell
git tag v1.0.0
git push origin v1.0.0
```

The tag must point to a commit containing the updated workflow. The ECR workflow then publishes both the full commit SHA tag and `v1.0.0`.