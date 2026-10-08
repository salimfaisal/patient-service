# Do not tag uncommitted source with a commit SHA that cannot identify it.
if (git status --porcelain) {
  throw "Commit or stash all changes before deploying a SHA-tagged image."
}

# Use the full commit SHA so each built image has an immutable, traceable tag.
$imageTag = (git rev-parse HEAD).Trim()
if ($LASTEXITCODE -ne 0) {
  throw "Could not determine the current Git commit SHA."
}
# Set this to a registry-qualified repository when deploying to a remote cluster.
$imageRepository = "patient-service"
$image = "${imageRepository}:$imageTag"

# Check which Kubernetes cluster kubectl will deploy to.
kubectl config current-context

# Confirm the selected cluster is reachable.
kubectl get nodes

# Build the patient-service image tagged with the current Git commit SHA.
docker build -t $image .

# For a remote cluster, log in to its registry and push the image.
# docker push $image

# Create the namespace before adding namespaced resources.
kubectl apply -f k8s/namespace.yaml

# Load the same local development credentials used by Compose.
$localCredentials = Get-Content .env -Raw | ConvertFrom-StringData
$env:DB_USERNAME = $localCredentials["DB_USERNAME"]
$env:DB_PASSWORD = $localCredentials["DB_PASSWORD"]

# Create or update the Kubernetes Secret without writing credentials to a manifest.
kubectl create secret generic patient-db --namespace patient `
  --from-literal="DB_USERNAME=$env:DB_USERNAME" `
  --from-literal="DB_PASSWORD=$env:DB_PASSWORD" `
  --dry-run=client -o yaml | kubectl apply -f -

# Configure the application endpoints for PostgreSQL, Kafka, and Redis.
kubectl apply -f k8s/configmap.yaml

# Deploy the local development instances of PostgreSQL, Kafka, and Redis.
kubectl apply -f k8s/dependencies.yaml

# Deploy the patient-service API using the image built from this commit.
(Get-Content .\k8s\deployment.yaml -Raw).Replace(
  "image: patient-service:dev",
  "image: $image"
) | kubectl apply -f -

# Create the API ClusterIP service and disruption budget.
kubectl apply -f k8s/service.yaml -f k8s/pdb.yaml

# Wait until PostgreSQL is ready.
kubectl rollout status statefulset/postgres --namespace patient --timeout=240s

# Wait until Kafka is ready.
kubectl rollout status deployment/kafka --namespace patient --timeout=240s

# Wait until Redis is ready.
kubectl rollout status deployment/redis --namespace patient --timeout=240s

# Wait until both patient-service replicas are ready.
kubectl rollout status deployment/patient-service --namespace patient --timeout=240s

# Run this in a terminal to access the API at http://localhost:8081.
kubectl port-forward --address 127.0.0.1 service/patient-service 8081:80 --namespace patient
