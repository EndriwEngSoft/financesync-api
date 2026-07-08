# 💰 FinanceSync API

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 21">
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.3-brightgreen?style=for-the-badge&logo=spring&logoColor=white" alt="Spring Boot 4.0.3">
  <img src="https://img.shields.io/badge/PostgreSQL-16-blue?style=for-the-badge&logo=postgresql&logoColor=white" alt="PostgreSQL 16">
  <img src="https://img.shields.io/badge/Docker-Multi--Stage-blue?style=for-the-badge&logo=docker&logoColor=white" alt="Docker">
  <img src="https://img.shields.io/badge/Kubernetes-K8s-blue?style=for-the-badge&logo=kubernetes&logoColor=white" alt="Kubernetes">
</p>

API REST de alto desempenho para gerenciamento financeiro pessoal. A aplicação conta com ecossistema completo de segurança robusta baseada em JWT, controle de contas bancárias, categorização inteligente, barreira de validação de dados, tratamento global de exceções e orquestração moderna pronta para produção.

---

## 📌 Sobre o Projeto

O **FinanceSync API** é o motor backend de um ecossistema de finanças pessoais desenvolvido com **Java 21** e **Spring Boot 4.0.3**. Projetado sob princípios de design de software robustos e arquitetura limpa em camadas, o sistema garante o isolamento completo de dados entre os usuários: cada indivíduo autenticado possui governança total e exclusiva sobre suas próprias contas, categorias e movimentações financeiras.

### Diferenciais do Ecossistema Atualizado:
* **DevOps Ready:** Dockerfile otimizado via *multi-stage build* reduzindo drasticamente a pegada em memória e tamanho final da imagem.
* **Orquestração Escalável:** Manifestos nativos do Kubernetes estruturados via Kustomize para ambientes locais ou cloud.
* **Observabilidade:** Health checks nativos através do Spring Actuator integrados diretamente às sondas do Docker e Kubernetes (*Liveness/Readiness*).
* **Alta Cobertura de Testes:** Suíte robusta de testes utilizando JUnit 5 e Mockito simulando cenários reais de negócio de ponta a ponta.

---

## ✨ Funcionalidades Principais

| Módulo / Categoria | Detalhes Técnicos Implementados |
| :--- | :--- |
| **Autenticação & Autorização** | Registro (`/auth/register`) e Login (`/auth/login`) gerando tokens JWT criptografados (`io.jsonwebtoken` 0.12.6) com senhas em hash BCrypt. |
| **Contas Bancárias** | CRUD completo suportando tipos dinâmicos como Conta Corrente, Poupança e Carteira Física com rastreamento de status. |
| **Categorias** | Gerenciamento de categorias customizáveis criadas de forma isolada e específica para o contexto de cada usuário (ex: Alimentação, Saúde, Lazer). |
| **Transações Financeiras** | Registro completo de fluxos monetários mapeando tipo (Entrada/Saída), métodos de pagamento, taxas operacionais e status da transação. |
| **Validação de Entrada** | Camada defensiva utilizando Bean Validation (`@NotBlank`, `@Email`, `@Positive`, etc.) interceptando payloads inválidos nos DTOs antes da persistência. |
| **Tratamento de Erros** | Arquitetura com `@RestControllerAdvice` centralizado, devolvendo respostas padronizadas através do objeto customizado `ErrorResponse`. |
| **Documentação Viva** | Mapeamento automático dos contratos da API através do SpringDoc OpenAPI 3.0.2, gerando uma interface gráfica interativa via Swagger UI. |
| **Observabilidade** | Exposição estratégica de métricas de saúde via Actuator (`/api/actuator/health`) garantindo monitoramento constante do ciclo de vida da app. |

---

## 🛠️ Tecnologias & Versões

* **Linguagem Core:** Java 21
* **Framework Principal:** Spring Boot 4.0.3
* **Persistência de Dados:** Spring Data JPA / Hibernate
* **Segurança:** Spring Security 7.x + JWT (JJWT 0.12.6)
* **Banco de Dados Relacional:** PostgreSQL 16 (Alpine)
* **Documentação:** SpringDoc OpenAPI 3.0.2 / Swagger UI
* **Produtividade:** Lombok (Opcional, redução de boilerplate)
* **Automação & Build:** Maven Wrapper (`./mvnw`)
* **Containerização:** Docker & Docker-Compose (v3.9)
* **Orquestração de Infraestrutura:** Kubernetes (Manifestos YAML + Kustomization)

