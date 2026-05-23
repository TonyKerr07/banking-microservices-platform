# Banking Microservices Platform

![Java](https://img.shields.io/badge/Java-21-blue)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen)
![Maven](https://img.shields.io/badge/Maven-Multi--Module-orange)
![Build](https://img.shields.io/badge/Build-Passing-success)

## 📌 Visão Geral
Este repositório apresenta a **plataforma de microsserviços bancários** construída em **Java 21 + Spring Boot 3.2.5**, organizada em um **projeto multi-módulo Maven**.  
A proposta é demonstrar **arquitetura distribuída**, boas práticas de engenharia de software e maturidade na organização de código.

---

## 🏗️ Estrutura do Projeto
banking-microservices-platform/
│
├── pom.xml (POM pai, packaging=pom)
├── common/              → Código e utilitários compartilhados
├── accounts-service/    → Microsserviço de contas bancárias
├── transfers-service/   → Microsserviço de transferências
├── boletos-service/     → Microsserviço de boletos
├── pix-service/         → Microsserviço de pagamentos instantâneos (PIX)
└── api-gateway/         → Gateway central para roteamento de requisições

---

## ⚙️ Tecnologias Utilizadas
- **[Java 21](ca://s?q=Java_21_features)**  
- **[Spring Boot 3.2.5](ca://s?q=Spring_Boot_3.2.5)**  
- **[Maven](ca://s?q=Maven_multi_module_project)** (multi-módulo, com POM pai e filhos)  
- **JUnit 5** para testes automatizados  
- **API Gateway** para orquestração de microsserviços  

---

## 📂 Estrutura Maven
- **POM Pai**  
  - Centraliza dependências e plugins.  
  - Usa BOM do Spring Boot para garantir consistência de versões.  
  - Configura compilação para **Java 21**.  

- **Módulos Filhos**  
  - Herdam configuração do pai.  
  - Cada microsserviço possui seu próprio `pom.xml` e código isolado.  
  - Dependências específicas são adicionadas apenas onde necessário.  

---

## 🚀 Como Executar
1. Clonar o repositório:
   ```bash
   git clone https://github.com/antonio/banking-microservices-platform.git
   cd banking-microservices-platform

2. Compilar todos os módulos:

mvn clean install

3. Executar um microsserviço (exemplo: accounts-service):

cd accounts-service
mvn spring-boot:run

---

🎯 Objetivos do Projeto
Demonstrar arquitetura de microsserviços em um único repositório.

Evidenciar boas práticas de Maven multi-módulo.

Evoluir diariamente com commits incrementais, documentando cada avanço.

Servir como portfólio sênior para recrutadores e empresas.

---

📅 Roadmap de Evolução
[x] Configuração inicial do repositório

[x] Criação do POM pai com boas práticas

[x] Estrutura multi-módulo definida

[ ] Implementação do accounts-service

[ ] Implementação do transfers-service

[ ] Implementação do boletos-service

[ ] Implementação do pix-service

[ ] Configuração do API Gateway

[ ] Documentação de endpoints e testes automatizados

---

🧑‍💻 Autor
Antonio — Desenvolvedor focado em arquitetura de microsserviços, Java e Spring Boot.
Este projeto é atualizado diariamente, refletindo evolução contínua e práticas de engenharia de software de nível sênior.
