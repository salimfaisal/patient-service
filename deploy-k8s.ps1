$ErrorActionPreference = "Stop"

function Invoke-CheckedCommand {
    param(
        [scriptblock]$Command,
        [string]$Description
    )

    & $Command
    if ($LASTEXITCODE -ne 0) {
        throw "$Description failed with exit code $LASTEXITCODE."
    }
}

$envFile = Join-Path $PSScriptRoot ".env"
if (-not (Test-Path $envFile)) {
    throw "Missing .env. Copy .env.example to .env and set DB_USERNAME and DB_PASSWORD."
}

$envValues = @{}
foreach ($line in Get-Content $envFile) {
    $trimmedLine = $line.Trim()
    if (-not $trimmedLine -or $trimmedLine.StartsWith("#")) {
        continue
    }

    $separator = $trimmedLine.IndexOf("=")
    if ($separator -lt 1) {
        continue
    }

    $key = $trimmedLine.Substring(0, $separator).Trim()
    $value = $trimmedLine.Substring($separator + 1).Trim()
    if ($value.Length -ge 2 -and (($value.StartsWith('"') -and $value.EndsWith('"')) -or ($value.StartsWith("'") -and $value.EndsWith("'")))) {
        $value = $value.Substring(1, $value.Length - 2)
    }
    $envValues[$key] = $value
}

$dbUsername = $envValues["DB_USERNAME"]
$dbPassword = $envValues["DB_PASSWORD"]
if ([string]::IsNullOrWhiteSpace($dbUsername) -or [string]::IsNullOrWhiteSpace($dbPassword)) {
    throw ".env must define non-empty DB_USERNAME and DB_PASSWORD values."
}

Invoke-CheckedCommand { docker info --format '{{.ServerVersion}}' } "Docker engine check"

$context = & kubectl config current-context
if ($LASTEXITCODE -ne 0 -or [string]::IsNullOrWhiteSpace($context)) {
    throw "No active Kubernetes context. Select the target context with kubectl config use-context."
}
Write-Output "Deploying to Kubernetes context: $context"
Invoke-CheckedCommand { kubectl get nodes } "Kubernetes cluster check"

Invoke-CheckedCommand { docker compose build patient-service } "Patient service image build"
Invoke-CheckedCommand { kubectl apply -f (Join-Path $PSScriptRoot "k8s\namespace.yaml") } "Namespace apply"

$secretYaml = & kubectl create secret generic patient-db `
    --namespace patient `
    "--from-literal=DB_USERNAME=$dbUsername" `
    "--from-literal=DB_PASSWORD=$dbPassword" `
    --dry-run=client -o yaml
if ($LASTEXITCODE -ne 0) {
    throw "Database Secret generation failed with exit code $LASTEXITCODE."
}
$secretYaml | & kubectl apply -f -
if ($LASTEXITCODE -ne 0) {
    throw "Database Secret apply failed with exit code $LASTEXITCODE."
}

Invoke-CheckedCommand {
    kubectl apply `
        -f (Join-Path $PSScriptRoot "k8s\configmap.yaml") `
        -f (Join-Path $PSScriptRoot "k8s\dependencies.yaml") `
        -f (Join-Path $PSScriptRoot "k8s\deployment.yaml") `
        -f (Join-Path $PSScriptRoot "k8s\service.yaml") `
        -f (Join-Path $PSScriptRoot "k8s\pdb.yaml")
} "Application manifests apply"

Invoke-CheckedCommand { kubectl rollout status statefulset/postgres --namespace patient --timeout=240s } "PostgreSQL rollout"
Invoke-CheckedCommand { kubectl rollout status deployment/kafka --namespace patient --timeout=240s } "Kafka rollout"
Invoke-CheckedCommand { kubectl rollout status deployment/redis --namespace patient --timeout=240s } "Redis rollout"
Invoke-CheckedCommand { kubectl rollout status deployment/patient-service --namespace patient --timeout=240s } "Patient service rollout"

Write-Output "Deployment complete. To access the API, run: kubectl port-forward --address 127.0.0.1 service/patient-service 8081:80 --namespace patient"