---

## 🏗️ Estrutura de Código-Fonte (Arquitetura)

A API segue rigorosamente o padrão arquitetural em camadas (Controller-Service-Repository) facilitando a manutenibilidade, isolamento de escopo e facilidade na escrita de testes.

```text
src/main/java/
└── com/endriw/financesync/
    ├── Application.java               # Inicializador do Spring Boot
    ├── config/                        # Configurações globais (SwaggerConfig, etc.)
    ├── controller/                    # Controladores REST (Exposição de Endpoints)
    ├── dto/                           # Data Transfer Objects (Request/Response Payloads)
    ├── exception/                     # Captura global de erros (GlobalExceptionHandler)
    ├── model/                         # Entidades de Domínio JPA
    │   └── enums/                     # Mapeamento de Estados (UserRole, TransactionType, etc.)
    ├── repository/                    # Interfaces de Comunicação com o PostgreSQL (JPA Repositories)
    ├── security/                      # Configuração de Filtros Stateless, JWT Service e Criptografia
    └── service/                       # Camada de Serviços Contendo as Regras de Negócio Core
src/test/java/                         # Suíte de Testes Unitários e Integrados (JUnit 5 / Mockito)
src/main/resources/
    ├── application.properties         # Configurações do Spring (Não versionado - Sobrescrito em produção)
    └── application.properties.example # Arquivo de exemplo com as chaves necessárias

```

---

## 📡 Endpoints REST

Todas as rotas, exceto as localizadas em `/auth/`, exigem obrigatoriamente a passagem do token Jwt no cabeçalho HTTP da requisição:
`Authorization: Bearer <seu-jwt-token>`

### 🔑 Autenticação

* `POST /auth/register` → Registra um novo usuário no sistema.
* `POST /auth/login` → Valida as credenciais e retorna o Token JWT de acesso.

### 💳 Contas Bancárias

* `GET /accounts` → Lista todas as contas pertencentes ao usuário autenticado.
* `GET /accounts/{id}` → Busca os detalhes de uma conta específica por ID.
* `POST /accounts` → Cria uma nova conta bancária (Corrente, Poupança, Carteira).
* `PUT /accounts/{id}` → Atualiza os dados de uma conta existente.
* `DELETE /accounts/{id}` → Remove permanentemente a conta informada.

### 🏷️ Categorias

* `GET /categories` → Lista as categorias personalizadas criadas pelo usuário.
* `GET /categories/{id}` → Detalha uma categoria por ID.
* `POST /categories` → Cadastra uma nova categoria de transação.
* `PUT /categories/{id}` → Atualiza uma categoria existente.
* `DELETE /categories/{id}` → Remove uma categoria.

### 💸 Transações

* `GET /transactions` → Lista o histórico financeiro completo do usuário.
* `GET /transactions/{id}` → Detalha uma transação específica por ID.
* `POST /transactions` → Efetua o registro de uma nova movimentação de entrada ou saída.
* `PUT /transactions/{id}` → Altera os dados de uma transação.
* `DELETE /transactions/{id}` → Remove uma transação financeira.

---

## ▶️ Como Executar o Projeto

Escolha a abordagem que melhor se adapta ao seu cenário atual de desenvolvimento ou deploy.

### 1️⃣ Ambiente Local Automatizado (Recomendado via Docker Compose)

Esta abordagem provisiona a aplicação e o banco de dados PostgreSQL instantaneamente sem a necessidade de instalar dependências locais no sistema.

```bash
# 1. Clone o repositório
git clone [https://github.com/EndriwEngSoft/financesync-api.git](https://github.com/EndriwEngSoft/financesync-api.git)
cd financesync-api

# 2. Configure as variáveis de ambiente baseando-se no modelo
cp .env.example .env
# Abra o arquivo .env com seu editor de texto e ajuste as credenciais (DB, JWT_SECRET, etc)

# 3. Suba todo o ambiente em modo de segundo plano (detached)
docker compose up -d

# 4. Monitore a inicialização completa dos containers e health checks
docker compose ps

```

