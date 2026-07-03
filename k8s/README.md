# Manifestos Kubernetes - FinanceSync

## Estrutura de Arquivos

```
k8s/
├── namespace.yaml                 # Isolamento lógico
├── configmap.yaml                 # Variáveis não-sensíveis
├── secret.yaml                    # Dados sensíveis (senhas, tokens) - NÃO versionado (veja .gitignore)
├── postgres-deployment.yaml       # Deployment do PostgreSQL
├── postgres-service.yaml          # Serviço do PostgreSQL
├── app-deployment.yaml            # Deployment do Spring Boot (3 réplicas)
├── app-service.yaml               # Serviço LoadBalancer
├── kustomization.yaml             # Gerenciamento de recursos
├── secret.yaml.example            # Modelo para secret.yaml (versionado)
└── README.md                       # Documentação
```

## Componentes Kubernetes

### Namespace
- Isolamento lógico dentro do cluster
- Nome: `financesync`
- Todos os recursos estão isolados

### ConfigMap
- Variáveis de ambiente não-sensíveis
- Exemplos: portas, hosts, nomes de banco de dados
- **Todos os valores estão entre aspas** conforme exigido pelo Kubernetes (ex: `"5432"`, `"true"`)
- Mutável sem necessidade de redeploy

### Secret
- Dados sensíveis (senhas, tokens JWT)
- Criptografados em repouso em produção
- **Não versionado**: `k8s/secret.yaml` está listado em `.gitignore` e **nunca deve ser commitado**
- Use `k8s/secret.yaml.example` como modelo para desenvolvimento local
- Arquivos de backup (`*.bak`, `*.bak2`, `*.test`) também são ignorados para evitar exposição acidental

### Deployment do PostgreSQL
- 1 réplica (stateful)
- Imagem: `postgres:16-alpine`
- Volume: emptyDir (temporário - use PersistentVolume em produção)
- Verificações de saúde: liveness + readiness

### Serviço do PostgreSQL
- Tipo: ClusterIP (interno apenas)
- Nome: `postgres` (resolúvel dentro do cluster)
- Porta: 5432

### Deployment do Spring Boot
- 3 réplicas (alta disponibilidade)
- Imagem: `financesync:latest`
- Atualizaçõesrolling (zero downtime)
- Verificações de saúde: liveness + readiness
- Limites de recursos: 256Mi RAM, 250m CPU (request), 512Mi RAM, 500m CPU (limit)

### Serviço do Spring Boot
- Tipo: LoadBalancer (externo)
- Porta 80 (externo) → 8080 (container)
- NodePort: 30000 (Docker Desktop K8s)

### Kustomization
- Gerencia todos os arquivos YAML
- Deploy com: `kubectl apply -k k8s/`

## Gerenciamento de Secrets

**⚠️ IMPORTANTE: `k8s/secret.yaml` NÃO é versionado no Git (está no `.gitignore`)**
Arquivos de backup (`*.bak`, `*.bak2`, `*.test`) também são ignorados.

### Configuração Local do Secret (Desenvolvimento):

1. Copie o modelo:
```bash
cp k8s/secret.yaml.example k8s/secret.yaml
```

2. Edite com seus valores:
```bash
# Edite o arquivo:
vim k8s/secret.yaml
# ou
code k8s/secret.yaml
```

3. Defina senhas fortes:
```yaml
DB_PASSWORD: "sua_senha_banco_forte"        # Altere isto!
POSTGRES_PASSWORD: "sua_senha_postgres_forte"  # Altere isto!
JWT_SECRET: "sua_chave_jwt_forte_de_64_caracteres_minimo"  # Altere isto!
```

### Como gerar um JWT Secret seguro:

**Linux/macOS:**
```bash
openssl rand -base64 64 | tr -d '\n'
```

**PowerShell (Windows):**
```powershell
-join ((0..63) | ForEach-Object { "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz012341]]))
```

> **Observação**: Para este repositório, senhas fortes e um JWT secret já foram definidos localmente (eles **não** são commitados). Substitua-os pelos seus próprios valores se desejar rodar as credenciais.

### Gerenciamento de Secrets em Produção:
- **AWS**: AWS Secrets Manager
- **Azure**: Azure Key Vault
- **GCP**: Secret Manager
- **On-Premises**: HashiCorp Vault ou similar

Referência no Kubernetes (exemplo com operador de secrets externos):
```yaml
secretRef:
  name: financesync-secret  # Secret externo sincronizado por controller
```

## Uso

### Deploy no Kubernetes:

```bash
# 1. Ative o Kubernetes no Docker Desktop (Settings → Kubernetes → Enable)

# 2. Aplique os manifests
kubectl apply -k k8s/

# 3. Verifique o status
kubectl get pods -n financesync
kubectl get svc -n financesync

# 4. Visualize logs
kubectl logs -n financesync -l app=financesync-app

# 5. Acesse a aplicação
# URL: http://localhost:30000/api/swagger-ui.html
```

## Comparação: docker-compose vs Kubernetes

| Feature | docker-compose | Kubernetes |
|---------|---------|-----------|
| Escala | Máquina única | Múltiplos nós |
| Réplicas | Manual | Automática |
| Verificações de saúde | Básicas | Avançadas |
| Atualizações rolling | Não | Sim |
| Balanceamento de carga | Básico | Nativo |
| Armazenamento | Volumes | PersistentVolumes |
| Secrets | Arquivo .env | Secrets do Kubernetes (criptografados) |
| Monitoramento | Nenhum | Métricas nativas |

## Desenvolvimento vs Produção

### Desenvolvimento:
- Secrets armazenados em YAML (visíveis apenas localmente, nunca commitados)
- Armazenamento: emptyDir (dados perdidos ao reiniciar)
- 3 réplicas da aplicação

### Produção:
- Secrets no AWS Secrets Manager / Azure Key Vault / HashiCorp Vault
- Armazenamento: PersistentVolume (NFS/EBS/Azure Disk)
- Limites de recursos maiores
- Políticas de rede
- RBAC (Controle de Acesso baseado em Funções)

## Ciclo de Deploy

```
1. kubectl apply -k k8s/
2. Namespace criado
3. ConfigMap e Secret criados
4. Deployment do PostgreSQL inicia (1 Pod)
5. Serviço do PostgreSQL criado
6. Deployment do Spring Boot inicia (3 Pods, rolling)
7. Serviço LoadBalancer do Spring Boot criado
8. Aplicação pronta no endpoint do LoadBalancer
```

## Notas de Segurança
- Todos os arquivos de backup/teste (`*.bak`, `*.bak2`, `*.test`) são ignorados por `.gitignore` para evitar o commit acidental de dados sensíveis.
- Os valores do `configmap.yaml` estão devidamente entre aspas (ex: `"5432"`, `"true"`) para satisfazer o requisito do Kubernetes de `map[string]string`.
- Caso precise rotacionar os secrets localmente, atualize `k8s/secret.yaml` e reaplique: `kubectl apply -k k8s/`.