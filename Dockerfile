# ============================================
# STAGE 1: BUILD (apenas para compilar)
# ============================================
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder

# Define arg para skip tests (vai ser passado no docker build)
ARG SKIP_TESTS=true

WORKDIR /app

# Copia apenas pom.xml primeiro (Docker cache layers)
# Se dependências não mudarem, essa layer é reutilizada
# Isso evita baixar dependências toda vez que o código muda
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte
# Souce da aplicação (src) para dentro do container
COPY src/ src/

# Build da aplicação
# Se SKIP_TESTS=true, os testes não serão executados
# Isso é útil para builds de produção, onde não queremos gastar tempo rodando testes
# Se SKIP_TESTS=false, os testes serão executados, útil para builds de desenvolvimento
# O resultado do build será um JAR na pasta target/
RUN mvn clean package -DskipTests=${SKIP_TESTS}

# ============================================
# STAGE 2: RUNTIME (apenas o necessário)
# ============================================
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia APENAS o JAR da stage anterior
# A stage 1 (Maven, SDK) não vai estar na imagem final!
# Isso deixa a imagem final muito menor, apenas com o JRE e o JAR da aplicação
COPY --from=builder /app/target/financesync-*.jar app.jar

# Expõe porta (documentação, não faz nada sem -p)
EXPOSE 8080

# Health check para Kubernetes / Docker saber se está saudável
# O health check vai tentar acessar o endpoint /api/actuator/health da aplicação
# Se a aplicação não estiver saudável, o container será reiniciado
HEALTHCHECK --interval=30s --timeout=5s --start-period=10s --retries=3 \
    CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

# Executa a aplicação
ENTRYPOINT ["java", "-jar", "app.jar"]