### 2️⃣ Ambiente de Desenvolvimento (Spring Boot Direto)

Caso queira debugar o código em sua IDE favorita (IntelliJ, Eclipse, VS Code):

* **Pré-requisito:** Ter uma instância do PostgreSQL rodando localmente (na porta `5432`). Você pode usar apenas o container do banco do compose executando `docker compose up db -d`.

```bash
# Crie o banco de dados necessário caso esteja usando PostgreSQL nativo
psql -U postgres -c "CREATE DATABASE financesync_db;"

# Configure o arquivo properties local
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Ajuste as chaves de acesso do banco e segredos do JWT no arquivo copiado

# Execute o comando de boot do Maven
./mvnw spring-boot:run

```

### 3️⃣ Ambiente de Orquestração (Kubernetes - K8s)

O projeto está pronto para rodar em clusters locais (como Docker Desktop K8s, Minikube, Kind) ou ambientes de Cloud Providers de produção.

```bash
# 1. Crie o arquivo secret do K8s a partir do template (este arquivo é ignorado no git por segurança)
cp k8s/secret.yaml.example k8s/secret.yaml
# Insira os valores em Base64 correspondentes aos seus segredos de produção no secret.yaml

# 2. Aplique as definições declarativas usando o Kustomize nativo do kubectl
kubectl apply -k k8s/

# 3. Verifique a integridade e inicialização dos Pods e Services no Namespace dedicado
kubectl get pods -n financesync
kubectl get svc -n financesync

```

*Acessível localmente via NodePort configurado em `http://localhost:30000` (conforme configurações do manifesto).*

---

## 🔒 Arquitetura de Segurança

A proteção do sistema é estrita e segue os padrões recomendados da indústria:

* **Filtros Inteligentes:** Implementação do `OncePerRequestFilter` interceptando as requisições HTTP, validando a assinatura criptográfica e tempo de expiração do Token JWT antes de dar acesso ao contexto de segurança do Spring.
* **Senhas Blindadas:** Processo adaptável de hash de senhas utilizando o algoritmo **BCrypt** dentro da implementação customizada do `UserDetailsService`.
* **Segurança Stateless:** O servidor não armazena sessões em memória, garantindo alta escalabilidade horizontal do backend.

---

## 🧪 Suíte de Testes

O projeto utiliza pirâmide de testes focada em qualidade de entrega contínua. Para rodar a suíte completa de testes e verificar a estabilidade do código:

```bash
./mvnw test

```

*Os relatórios gerados detalhadamente podem ser encontrados no diretório consolidado `target/surefire-reports/`.*

---

## 📦 Build Otimizado da Imagem Docker

O arquivo `Dockerfile` do projeto foi arquitetado em duas fases distintas (*Multi-Stage Build*):

1. **Fase Builder (Compilação):** Utiliza uma imagem completa do Maven para compilar as classes, rodar validações e empacotar o executável `.jar`.
2. **Fase Runtime (Execução):** Descarta o peso do Maven e do código fonte, extraindo apenas o artefato gerado para uma imagem enxuta contendo apenas o JRE do OpenJDK. O resultado é uma imagem de produção de alta performance com tamanho reduzido para cerca de **~135MB** (medido via `docker images` sobre `eclipse-temurin:21-jre-alpine`).

---

## 📄 Documentação da API

Após inicializar a aplicação com sucesso através de qualquer um dos métodos descritos acima, você pode explorar e realizar requisições de testes diretamente nos contratos expostos nos seguintes endereços:

* **Interface Interativa (Swagger UI):** `http://localhost:8080/api/swagger-ui/index.html`
* **OpenAPI Definições JSON:** `http://localhost:8080/api/v3/api-docs`
* **OpenAPI Definições YAML:** `http://localhost:8080/api/v3/api-docs.yaml`

> O prefixo `/api` vem de `server.servlet.context-path`, que é `/api` por padrão (veja `application.properties.example`) em qualquer um dos 3 modos de execução acima.

---

## 👨‍💻 Autor

**Endriw Colvara Bento**
