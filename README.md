# 🏦 Banking Microservices Platform

[![CI — Build & Test](https://github.com/TonyKerr07/banking-microservices-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/TonyKerr07/banking-microservices-platform/actions/workflows/ci.yml)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=flat&logo=spring&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2023.0.1-6DB33F?style=flat&logo=spring)](https://spring.io/projects/spring-cloud)
[![Maven](https://img.shields.io/badge/Maven-Multi--Module-C71A36?style=flat&logo=apache-maven)](https://maven.apache.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat&logo=docker&logoColor=white)](https://docs.docker.com/compose/)

Plataforma bancária de microsserviços desenvolvida em **Java 21 + Spring Boot 3.2.5** como projeto de portfólio de nível sênior. Cobre os principais domínios de um banco digital: **contas, transferências, boletos e PIX**, roteados por um **API Gateway** com autenticação JWT e circuit breaker.

---

## 🧩 Módulos

| Módulo              | Porta  | Responsabilidade                                                      |
|---------------------|--------|-----------------------------------------------------------------------|
| `common`            | —      | Exceções, DTOs, GlobalExceptionHandler via Spring Auto-Configuration  |
| `accounts-service`  | 8081   | CRUD de contas, saldo com Redis cache, Flyway migrations              |
| `transfers-service` | 8082   | Transferências internas/TED/DOC, validação de conta via RestClient    |
| `boletos-service`   | 8083   | Emissão, pagamento, cancelamento + job diário de boletos vencidos     |
| `pix-service`       | 8084   | Chaves PIX (regra BACEN 5 chaves), transações PIX, E2EID, soft delete |
| `api-gateway`       | 8080   | Roteamento, JWT auth, CORS global, Circuit Breaker, Swagger agregado  |

---

## 🛠️ Stack Técnico

| Categoria       | Tecnologia                                              |
|-----------------|---------------------------------------------------------|
| Linguagem       | Java 21 (Virtual Threads, Records, Text Blocks)         |
| Framework       | Spring Boot 3.2.5, Spring Cloud 2023.0.1                |
| Gateway         | Spring Cloud Gateway (WebFlux/reactive)                 |
| Segurança       | Spring Security WebFlux + JWT HS256 (jjwt 0.12.5)       |
| Resiliência     | Resilience4j Circuit Breaker                            |
| ORM             | Spring Data JPA + Hibernate 6                           |
| Migrations      | Flyway (H2 em dev/test, PostgreSQL em prod)             |
| Mapeamento      | MapStruct 1.5.5 (compile-time, zero reflection)         |
| Cache           | Redis 7 (Spring Cache, @Cacheable/@CacheEvict)          |
| HTTP Client     | Spring 6.1 RestClient (inter-service)                   |
| Documentação    | SpringDoc / Swagger UI (agregado no gateway)            |
| Testes          | JUnit 5 + Mockito (35+ cenários) + Testcontainers       |
| Observabilidade | Micrometer + Prometheus + Grafana                       |
| Build           | Maven multi-módulo + OpenAPI Generator (contract-first) |
| Infra           | Docker, Docker Compose, GitHub Actions CI               |

---

## ✅ Pré-requisitos

| Ferramenta     | Versão mínima   |
|----------------|-----------------|
| JDK            | 21              |
| Maven          | 3.9+            |
| Docker Desktop | 24+             |
| Docker Compose | v2              |

---

## 🚀 Como Executar

### Opção 1 — Docker Compose (stack completa)

```bash
# Clone o repositório
git clone https://github.com/TonyKerr07/banking-microservices-platform.git
cd banking-microservices-platform

# Build dos JARs
mvn -B clean package -DskipTests

# Sobe tudo: Postgres + Redis + todos os serviços + Prometheus + Grafana
docker-compose up --build

# Para parar
docker-compose down

# Para resetar os bancos
docker-compose down -v
```

### Opção 2 — Local com H2 (desenvolvimento rápido)

```bash
# Gera código dos contratos OpenAPI (obrigatório antes de compilar)
mvn -B clean generate-sources

# Roda o accounts-service localmente (H2 em memória)
mvn -pl accounts-service -am spring-boot:run

# Build completo com todos os testes
mvn -B clean install

# Testes de integração com Testcontainers (requer Docker)
mvn -pl accounts-service test -Dgroups="integration"
```

---

## 🔐 Autenticação JWT

Todas as rotas (exceto `/api/v1/auth/**` e `/actuator/**`) exigem Bearer token.

```bash
# 1. Obter token
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"antonio","password":"banking@2024"}'

# Resposta:
# {"accessToken":"eyJ...","tokenType":"Bearer","expiresIn":86400000}

# 2. Usar em todas as requisições
curl http://localhost:8080/api/v1/accounts \
  -H "Authorization: Bearer eyJ..."

# 3. Sem token retorna 401
curl http://localhost:8080/api/v1/accounts
```

| Usuário   | Senha          |
|-----------|----------------|
| `antonio` | `banking@2024` |
| `admin`   | `admin123`     |

---

## 📖 Documentação das APIs

| Serviço                     | Swagger UI                            |
|-----------------------------|---------------------------------------|
| Gateway (todos os serviços) | http://localhost:8080/swagger-ui.html |
| Accounts                    | http://localhost:8081/swagger-ui.html |
| Transfers                   | http://localhost:8082/swagger-ui.html |
| Boletos                     | http://localhost:8083/swagger-ui.html |
| PIX                         | http://localhost:8084/swagger-ui.html |

> **Dica Postman:** Import → cole `http://localhost:8081/v3/api-docs` e a coleção é gerada automaticamente.

---

## 🧪 Como Testar as Funcionalidades

### Fluxo completo via curl (Gateway na porta 8080)

```bash
# Defina o token após o login
TOKEN="Bearer SEU_TOKEN_AQUI"

# 1. Criar conta origem
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"holderName":"João Silva","documentNumber":"12345678901","accountType":"CHECKING","initialBalance":5000.00}'

# 2. Criar conta destino
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"holderName":"Maria Souza","documentNumber":"98765432100","accountType":"SAVINGS","initialBalance":1000.00}'

# 3. Consultar saldo (cacheado no Redis por 30s)
curl http://localhost:8080/api/v1/accounts/{ID_ORIGEM}/balance \
  -H "Authorization: $TOKEN"

# 4. Fazer transferência
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sourceAccountId":"{ID_ORIGEM}","targetAccountId":"{ID_DESTINO}","amount":250.00,"description":"Pagamento","transferType":"INTERNAL"}'

# 5. Registrar chave PIX
curl -X POST http://localhost:8080/api/v1/pix/keys \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"accountId":"{ID_ORIGEM}","keyType":"EMAIL","keyValue":"joao@email.com"}'

# 6. Enviar PIX
curl -X POST http://localhost:8080/api/v1/pix/transactions \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"sourceAccountId":"{ID_DESTINO}","targetPixKey":"joao@email.com","amount":100.00,"description":"PIX teste"}'

# 7. Emitir boleto
curl -X POST http://localhost:8080/api/v1/boletos \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"payerAccountId":"{ID_ORIGEM}","beneficiaryName":"Empresa XPTO","amount":350.00,"dueDate":"2026-12-31","description":"Fatura 001"}'

# 8. Pagar boleto
curl -X POST http://localhost:8080/api/v1/boletos/{ID_BOLETO}/pay \
  -H "Authorization: $TOKEN"
```

### Testar Circuit Breaker

```bash
# Para o accounts-service
docker-compose stop accounts-service

# Próxima requisição retorna 503 estruturado
curl http://localhost:8080/api/v1/accounts \
  -H "Authorization: $TOKEN"
# {"errorCode":"CIRCUIT_BREAKER_OPEN","message":"accounts-service is temporarily unavailable..."}

# Reinicia o serviço — circuit volta ao normal após 10s
docker-compose start accounts-service
```

### Acessar Observabilidade
Prometheus:  http://localhost:9091
Grafana:     http://localhost:3000  (admin / banking123)
Adminer:     http://localhost:9090  (banking_user / banking_pass / servidor: postgres)

No Grafana: Dashboards → Import → ID `4701` (JVM Micrometer da comunidade).

---

## 📋 Contratos OpenAPI (Contract-First)

O YAML é a fonte da verdade. O código gerado nunca vai para o Git.
accounts-service/src/main/resources/openapi/accounts-api.yaml
transfers-service/src/main/resources/openapi/transfers-api.yaml
boletos-service/src/main/resources/openapi/boletos-api.yaml
pix-service/src/main/resources/openapi/pix-api.yaml

```bash
# Gerar código de um serviço específico
mvn -pl accounts-service -am clean generate-sources

# No CI: mvn clean install já executa generate-sources automaticamente
```

---

## 🌿 Git Workflow
main        ← produção (protegida, merge via PR)
└── develop ← integração
├── feat/accounts-create-account
├── fix/transfer-balance-validation
└── chore/update-dependencies

Padrão de commits (Conventional Commits):
feat(accounts): add Account entity and repository
fix(pix): correct key lookup for deleted keys
test(transfers): add integration test with Testcontainers
docs(readme): update getting started instructions
chore(deps): bump spring-boot to 3.2.5

---

## 🗺️ Roadmap

- [x] Estrutura multi-módulo Maven (6 módulos)
- [x] Contract-first com OpenAPI Generator
- [x] Flyway migrations (H2 dev / PostgreSQL prod)
- [x] GlobalExceptionHandler via Spring Auto-Configuration
- [x] Docker Compose (Postgres + Redis + todos os serviços)
- [x] GitHub Actions CI (build + contract validation + docker build)
- [x] 35+ testes unitários (JUnit 5 + Mockito)
- [x] RestClient inter-serviço (transfers e pix validam accounts)
- [x] Spring Security + JWT no API Gateway
- [x] Testcontainers (testes de integração com PostgreSQL real)
- [x] Resilience4j Circuit Breaker com fallback estruturado
- [x] Job agendado (boletos vencidos — @Scheduled)
- [x] Observabilidade: Micrometer + Prometheus + Grafana
- [x] Cache Redis (saldo e dados de conta)
- [ ] Kafka + Outbox Pattern (eventos de transação)
- [ ] Spring Cloud Contract (consumer-driven contract tests)
- [ ] Deploy Railway (demo público)

---

*Desenvolvido por [Antonio](https://github.com/TonyKerr07) — portfólio de arquitetura de microsserviços bancários em Java 21 + Spring Boot 3.*