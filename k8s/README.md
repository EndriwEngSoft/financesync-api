 # Kubernetes Manifests - FinanceSync

 ## File Structure

 ```
 k8s/
 ├── namespace.yaml                 # Logical isolation
 ├── configmap.yaml                 # Non-sensitive variables
 ├── secret.yaml                    # Sensitive data (passwords, tokens)
 ├── postgres-deployment.yaml       # PostgreSQL Deployment
 ├── postgres-service.yaml          # PostgreSQL Service
 ├── app-deployment.yaml            # Spring Boot Deployment (3 replicas)
 ├── app-service.yaml               # LoadBalancer Service
 ├── kustomization.yaml             # Resource management
 └── README.md                       # Documentation
 ```

 ## Kubernetes Components

 ### Namespace
 - Logical isolation within cluster
 - Name: `financesync`
 - All resources are isolated

 ### ConfigMap
 - Non-sensitive environment variables
 - Examples: ports, hosts, database names
 - Mutable without redeployment

 ### Secret
 - Sensitive data (passwords, JWT tokens)
 - Encrypted at rest in production
 - Not versioned in production

 ### PostgreSQL Deployment
 - 1 replica (stateful)
 - Image: `postgres:16-alpine`
 - Volume: emptyDir (temporary - use PersistentVolume in production)
 - Health checks: liveness + readiness

 ### PostgreSQL Service
 - Type: ClusterIP (internal only)
 - Name: `postgres` (resolvable within cluster)
 - Port: 5432

 ### Spring Boot Deployment
 - 3 replicas (high availability)
 - Image: `financesync:latest`
 - Rolling updates (zero downtime)
 - Health checks: liveness + readiness
 - Resource limits: 256Mi RAM, 250m CPU (request), 512Mi RAM, 500m CPU (limit)

 ### Spring Boot Service
 - Type: LoadBalancer (external)
 - Port 80 (external) → 8080 (container)
 - NodePort: 30000 (Docker Desktop K8s)

 ### Kustomization
 - Manages all YAML files
 - Deploy with: `kubectl apply -k k8s/`

 ## Usage

 ### Deploy on Kubernetes:

 ```bash
 # 1. Enable K8s on Docker Desktop (Settings → Kubernetes → Enable)

 # 2. Apply manifests
 kubectl apply -k k8s/

 # 3. Check status
 kubectl get pods -n financesync
 kubectl get svc -n financesync

 # 4. View logs
 kubectl logs -n financesync -l app=financesync-app

 # 5. Access application
 # URL: http://localhost:30000/api/swagger-ui.html
 ```

 ## Comparison: docker-compose vs Kubernetes

 | Feature | docker-compose | Kubernetes |
 |---------|---------|-----------|
 | Scale | Single machine | Multiple nodes |
 | Replicas | Manual | Automatic |
 | Health checks | Basic | Advanced |
 | Rolling updates | No | Yes |
 | Load balancing | Basic | Native |
 | Storage | Volumes | PersistentVolumes |
 | Secrets | .env file | K8s Secrets (encrypted) |
 | Monitoring | No | Native metrics |

 ## Development vs Production

 ### Development:
 - Secrets stored in YAML (visible)
 - Storage: emptyDir (data lost on restart)
 - 3 app replicas

 ### Production:
 - Secrets in AWS Secrets Manager / Azure Key Vault / HashiCorp Vault
 - Storage: PersistentVolume (NFS/EBS/Azure Disk)
 - Higher resource limits
 - Network policies
 - RBAC (Role-Based Access Control)

 ## Deployment Lifecycle

 ```
 1. kubectl apply -k k8s/
 2. Namespace created
 3. ConfigMap and Secret created
 4. PostgreSQL Deployment starts (1 Pod)
 5. PostgreSQL Service created
 6. Spring Boot Deployment starts (3 Pods, rolling)
 7. Spring Boot Service LoadBalancer created
 8. Application ready at LoadBalancer endpoint
 ```
