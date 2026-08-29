# FinanceSync API

> API REST de alto desempenho para gerenciamento financeiro pessoal, com contas, categorias e transações isoladas por usuário, autenticação JWT e infraestrutura pronta para produção (Docker + Kubernetes).

[![CI](https://github.com/EndriwEngSoft/financesync-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EndriwEngSoft/financesync-api/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?logo=kubernetes&logoColor=white)](https://kubernetes.io/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Índice

1. [Visão geral](#visão-geral)
2. [Stack técnica](#stack-técnica)
3. [Arquitetura](#arquitetura)
4. [Endpoints da API](#endpoints-da-api)
5. [Como rodar localmente](#como-rodar-localmente)
6. [Variáveis de ambiente](#variáveis-de-ambiente)
7. [Segurança](#segurança)
8. [Testes](#testes)
9. [Deploy](#deploy)
10. [Documentação da API](#documentação-da-api)
11. [Licença](#licença)
12. [Autor](#autor)

---

## Visão geral

O FinanceSync API é o motor backend de um ecossistema de finanças pessoais, desenvolvido com Java 21 e Spring Boot 4.0.3. O sistema garante isolamento completo de dados entre usuários: cada conta autenticada tem governança total e exclusiva sobre suas próprias contas, categorias e transações.

Pontos que valem destaque técnico:

- **Isolamento por usuário em todas as camadas.** Contas, categorias e transações são sempre filtradas pelo usuário autenticado.
- **Autenticação stateless via JWT** (JJWT), com senhas em hash BCrypt.
- **Camada de validação defensiva** com Bean Validation (`@NotBlank`, `@Email`, `@Positive`) nos DTOs, antes da persistência.
- **Tratamento de erros centralizado** via `@RestControllerAdvice`, com respostas padronizadas (`ErrorResponse`).
- **Observabilidade nativa** via Spring Actuator (`/api/actuator/health`), integrada às sondas de liveness/readiness do Docker e Kubernetes.
- **Infraestrutura como código**, com manifestos Kubernetes organizados via Kustomize.
- **Build de produção enxuto** — Dockerfile multi-stage reduz a imagem final para ~135MB.

---

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem / Runtime | Java 21, Spring Boot 4.0.3 |
| Web / Segurança | Spring Web, Spring Security 7.x |
| Persistência | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Autenticação | JWT (JJWT 0.12.6), senhas com BCrypt |
| Validação | Bean Validation (`@NotBlank`, `@Email`, `@Positive`) |
| Documentação | SpringDoc OpenAPI 3.0.2 (Swagger UI) |
| Observabilidade | Spring Actuator |
| Testes | JUnit 5, Mockito |
| Build | Maven (via wrapper `mvnw`) |
| Containerização | Docker, Docker Compose |
| Orquestração | Kubernetes (manifestos + Kustomize) |
| CI/CD | GitHub Actions |

---

## Arquitetura

### Estrutura de pacotes

Organização em camadas técnicas (Controller → Service → Repository):

```
src/main/java/
└── com/endriw/financesync/
    ├── Application.java               # Inicializador do Spring Boot
    ├── config/                        # Configurações globais (SwaggerConfig, etc.)
    ├── controller/                    # Controllers REST
    ├── dto/                           # Data Transfer Objects (request/response)
    ├── exception/                     # GlobalExceptionHandler
    ├── model/                         # Entidades JPA
    │   └── enums/                     # UserRole, TransactionType, etc.
    ├── repository/                    # Interfaces JPA Repository
    ├── security/                      # Filtros JWT, UserDetailsService, criptografia
    └── service/                       # Regras de negócio
src/test/java/                         # Testes unitários e de integração (JUnit 5 / Mockito)
src/main/resources/
    ├── application.properties         # Configuração local (não versionado)
    └── application.properties.example # Template com as chaves necessárias
```

---

## Endpoints da API

Base local: `http://localhost:8080/api` · Documentação interativa: `/swagger-ui/index.html`

Todos os endpoints exigem `Authorization: Bearer <token>`, exceto os de `/auth/*`.

#### Autenticação

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Registra um novo usuário |
| `POST` | `/auth/login` | Autentica e retorna o token JWT |

#### Contas Bancárias

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/accounts` | Lista as contas do usuário autenticado |
| `GET` | `/accounts/{id}` | Busca uma conta por ID |
| `POST` | `/accounts` | Cria uma conta (Corrente, Poupança, Carteira) |
| `PUT` | `/accounts/{id}` | Atualiza uma conta existente |
| `DELETE` | `/accounts/{id}` | Remove a conta |

#### Categorias

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/categories` | Lista as categorias do usuário |
| `GET` | `/categories/{id}` | Busca uma categoria por ID |
| `POST` | `/categories` | Cria uma categoria |
| `PUT` | `/categories/{id}` | Atualiza uma categoria |
| `DELETE` | `/categories/{id}` | Remove uma categoria |

#### Transações

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/transactions` | Lista o histórico de transações do usuário |
| `GET` | `/transactions/{id}` | Busca uma transação por ID |
| `POST` | `/transactions` | Registra uma nova transação (entrada ou saída) |
| `PUT` | `/transactions/{id}` | Atualiza uma transação |
| `DELETE` | `/transactions/{id}` | Remove uma transação |

---

## Como rodar localmente

### Pré-requisitos

- JDK 21+
- Docker (para Docker Compose ou Kubernetes local)
- PostgreSQL 16+ (opcional, só se não for usar Docker)

### Docker Compose (recomendado)

```bash
git clone https://github.com/EndriwEngSoft/financesync-api.git
cd financesync-api

cp .env.example .env
# ajuste as credenciais (DB, JWT_SECRET, etc.) no .env

docker compose up -d
docker compose ps
```

### Spring Boot direto

Requer uma instância do PostgreSQL rodando localmente na porta `5432` (pode usar só o container do banco: `docker compose up db -d`).

```bash
psql -U postgres -c "CREATE DATABASE financesync_db;"

cp src/main/resources/application.properties.example src/main/resources/application.properties
# ajuste as credenciais do banco e o segredo do JWT no arquivo copiado

./mvnw spring-boot:run
```

### Kubernetes

```bash
cp k8s/secret.yaml.example k8s/secret.yaml
# preencha os valores em Base64 no secret.yaml

kubectl apply -k k8s/
kubectl get pods -n financesync
kubectl get svc -n financesync
```

Acessível localmente via NodePort em `http://localhost:30000` (conforme o manifesto).

---

## Variáveis de ambiente

| Variável | Fallback (dev) | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` (sobrescrito para `postgres` no Docker Compose) | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `financesync_db` | Nome do banco de dados |
| `DB_USER` | `seu_usuario` | Usuário do banco |
| `DB_PASSWORD` | `sua_senha` | Senha do banco |
| `JWT_SECRET` | chave de dev embutida | Chave HMAC para assinatura dos tokens (mínimo 256 bits) |
| `JWT_EXPIRATION` | `86400000` (24h) | TTL do token, em ms |
| `SERVER_PORT` | `8080` | Porta HTTP |
| `SERVER_SERVLET_CONTEXT_PATH` | `/api` | Prefixo base das rotas |

---

## Segurança

- **Filtros stateless.** `OncePerRequestFilter` valida assinatura e expiração do JWT antes de liberar o contexto de segurança do Spring, em cada requisição.
- **Senhas com hash BCrypt**, aplicado na implementação customizada de `UserDetailsService`.
- **Sem sessão em memória** — o servidor é stateless, permitindo escalabilidade horizontal.

---

## Testes

```bash
./mvnw test
```

Suíte com testes unitários (JUnit 5, Mockito) para services e controllers. Relatórios gerados em `target/surefire-reports/`.

---

## Deploy

### Docker (build multi-stage)

O `Dockerfile` roda em duas fases: uma imagem completa do Maven compila e empacota o `.jar`; a imagem final descarta Maven e código-fonte, mantendo só o JRE do OpenJDK. Resultado: imagem de produção de ~135MB (`eclipse-temurin:21-jre-alpine`).

### CI/CD

`.github/workflows/ci.yml` roda em todo push/PR para `main`: sobe um serviço PostgreSQL, copia o `application.properties.example`, dá permissão de execução ao `mvnw` e executa `./mvnw -B clean test`.

### Kubernetes

Manifestos organizados via Kustomize em `k8s/` — deployment, service, configmap, secret de exemplo, PVC e namespace dedicado.

---

## Documentação da API

Após subir a aplicação por qualquer um dos métodos acima:

- **Swagger UI:** `http://localhost:8080/api/swagger-ui/index.html`
- **OpenAPI JSON:** `http://localhost:8080/api/v3/api-docs`
- **OpenAPI YAML:** `http://localhost:8080/api/v3/api-docs.yaml`

> O prefixo `/api` vem de `server.servlet.context-path`, configurável via `SERVER_SERVLET_CONTEXT_PATH`.

---

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE) para o texto completo.

---

## Autor

**Endriw Colvara Bento**  
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/endriw-bento)
[![Portfólio](https://img.shields.io/badge/Portf%C3%B3lio-000?style=flat&logo=vercel&logoColor=white)](https://endriwdev.vercel.app/)
