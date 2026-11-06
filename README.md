# FinanceSync API

> API REST de finanças pessoais em Spring Boot: **contas**, **categorias** e **transações** isoladas por usuário, com **conversão de moeda** via API externa, autenticação JWT, Docker e manifestos Kubernetes.

[![CI](https://github.com/EndriwEngSoft/financesync-api/actions/workflows/ci.yml/badge.svg)](https://github.com/EndriwEngSoft/financesync-api/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white)](https://www.oracle.com/java/technologies/downloads/#java21)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## Índice

1. [Decisões técnicas](#decisões-técnicas)
2. [Stack técnica](#stack-técnica)
3. [Arquitetura](#arquitetura)
4. [Endpoints da API](#endpoints-da-api)
5. [Testes](#testes)
6. [Como rodar localmente](#como-rodar-localmente)
7. [Variáveis de ambiente](#variáveis-de-ambiente)
8. [Infraestrutura](#infraestrutura)
9. [Limitações conhecidas](#limitações-conhecidas)
10. [Licença](#licença)
11. [Autor](#autor)

---

## Decisões técnicas

- **Conversão de moeda com integração externa.** Quando a moeda da transação é diferente da moeda da conta, o serviço consulta a [Frankfurter API](https://frankfurter.dev) via `RestClient` e grava o valor original e o convertido.
- **Cotação auditável.** A taxa usada (moedas de origem e destino, valor e data) é persistida em uma entidade `CurrencyConversion` ligada à transação. Dá para saber exatamente qual cotação gerou cada valor convertido, mesmo depois que o câmbio mudar.
- **Falha externa vira `503`, não `500`.** Se a Frankfurter não responder, o cliente lança `ExternalServiceException`, convertida em `503 Service Unavailable`. Quem consome a API sabe que o problema é temporário e externo, não um bug do servidor.
- **Dinheiro em `BigDecimal`.** Valores e taxas nunca passam por `double`; a conversão usa escala 4 com arredondamento `HALF_UP`. A moeda é validada no DTO como código ISO 4217 de 3 letras.
- **Isolamento por usuário.** Listagens filtram pelo usuário do token. Na busca por ID, o serviço verifica o dono do recurso. Ao criar uma transação, valida que a conta **e** a categoria pertencem ao usuário, para que ninguém lance uma transação na conta de outra pessoa.
- **Autenticação stateless** com JWT (JJWT) e senhas em hash BCrypt; nenhuma sessão fica em memória no servidor.
- **Erros padronizados** em um `@RestControllerAdvice`: `400` para validação, `403` para recurso de outro usuário, `404` para recurso inexistente, `503` para falha da API externa.

---

## Stack técnica

| Camada | Tecnologia |
|---|---|
| Linguagem / Runtime | Java 21, Spring Boot 4.0.3 |
| Web / Segurança | Spring Web, Spring Security (stateless) |
| Persistência | Spring Data JPA, Hibernate, PostgreSQL 16 |
| Autenticação | JWT (JJWT 0.12.6), BCrypt |
| Integração externa | `RestClient` + Frankfurter API (câmbio) |
| Validação | Bean Validation |
| Documentação | SpringDoc OpenAPI (Swagger UI) |
| Observabilidade | Spring Actuator (health check) |
| Testes | JUnit 5, Mockito, MockMvc (`@WebMvcTest`) |
| Build | Maven (via wrapper `mvnw`) |
| Containerização | Docker (multi-stage), Docker Compose |
| Orquestração | Manifestos Kubernetes com Kustomize |
| CI | GitHub Actions |

---

## Arquitetura

Organização em camadas técnicas (Controller → Service → Repository):

```
com.endriw.financesync
├── client/       # FrankfurterClient (chamada HTTP à API de câmbio)
├── config/       # RestClientConfig, SwaggerConfig
├── controller/   # Controllers REST
├── dto/          # Requests e respostas (inclui DTO da integração)
├── exception/    # GlobalExceptionHandler, ExternalServiceException
├── model/        # Entidades JPA + enums
├── repository/   # Interfaces Spring Data JPA
├── security/     # Filtro JWT, JwtService, UserDetailsService, SecurityConfig
├── service/      # Regras de negócio
└── Application.java
```

---

## Endpoints da API

Base local: `http://localhost:8080/api` · Documentação interativa: `/api/swagger-ui/index.html`

O prefixo `/api` vem de `server.servlet.context-path`. Todos os endpoints exigem `Authorization: Bearer <token>`, exceto os de `/auth/*`, o Swagger e o health check.

#### Autenticação

| Método | Rota | Descrição |
|---|---|---|
| `POST` | `/auth/register` | Registra um novo usuário |
| `POST` | `/auth/login` | Autentica e retorna o token JWT |

#### Contas

| Método | Rota | Descrição |
|---|---|---|
| `GET` | `/accounts` | Lista as contas do usuário autenticado |
| `GET` | `/accounts/{id}` | Busca uma conta por ID |
| `POST` | `/accounts` | Cria uma conta (`CHECKING`, `SAVING` ou `WALLET`) |
| `PUT` | `/accounts/{id}` | Atualiza uma conta |
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
| `GET` | `/transactions` | Lista as transações do usuário |
| `GET` | `/transactions/{id}` | Busca uma transação por ID |
| `POST` | `/transactions` | Registra uma transação (`INCOME` ou `EXPENSE`), com conversão se a moeda for diferente da conta |
| `PUT` | `/transactions/{id}` | Atualiza uma transação |
| `DELETE` | `/transactions/{id}` | Remove uma transação |

---

## Testes

**102 testes**, executados a cada push e pull request pelo CI (veja o badge no topo).

| Classe | Tipo | Qtde. |
|---|---|---|
| `AccountControllerTest` | Fatia web (`@WebMvcTest` + MockMvc) | 20 |
| `CategoryControllerTest` | Fatia web (`@WebMvcTest` + MockMvc) | 20 |
| `TransactionControllerTest` | Fatia web (`@WebMvcTest` + MockMvc) | 20 |
| `TransactionServiceTest` | Unitário (Mockito), inclui conversão de moeda | 17 |
| `AccountServiceTest` | Unitário (Mockito) | 12 |
| `CategoryServiceTest` | Unitário (Mockito) | 12 |
| `ApplicationTests` | Carga do contexto Spring contra PostgreSQL | 1 |

```bash
./mvnw test
```

No CI, o `ApplicationTests` sobe o contexto completo contra um PostgreSQL 16 rodando como *service container* do GitHub Actions. Localmente, ele precisa de um PostgreSQL acessível (veja abaixo).

---

## Como rodar localmente

### Pré-requisitos

- JDK 21+
- Docker (para Docker Compose ou Kubernetes local)
- PostgreSQL 16+ (só se não for usar Docker)

### Docker Compose (recomendado)

```bash
git clone https://github.com/EndriwEngSoft/financesync-api.git
cd financesync-api

cp .env.example .env
# ajuste as credenciais (banco, JWT_SECRET etc.) no .env

docker compose up -d
docker compose ps
```

O Compose sobe o PostgreSQL com health check e só inicia a aplicação depois que o banco estiver saudável (`depends_on: condition: service_healthy`).

### Spring Boot direto

Requer um PostgreSQL na porta `5432` (pode subir só o banco com `docker compose up postgres -d`).

```bash
cp src/main/resources/application.properties.example src/main/resources/application.properties
# ajuste as credenciais do banco e o segredo do JWT no arquivo copiado

./mvnw spring-boot:run
```

---

## Variáveis de ambiente

| Variável | Fallback (dev) | Descrição |
|---|---|---|
| `DB_HOST` | `localhost` (no Compose, `postgres`) | Host do PostgreSQL |
| `DB_PORT` | `5432` | Porta do PostgreSQL |
| `DB_NAME` | `financesync_db` | Nome do banco |
| `DB_USER` | `seu_usuario` | Usuário do banco |
| `DB_PASSWORD` | `sua_senha` | Senha do banco |
| `POSTGRES_PASSWORD` | (sem fallback) | Senha usada pelo container do PostgreSQL no Compose |
| `JWT_SECRET` | `sua-chave-secreta-aqui` | Chave HMAC para assinar os tokens (mínimo 32 caracteres / 256 bits) |
| `JWT_EXPIRATION` | `86400000` (24h) | Validade do token, em ms |
| `SERVER_PORT` | `8080` | Porta HTTP |
| `SERVER_SERVLET_CONTEXT_PATH` | `/api` | Prefixo base das rotas |

---

## Infraestrutura

### Docker (build multi-stage)

- **Estágio de build:** imagem Maven com JDK 21. O `pom.xml` é copiado e as dependências baixadas (`mvn dependency:go-offline`) **antes** do código-fonte, então mudar só o código reaproveita a camada de dependências em cache.
- **Estágio final:** `eclipse-temurin:21-jre-alpine` com apenas o `.jar`, sem Maven, JDK nem código-fonte.
- **`HEALTHCHECK`** consulta `/api/actuator/health`, e o argumento `SKIP_TESTS` controla se o build roda os testes.

### Kubernetes (Kustomize)

Manifestos em `k8s/`, aplicados com `kubectl apply -k k8s/`:

- `namespace` dedicado e `configmap` para configuração não sensível;
- `secret.yaml.example` como modelo: o secret real não é versionado;
- `Deployment` da aplicação com 3 réplicas, `RollingUpdate`, *liveness* e *readiness probes* no Actuator e `requests`/`limits` de CPU e memória;
- PostgreSQL com `Deployment`, `Service` e `PersistentVolumeClaim`.

```bash
cp k8s/secret.yaml.example k8s/secret.yaml
# preencha os valores em Base64 no secret.yaml

kubectl apply -k k8s/
kubectl get pods -n financesync
```

O `Service` da aplicação usa `nodePort: 30000`, pensado para o Kubernetes do Docker Desktop.

### CI

`.github/workflows/ci.yml` roda em todo push e pull request para `main`: sobe um PostgreSQL 16 como *service container*, copia o `application.properties.example`, configura o JDK 21 e executa `./mvnw -B clean test`.

---

## Limitações conhecidas

- O schema é gerado pelo Hibernate (`ddl-auto=update`), sem migrations versionadas.
- Os IDs são `Long` sequenciais, e recurso de outro usuário responde `403` (o que confirma que o ID existe).
- O usuário tem um campo `role`, mas ainda não há regras de autorização por papel.
- Novas contas são criadas em `BRL`; a moeda da conta ainda não é configurável pela API.
- Não há deploy público: a API roda localmente via Docker Compose ou Kubernetes.

---

## Licença

Distribuído sob a licença MIT. Veja [LICENSE](LICENSE).

---

## Autor

**Endriw Colvara Bento**, estudante de Engenharia de Software (Estácio), com foco em backend Java e Spring Boot.

- GitHub: [github.com/EndriwEngSoft](https://github.com/EndriwEngSoft)
- LinkedIn: [linkedin.com/in/endriw-bento](https://www.linkedin.com/in/endriw-bento/)
- Portfólio: [endriwdev.vercel.app](https://endriwdev.vercel.app/)
