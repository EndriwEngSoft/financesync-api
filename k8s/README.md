 # Kubernetes Manifests - FinanceSync

## 📋 Estrutura de Arquivos

```
k8s/
├── namespace.yaml                 # Isolamento lógico do cluster
├── configmap.yaml                 # Variáveis públicas
├── secret.yaml                    # Variáveis sensíveis (senhas, tokens)
├── postgres-deployment.yaml       # Deployment do PostgreSQL
├── postgres-service.yaml          # Service que expõe PostgreSQL
├── app-deployment.yaml            # Deployment do Spring Boot (3 replicas)
├── app-service.yaml               # Service LoadBalancer da app
├── kustomization.yaml             # Agrupa todos recursos
└── README.md                       # Este arquivo
```

## 🎯 Componentes Kubernetes

### 1. **Namespace**
- Isolamento lógico dentro do cluster
- Nome: `financesync`
- Todos recursos aqui estão isolados

### 2. **ConfigMap**
- Variáveis de ambiente NÃO-sensíveis
- Exemplos: portas, hosts, nomes de database
- Alterável sem redesployment

### 3. **Secret**
- Variáveis SENSÍVEIS (senhas, tokens JWT)
- Armazenado criptografado em produção
- Em desenvolvimento: arquivo YAML (NÃO VERSIONADO em produção)

### 4. **PostgreSQL Deployment**
- 1 replica (banco é stateful)
- Image: `postgres:16-alpine`
- Volume: emptyDir (temporário - em produção usar PersistentVolume)
- Health checks: liveness + readiness

### 5. **PostgreSQL Service**
- Type: ClusterIP (apenas acesso interno)
- Name: `postgres` (resolvível dentro do cluster)
- Port: 5432

### 6. **Spring Boot Deployment**
- 3 replicas (alta disponibilidade)
- Image: `financesync:latest` (sua imagem do docker build)
- Rolling updates (sem downtime)
- Health checks: liveness + readiness
- Resource limits: 256Mi RAM, 250m CPU (request), 512Mi RAM, 500m CPU (limit)

### 7. **Spring Boot Service**
- Type: LoadBalancer (expõe externamente)
- Port 80 (externa) → 8080 (container)
- NodePort: 30000 (para Docker Desktop K8s)

### 8. **Kustomization**
- Agrupa todos os arquivos YAML
- Permite aplicar com: `kubectl apply -k k8s/`

## 🚀 Como Usar

### Deploy no Kubernetes Desktop:

```bash
# 1. Ativar K8s no Docker Desktop (Settings → Kubernetes → Enable)

# 2. Aplicar manifests
kubectl apply -k k8s/

# 3. Verificar status
kubectl get pods -n financesync
kubectl get svc -n financesync

# 4. Ver logs
kubectl logs -n financesync -l app=financesync-app

# 5. Acessar app
# Localhost: http://localhost:30000/api/swagger-ui.html
```

## 📊 Diferenças: docker-compose vs Kubernetes

| Feature | docker-compose | Kubernetes |
|---------|---------|-----------|
| Escala | 1 máquina | Múltiplos nodes |
| Replicas | Manual | Automático |
| Health checks | ✅ Básico | ✅ Avançado |
| Rolling updates | ❌ Não | ✅ Sim |
| Load balancing | ⚠️ Básico | ✅ Nativo |
| Storage | Volumes | PersistentVolumes |
| Secrets | .env | K8s Secrets (encrypted) |
| Monitoring | ❌ | ✅ Métricas nativas |

## ⚠️ Notas Importantes

### Desenvolvimento vs Produção:

**Desenvolvimento (aqui):**
- Secret armazenado em YAML (visível)
- Storage: emptyDir (dados perdidos ao reiniciar)
- 3 replicas da app (pode ser 1)

**Produção REAL:**
- Secret em AWS Secrets Manager / Azure Key Vault / Vault
- Storage: PersistentVolume em NFS/EBS/Azure Disk
- Resource limits mais altos
- Network policies
- RBAC (Role-Based Access Control)

## 🔄 Lifecycle de Deploy

```
1. kubectl apply -k k8s/
2. Namespace é criado
3. ConfigMap e Secret são criados
4. PostgreSQL Deployment inicia (1 Pod)
5. PostgreSQL Service é criado
6. Spring Boot Deployment inicia (3 Pods, gradualmente)
7. Spring Boot Service LoadBalancer é criado
8. Usuários acessam via LoadBalancer (porta 80)
```

## 📝 Probes Explicadas

### Liveness Probe
```yaml
livenessProbe:
  httpGet: GET /api/swagger-ui.html
  initialDelaySeconds: 60  # espera 60s antes de começar
  periodSeconds: 10        # verifica a cada 10s
  failureThreshold: 3      # falha 3 vezes = reinicia
```

### Readiness Probe
```yaml
readinessProbe:
  httpGet: GET /api/swagger-ui.html
  initialDelaySeconds: 30  # espera 30s
  periodSeconds: 5         # verifica a cada 5s
  failureThreshold: 3      # falha 3 vezes = remove do load balancer
```

## 🎯 Próximos Passos

1. ✅ Docker + docker-compose (DONE)
2. ⏳ Kubernetes local (este passo)
3. → Helm charts (automação de K8s)
4. → CI/CD com GitHub Actions
5. → Deploy em cluster real (EKS/AKS/GKE)
