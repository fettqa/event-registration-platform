# Kubernetes manifests (local lab)

Namespace `erp`: Postgres + Event Registration API.

```powershell
kind create cluster --name erp
docker build -t event-registration:local .
kind load docker-image event-registration:local --name erp
kubectl apply -f k8s/
kubectl port-forward -n erp svc/event-registration 8080:8080
```
