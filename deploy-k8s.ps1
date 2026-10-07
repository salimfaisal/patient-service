# Check which Kubernetes cluster kubectl will deploy to.
kubectl config current-context

# Confirm the selected cluster is reachable.
kubectl get nodes

# Build the patient-service:dev image referenced by k8s/deployment.yaml.
docker compose build patient-service

# Create the namespace before adding namespaced resources.
kubectl apply -f k8s/namespace.yaml

# Set these to the same database credentials used by the application.
# Use a local development password here; do not commit real credentials.
$env:DB_USERNAME = "patientapp"
$env:DB_PASSWORD = "<replace-with-a-local-development-password>"

# Create or update the Kubernetes Secret without writing credentials to a manifest.
kubectl create secret generic patient-db --namespace patient `
  --from-literal="DB_USERNAME=$env:DB_USERNAME" `
  --from-literal="DB_PASSWORD=$env:DB_PASSWORD" `
  --dry-run=client -o yaml | kubectl apply -f -

# Configure the application endpoints for PostgreSQL, Kafka, and Redis.
kubectl apply -f k8s/configmap.yaml

# Deploy the local development instances of PostgreSQL, Kafka, and Redis.
kubectl apply -f k8s/dependencies.yaml

# Deploy the patient-service API.
kubectl apply -f k8s/deployment.yaml

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
