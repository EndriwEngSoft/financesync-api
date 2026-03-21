# 💰 FinanceSync API

API REST para gerenciamento financeiro pessoal com autenticação JWT, controle de contas bancárias, categorias e transações.

## 📌 Sobre o Projeto

Aplicação backend desenvolvida com Spring Boot que permite aos usuários gerenciar suas finanças pessoais de forma segura. Cada usuário gerencia suas próprias contas, categorias e transações após autenticação via JWT, com validação de dados em todas as entradas.

## ✨ Funcionalidades

- Autenticação e registro de usuários com JWT
- Gerenciamento de contas bancárias (corrente, poupança, carteira)
- Categorização de transações personalizadas por usuário
- Registro de transações financeiras com tipo, método de pagamento e taxas
- Validação de dados com Bean Validation
- Tratamento de erros centralizado com @RestControllerAdvice
- Documentação interativa com Swagger/OpenAPI

## 🛠️ Tecnologias

| Tecnologia | Versão |
|---|---|
| Java | 21+ |
| Spring Boot | 4.0.3 |
| Spring Security | 7.x |
| Spring Data JPA | - |
| PostgreSQL | 18 |
| JWT (jjwt) | 0.12.6 |
| Bean Validation | - |
| Swagger/OpenAPI | 3.0.2 |
| Maven | - |

## 🏗️ Arquitetura
```
src/main/java/
└── com/endriw/financesync/
    ├── config/           # Configurações (Swagger)
    ├── controller/       # Controllers REST
    ├── service/          # Regras de negócio
    ├── repository/       # Interfaces JPA Repository
    ├── model/            # Entidades JPA e Enums
    │   └── enums/        # Enumerações
    ├── dto/              # Data Transfer Objects
    ├── security/         # JWT, Filtros e configuração de segurança
    └── exception/        # Handler global de exceções
```

## 📡 Endpoints
```
POST   /auth/register         → Registra novo usuário
POST   /auth/login            → Realiza login e retorna token JWT

GET    /accounts              → Lista contas do usuário
GET    /accounts/{id}         → Busca conta por ID
POST   /accounts              → Cria nova conta bancária
PUT    /accounts/{id}         → Atualiza conta
DELETE /accounts/{id}         → Remove conta

GET    /categories            → Lista categorias do usuário
GET    /categories/{id}       → Busca categoria por ID
POST   /categories            → Cria nova categoria
PUT    /categories/{id}       → Atualiza categoria
DELETE /categories/{id}       → Remove categoria

GET    /transactions          → Lista transações do usuário
GET    /transactions/{id}     → Busca transação por ID
POST   /transactions          → Registra nova transação
PUT    /transactions/{id}     → Atualiza transação
DELETE /transactions/{id}     → Remove transação
```

## ▶️ Como Executar

**Pré-requisitos:** PostgreSQL rodando localmente na porta 5432
```bash
# Clone o repositório
git clone https://github.com/EndriwEngSoft/financesync-api.git

# Entre na pasta
cd financesync-api

# Crie o banco de dados
psql -U postgres -c "CREATE DATABASE financesync_db;"

# Crie o arquivo application.properties em src/main/resources/
# (use o application.properties.example como base)

# Execute
./mvnw spring-boot:run
```

Acesse a documentação: `http://localhost:8080/swagger-ui/index.html`

## 🔒 Autenticação

Todas as rotas exceto `/auth/**` requerem token JWT no header:
```
Authorization: Bearer {token}
```

## 🧠 Conceitos Aplicados

- Autenticação stateless com Spring Security e JWT
- Filtro de requisições com `OncePerRequestFilter`
- Relacionamentos JPA: `@ManyToOne` e `@JoinColumn`
- Navegação de relacionamentos no Spring Data (`findByAccountUser`)
- Validação de entrada com Bean Validation (`@NotBlank`, `@Email`, `@Positive`)
- Arquitetura em camadas (Controller → Service → Repository)
- Tratamento de exceções com `@RestControllerAdvice`
- Documentação automática com SpringDoc OpenAPI

## 👨‍💻 Autor

**Endriw Colvara Bento**
[![LinkedIn](https://img.shields.io/badge/LinkedIn-0077B5?style=flat&logo=linkedin&logoColor=white)](https://linkedin.com/in/endriw-bento)
[![Portfólio](https://img.shields.io/badge/Portf%C3%B3lio-000?style=flat&logo=vercel&logoColor=white)](https://portfolio-endriw.vercel.app)
