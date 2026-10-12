# 🛡️ Harness Engineering & Validação Automatizada de Specs

> **Documento:** Guia Técnico do Test Harness e Validação Contínua  
> **Referência:** Seção 9 do [Guia de Aprendizado SDD](file:///home/diegosneves/Github/pessoal/java/payment-system/guia-spec-driven-development-sdd.md)  
> **Status:** Ativo

---

## 1. O Conceito de Harness Engineering no SDD

No desenvolvimento orientado por especificações com auxílio de Inteligência Artificial, **o maior erro é deixar o agente de IA livre no repositório sem grades de proteção**. 

Conforme estabelecido pela engenharia moderna da SoftDesign e frameworks como Tessl e Kiro:
> **A *Spec* é o mapa que direciona o agente; o *Harness* é o trilho que impede o agente de introduzir regressões, falhas de segurança ou violações de arquitetura.**

```
 ┌────────────────────────────────────────────────────────┐
 │                   O CICLO DO HARNESS                   │
 │                                                        │
 │   1. Especificação Viva (/spec)                        │
 │           │                                            │
 │           ▼ (Prompt Estruturado / Intenção)            │
 │   2. Agente de IA / Engenheiro gera Código             │
 │           │                                            │
 │           ▼                                            │
 │   3. EXECUÇÃO DO TEST HARNESS                          │
 │      ├── ArchUnit (Regras de Arquitetura)              │
 │      ├── Mockito / Unit Tests                          │
 │      ├── Testcontainers (MySQL 8 Real)                 │
 │      └── MockMvc (Contratos REST)                      │
 │           │                                            │
 │      [ Passou? ]                                       │
 │       ├── SIM ──> PR Aprovado / Deploy                 │
 │       └── NÃO ──> Self-Healing: Harness devolve erro   │
 │                   + Spec ao Agente para auto-correção  │
 └────────────────────────────────────────────────────────┘
```

---

## 2. A Camada de Harness do `payment-system`

O repositório `payment-system` possui um **Harness de nível avançado (Nível 4/5 de maturidade)**, estruturado em três níveis de isolamento:

### 2.1 Camada 1: Unit Harness (Isolamento Rápido de Regras de Negócio)
- **Tecnologias:** JUnit 5 (`junit-jupiter`), Mockito (`mockito-core`, `mockito-junit-jupiter`), AssertJ.
- **Classes:**
  - `PaymentLimitValidatorTest`: Validação determinística das regras de limites (RN-02 e RN-03 da SPEC-001) cobrindo limites máximos, valores negativos, nulos e valores de borda (`1999.99`, `2000.00`, `2000.01`).
  - `PaymentServiceTest`: Validação com mocks das regras de transição de status (SPEC-003) e do fluxo transacional sem tocar em banco de dados.

### 2.2 Camada 2: Persistence Harness com Testcontainers (Validação Real de SQL)
- **Tecnologias:** `@DataJpaTest`, `Testcontainers MySQL 8.2.0`, `TestEntityManager`.
- **Classe:** `PaymentRepositoryTest`
- **Garantia:** Em vez de usar H2 em memória (que mascara comportamentos do MySQL como `COALESCE`, agregações de data e fusos horários), o harness sobe uma instância real do MySQL em Docker para garantir que a agregação:
  ```sql
  SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.payerId = :payerId AND p.createdAt BETWEEN :start AND :end
  ```
  seja validada exatamente como roda em produção.

### 2.3 Camada 3: Integration Harness (Validação E2E de Contratos HTTP)
- **Tecnologias:** `@SpringBootTest`, `@AutoConfigureMockMvc`, `Testcontainers MySQL`, `ObjectMapper`.
- **Classe:** `PaymentIntegrationTest`
- **Garantia:** Valida os contratos OpenAPI/JSON Schema definidos em `SPEC-001`, `SPEC-002` e `SPEC-003`, assegurando que:
  - Criações retornem `201 Created` com payload serializado no fuso `America/Sao_Paulo`.
  - Violações retornem `422 Unprocessable Entity` com schema `ApiError`.
  - Identificadores malformados recebam `400 Bad Request`.
  - Recursos inexistentes recebam `404 Not Found`.

### 2.4 Camada 4: Synthetic Data Fixtures
- **Tecnologia:** DataFaker + `Fixture.java`
- **Papel:** Gera payloads válidos, randômicos e reprodutíveis para simulação contínua sem depender de dados de produção.

---

## 3. O Fluxo Inverso: Como Chegamos Aqui

Para demonstrar a eficácia do SDD, aqui está a cronologia inversa de como este projeto teria sido gerado a partir das especificações:

```
Passo 1: Escrita da Intenção e Especificações
  ├── Redação de SPEC-001, SPEC-002, SPEC-003, SPEC-004
  └── Revisão por Product e Tech Lead

Passo 2: Geração do Scaffold de Contratos
  ├── IA gera DTOs Records: PaymentRequest, PaymentResponse, UpdatePaymentStatusRequest
  └── IA gera Model JPA: Payment com @PrePersist e Enums (PaymentStatus, PaymentSource)

Passo 3: Geração do Test Harness Primeiro (Spec-to-Test)
  ├── A partir dos cenários BDD da SPEC-001 -> Gerado PaymentLimitValidatorTest
  ├── A partir dos cenários da SPEC-003 -> Gerado PaymentServiceTest (validação de estados)
  └── A partir dos contratos REST -> Gerado PaymentIntegrationTest

Passo 4: Geração da Implementação (Test-Driven via Spec)
  ├── Implementação de PaymentLimitValidator até passar nos testes unitários
  ├── Implementação de PaymentService e regras transacionais
  ├── Implementação de GlobalExceptionHandler para mapear exceções em ApiError (400, 404, 422)
  └── Implementação de PaymentController com anotações OpenAPI Swagger

Passo 5: Integração de IA & RAG (SPEC-004)
  ├── Configuração de RagConfiguration com LangChain4j + Ollama
  └── Criação do TravelAgentAssistant e TravelAgentController
```

---

## 4. Como Executar a Validação do Harness Localmente

Para validar se o código do projeto cumpre 100% das especificações do diretório `/spec`:

```bash
# 1. Executar todos os testes do Harness (requer Docker para Testcontainers MySQL):
./mvnw clean test

# 2. Executar apenas os testes de validação de regras de negócio:
./mvnw test -Dtest=PaymentLimitValidatorTest,PaymentServiceTest

# 3. Executar o harness de integração de contratos REST:
./mvnw test -Dtest=PaymentIntegrationTest
```

---

## 5. Práticas de Self-Healing em Pipelines CI/CD

Quando este projeto for integrado a agentes autônomos de desenvolvimento:
1. O agente recebe uma alteração de requisito em uma das especificações (ex: novo meio de pagamento `CRYPTOCURRENCY` na `SPEC-001`).
2. O agente atualiza primeiro a `SPEC-001`.
3. O agente executa o harness: os testes quebram.
4. O agente altera o enum `PaymentSource`, os schemas e os testes.
5. O harness passa com 100% de sucesso.
6. O PR é submetido contendo a **Spec atualizada + Código correspondente + Testes correspondentes**.
