# SPEC-001: Criação de Pagamento e Controle de Limite Diário

- **Versão:** 1.0.0
- **Squad / Domínio:** Core Banking / Payments
- **Status:** Implemented
- **Data de Criação:** 2026-10-12
- **Última Atualização:** 2026-10-12
- **Autores / Responsáveis:** Diego Neves

---

## 1. Intenção & Contexto de Negócio

### 1.1 Objetivo
Permitir a criação segura de pagamentos no sistema via múltiplos canais (`PIX`, `DEBIT_CARD`, `CREDIT_CARD`), estabelecendo uma grade de proteção financeira baseada em teto de gastos diários por pagador para mitigar riscos de fraude e inadimplência.

### 1.2 Problema Resolvido
Em sistemas de transações financeiras, a criação descontrolada de débitos e transferências pode gerar exposições a fraudes ou estouro de limites operacionais. Esta especificação formaliza os requisitos de validação prévia de valores e controle cumulativo de limite diário por pagador no fuso horário brasileiro.

### 1.3 Escopo & Limites
- **Dentro do escopo:**
  - Validação estrita de formato e positividade do montante financeiro.
  - Validação cumulativa do limite diário de até **R$ 2.000,00** por pagador.
  - Persistência com estado inicial `PENDING` e timestamp auditável.
- **Fora do escopo:**
  - Liquidação bancária assíncrona externa (Webhooks do Banco Central / Adquirente).
  - Análise antifraude por biometria comportamental.

---

## 2. Contratos Técnicos

### 2.1 Interface REST
- **Método HTTP:** `POST`
- **Rota:** `/api/payments`
- **Consumes:** `application/json`
- **Produces:** `application/json`

