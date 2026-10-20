# EKS learning manifests

This Kustomize overlay is for the disposable, one-node EKS learning cluster. It
keeps the local Kubernetes manifests unchanged, reduces the Patient Service to
one replica, and gives the PostgreSQL pod temporary storage instead of an EBS
persistent volume. Its PodDisruptionBudget allows that single replica to be
voluntarily unavailable during node maintenance.

PostgreSQL data is lost when its pod is recreated. The overlay still uses the
local development PostgreSQL, Kafka, and Redis workloads; do not use it for
production.

The overlay references trusted manifests outside this directory, so Kustomize's
default load restriction must be disabled when rendering it:

```powershell
kubectl kustomize aws\k8s-learning --load-restrictor LoadRestrictionsNone
```

This command only renders YAML. Review the output before applying it to a
cluster. The Patient Service image must be changed to an ECR image before EKS
can pull and run it.