### 2.2 Contrato de Entrada (JSON Schema / Request)
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["payerId", "paymentSource", "amount"],
  "properties": {
    "payerId": {
      "type": "string",
      "format": "uuid",
      "description": "Identificador único do pagador (UUID RFC 4122)"
    },
    "paymentSource": {
      "type": "string",
      "enum": ["PIX", "DEBIT_CARD", "CREDIT_CARD"],
      "description": "Canal / método de pagamento escolhido"
    },
    "amount": {
      "type": "number",
      "minimum": 0.01,
      "description": "Valor monetário da transação em Reais (BRL), estritamente positivo"
    }
  }
}
```

*Representação Java Record correspondente:*
```java
public record PaymentRequest(
    @NotNull(message = "Payer ID is required") UUID payerId,
    @NotNull(message = "Payment source is required") PaymentSource paymentSource,
    @NotNull(message = "Amount is required") @Positive(message = "Amount must be positive") BigDecimal amount
) {}
```

### 2.3 Contrato de Saída (Sucesso - HTTP 201 Created)
```json
{
  "type": "object",
  "required": ["id", "payerId", "paymentSource", "amount", "status", "createdAt"],
  "properties": {
    "id": { "type": "integer", "example": 1 },
    "payerId": { "type": "string", "format": "uuid" },
    "paymentSource": { "type": "string", "enum": ["PIX", "DEBIT_CARD", "CREDIT_CARD"] },
    "amount": { "type": "number", "example": 100.00 },
    "status": { "type": "string", "example": "PENDING" },
    "createdAt": { "type": "string", "format": "date-time", "example": "2026-10-12T17:30:00" }
  }
}
```

### 2.4 Respostas de Erro Padronizadas (`ApiError`)
| HTTP Status | Causa | Formato do Payload |
| :--- | :--- | :--- |
| `400 Bad Request` | Falha de validação Bean Validation (campos nulos/inválidos) | `ApiError` com erros descritivos |
| `422 Unprocessable Entity` | Montante <= 0 (`PaymentLimitException`) | `{"message": "Amount must be greater than zero", "errors": [...]}` |
| `422 Unprocessable Entity` | Limite diário excedido (`PaymentLimitException`) | `{"message": "Daily payment limit exceeded for source: [SOURCE]", "errors": [...]}` |
| `500 Internal Server Error` | Erro de infraestrutura não controlado | `ApiError` com detalhes da exceção |

---

## 3. Regras de Negócio & Invariantes

- **RN-01 [Fontes Suportadas]:** Os meios aceitos são estritamente `PIX`, `DEBIT_CARD` e `CREDIT_CARD`. Qualquer outro valor deve ser rejeitado na deserialização.
- **RN-02 [Positividade do Valor]:** O valor `amount` não pode ser nulo, zero ou negativo. Caso seja `<= 0`, o sistema lança `PaymentLimitException` com a mensagem `"Amount must be greater than zero"` (HTTP 422).
- **RN-03 [Teto Máximo Diário de R$ 2.000,00]:**
  - Cada pagador possui uma quota cumulativa máxima de **R$ 2.000,00** por dia civil.
  - O cálculo considera todos os pagamentos já criados no intervalo entre `00:00:00` e `23:59:59.999` do dia corrente no fuso horário oficial `America/Sao_Paulo`.
  - Se `(Soma do dia + Novo Valor) > 2000.00`, a transação é abortada e lança-se `PaymentLimitException` com a mensagem `"Daily payment limit exceeded for source: %s".formatted(paymentSource)` (HTTP 422).
  - Transações que somem exatamente R$ 2.000,00 no dia são aceitas.
- **RN-04 [Estado Inicial PENDING]:** Todo pagamento criado é obrigatoriamente inicializado com status `PENDING`.
- **RN-05 [Auditoria Temporal Automática]:** O campo `createdAt` deve ser gravado automaticamente no callback JPA `@PrePersist` baseado no instante atual, sendo convertido para a zona horária de São Paulo ao serializar para o cliente.
- **RN-06 [Transacionalidade]:** A validação e o salvamento devem ocorrer sob uma transação atômica (`@Transactional`).

---

## 4. Critérios de Aceitação (Given-When-Then / BDD)

### Cenário 1: Criação de pagamento com sucesso abaixo do limite
- **Dado que** o pagador `739e5f1e-2459-499d-9cdd-ab59b31c02d9` não possui pagamentos hoje,
- **Quando** enviar uma requisição `POST /api/payments` com `paymentSource=PIX` e `amount=100.00`,
- **Então** o sistema deve responder com HTTP 201 Created,
- **E** o corpo da resposta deve conter `id` não nulo, `status=PENDING`, `amount=100.00` e timestamp `createdAt`.

### Cenário 2: Criação bem-sucedida atingindo exatamente o teto de R$ 2.000,00
- **Dado que** o pagador possui R$ 1.500,00 em pagamentos no dia de hoje,
- **Quando** enviar uma requisição `POST /api/payments` com `amount=500.00`,
- **Então** a transação deve ser aprovada com HTTP 201 Created.

### Cenário 3: Rejeição ao ultrapassar o teto diário cumulativo
- **Dado que** o pagador possui R$ 1.901,00 em pagamentos no dia de hoje,
- **Quando** enviar uma requisição `POST /api/payments` com `amount=100.00` via `PIX`,
- **Então** o sistema não deve persistir o novo pagamento,
- **E** deve retornar HTTP 422 Unprocessable Entity com a mensagem `"Daily payment limit exceeded for source: PIX"`.

### Cenário 4: Rejeição de transação unitária acima de R$ 2.000,00
- **Dado que** o pagador não possui nenhum pagamento anterior,
- **Quando** tentar criar um pagamento de `amount=2001.00`,
- **Então** o sistema deve retornar HTTP 422 Unprocessable Entity com `"Daily payment limit exceeded for source: [SOURCE]"`.

### Cenário 5: Rejeição de valor zero ou negativo
- **Dado que** uma requisição contenha `amount=0.00` ou `amount=-50.00`,
- **Quando** o processamento for iniciado,
- **Então** o validador deve disparar `PaymentLimitException` com a mensagem `"Amount must be greater than zero"` (HTTP 422).

### Cenário 6: Pagamentos de dias anteriores não afetam o teto diário de hoje
- **Dado que** o pagador realizou pagamentos de R$ 2.000,00 ontem,
- **Quando** realizar um novo pagamento de R$ 100,00 hoje,
- **Então** a transação deve ser aprovada com sucesso com HTTP 201 Created.

---

## 5. Rastreabilidade com Código e Test Harness

### Código de Produção
- `br.com.neves.paymentsystem.controllers.PaymentController#createPayment`
- `br.com.neves.paymentsystem.services.PaymentService#createPayment`
- `br.com.neves.paymentsystem.validators.PaymentLimitValidator#isWithinLimit`
- `br.com.neves.paymentsystem.validators.PaymentLimitValidator#validateAmount`
- `br.com.neves.paymentsystem.repository.PaymentRepository#sumPaymentsByPayerIdAndDate`
- `br.com.neves.paymentsystem.model.Payment`

### Testes Automatizados (Harness de Validação)
- `PaymentLimitValidatorTest`: Validação unitária de valores zerados, negativos, limites estritos e valores de borda (1999.99, 2000.00, 2000.01).
- `PaymentServiceTest#shouldSavePaymentWhenLimitIsNotExceeded`: Teste unitário de mock do serviço com verificação de argumentos capturados (`payerCaptor`).
- `PaymentServiceTest#shouldThrowExceptionWhenLimitIsExceeded`: Teste de rejeição de limite diário.
- `PaymentRepositoryTest#shouldSumDailyPaymentsByPayerId`: Teste com MySQL Testcontainers validando a agregação SQL no intervalo de datas.
- `PaymentRepositoryTest#shouldNotSumPaymentsFromDifferentDay`: Teste provando que pagamentos do dia anterior são ignorados.
- `PaymentIntegrationTest#shouldReturnPaymentResponseWithStatusCreatedWhenPaymentIsCreatedWithSuccess`: Teste de integração end-to-end com MockMvc e Testcontainers.
- `PaymentIntegrationTest#shouldThrowPaymentLimitExceptionWhenPaymentAmountIsGreaterThanLimit`: Teste de integração end-to-end do fluxo de erro HTTP 422.
