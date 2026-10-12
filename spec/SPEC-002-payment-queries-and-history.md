# SPEC-002: Consultas, Listagens e Histórico de Pagamentos

- **Versão:** 1.0.0
- **Squad / Domínio:** Core Banking / Payments
- **Status:** Implemented
- **Data de Criação:** 2026-10-12
- **Última Atualização:** 2026-10-12
- **Autores / Responsáveis:** Diego Neves

---

## 1. Intenção & Contexto de Negócio

### 1.1 Objetivo
Prover interfaces de consulta eficientes e seguras para visualização de transações individuais, auditoria global de pagamentos e recuperação do histórico detalhado de operações por pagador.

### 1.2 Problema Resolvido
Clientes, operadores de suporte e sistemas integrados precisam rastrear o status e os metadados de pagamentos passados. É indispensável validar a integridade dos parâmetros de busca (como formato canônico de UUID) para evitar injeções ou pesquisas ambíguas, garantindo respostas padronizadas em caso de inexistência de registros.

### 1.3 Escopo & Limites
- **Dentro do escopo:**
  - Consulta pontual por ID sequencial do pagamento com resposta 404 padronizada.
  - Listagem completa de todos os pagamentos da base.
  - Consulta filtrada por `payerId` com validação de UUID RFC 4122 e resposta 400 em formato inválido.
- **Fora do escopo:**
  - Paginação cursor-based ou offset-based (prevista para evolução futura).
  - Filtros avançados por range de datas ou status na rota global.

---

## 2. Contratos Técnicos

### 2.1 Interface REST / Endpoints

#### Endpoint 1: Recuperação por ID
- **Método HTTP:** `GET`
- **Rota:** `/api/payments/{paymentId}`
- **Parâmetros:** `paymentId` (Path Variable, Long, obrigatório)
- **Produces:** `application/json`

#### Endpoint 2: Listagem Geral de Pagamentos
- **Método HTTP:** `GET`
- **Rota:** `/api/payments`
- **Produces:** `application/json`

#### Endpoint 3: Histórico de Pagamentos por Pagador
- **Método HTTP:** `GET`
- **Rota:** `/api/payments/payer/{payerId}`
- **Parâmetros:** `payerId` (Path Variable, String, obrigatório, UUID válido)
- **Produces:** `application/json`

---

### 2.2 Schemas de Resposta

#### Sucesso - Consulta Unitária (HTTP 200 OK)
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "integer" },
    "payerId": { "type": "string", "format": "uuid" },
    "paymentSource": { "type": "string", "enum": ["PIX", "DEBIT_CARD", "CREDIT_CARD"] },
    "amount": { "type": "number" },
    "status": { "type": "string", "enum": ["PENDING", "PAID", "FRAUD"] },
    "createdAt": { "type": "string", "format": "date-time" }
  }
}
```

#### Sucesso - Consultas em Lote / Listas (HTTP 200 OK)
```json
{
  "type": "array",
  "items": {
    "$ref": "#/properties/PaymentResponse"
  }
}
```

#### Respostas de Erro (`ApiError`)
| HTTP Status | Rota | Causa | Formato do Payload |
| :--- | :--- | :--- | :--- |
| `404 Not Found` | `GET /api/payments/{paymentId}` | ID inexistente na base | `{"message": "Payment ID: [ID] is not found", "errors": []}` |
| `400 Bad Request` | `GET /api/payments/payer/{payerId}` | UUID malformado ou nulo | `{"message": "Invalid UUID format for payer ID: [PAYER_ID]", "errors": []}` |
| `500 Internal Error`| Todas | Falha não capturada | `ApiError` estruturado |

---

## 3. Regras de Negócio & Invariantes

- **RN-01 [Existência Obrigatória na Busca Unitária]:** Quando consultado por `paymentId`, o registro deve existir no banco de dados. Caso contrário, lança-se `PaymentNotFoundException` com a mensagem `"Payment ID: %s is not found".formatted(paymentId)` (HTTP 404).
- **RN-02 [Listagem Vazia Válida]:** Se não houver pagamentos cadastrados no sistema (ou para um determinado `payerId` válido), o sistema deve retornar HTTP 200 com array vazio `[]`, e **nunca** null ou 404.
- **RN-03 [Validação de UUID do Pagador]:** 
  - A rota `/api/payments/payer/{payerId}` exige que o parâmetro represente um UUID RFC 4122 sintaticamente válido.
  - Se a conversão disparar `IllegalArgumentException`, o serviço deve traduzir para `PaymentException` com a mensagem `"Invalid UUID format for payer ID: %s".formatted(payerId)` (HTTP 400).
  - Se o valor recebido for nulo, lança-se `PaymentException` com `"UUID must not be null"` (HTTP 400).
- **RN-04 [Imutabilidade de Metadados]:** As operações de consulta são de leitura estrita (`Idempotent` e `Safe`), não alterando qualquer estado na base de dados.

---

## 4. Critérios de Aceitação (Given-When-Then / BDD)

### Cenário 1: Consulta por ID de pagamento existente
- **Dado que** existe um pagamento persistido com ID `1` e `status=PENDING`,
- **Quando** enviar uma requisição `GET /api/payments/1`,
- **Então** o sistema deve retornar HTTP 200 OK,
- **E** o payload deve corresponder aos dados salvos no banco.

### Cenário 2: Consulta por ID inexistente
- **Dado que** não existe pagamento associado ao ID `999`,
- **Quando** enviar uma requisição `GET /api/payments/999`,
- **Então** o sistema deve responder com HTTP 404 Not Found,
- **E** o corpo do erro deve conter `"Payment ID: 999 is not found"`.

### Cenário 3: Listagem geral de pagamentos populada
- **Dado que** existem 2 pagamentos registrados no sistema (um PIX e um CREDIT_CARD),
- **Quando** enviar uma requisição `GET /api/payments`,
- **Então** o sistema deve retornar HTTP 200 OK com array contendo exatamente os 2 elementos.

### Cenário 4: Listagem geral de pagamentos vazia
- **Dado que** a base de dados não contém pagamentos,
- **Quando** enviar uma requisição `GET /api/payments`,
- **Então** o sistema deve retornar HTTP 200 OK com array vazio `[]`.

### Cenário 5: Histórico por pagador com UUID válido e registros
- **Dado que** o pagador `739e5f1e-2459-499d-9cdd-ab59b31c02d9` possui 3 pagamentos,
- **Quando** enviar uma requisição `GET /api/payments/payer/739e5f1e-2459-499d-9cdd-ab59b31c02d9`,
- **Então** o sistema deve responder com HTTP 200 OK trazendo os 3 pagamentos vinculados a este UUID.

### Cenário 6: Histórico por pagador com UUID válido sem nenhum registro
- **Dado que** o pagador `00000000-0000-0000-0000-000000000000` nunca fez pagamentos,
- **Quando** consultar suas transações,
- **Então** deve retornar HTTP 200 OK com array vazio `[]`.

### Cenário 7: Histórico por pagador com UUID malformado
- **Dado que** a requisição informa `payerId="uuid-invalido-123"`,
- **Quando** acionar o endpoint `GET /api/payments/payer/uuid-invalido-123`,
- **Então** o sistema deve recusar a requisição com HTTP 400 Bad Request,
- **E** a mensagem de erro deve explicitar `"Invalid UUID format for payer ID: uuid-invalido-123"`.

---

## 5. Rastreabilidade com Código e Test Harness

### Código de Produção
- `PaymentController#retrievePaymentByPaymentId`
- `PaymentController#retrieveAllPayments`
- `PaymentController#retrieveAllPaymentsByPayerId`
- `PaymentService#retrievePaymentDataById`
- `PaymentService#retrieveAllPayments`
- `PaymentService#retrievePaymentByPayerId`
- `PaymentService#validateAndGetUuidByString`
- `PaymentRepository#findAllByPayerId`

### Testes Automatizados (Harness de Validação)
- `PaymentServiceTest#shouldRetrievePaymentDataWhenReceiveValidPaymentID`
- `PaymentServiceTest#shouldThrowsPaymentNotFoundExceptionWhenReceiveInvalidPaymentID`
- `PaymentServiceTest#shouldThrowsPaymentNotFoundExceptionWhenReceiveNullPaymentID`
- `PaymentServiceTest#shouldRetrieveAllPaymentsWhenCallingTheMethodRetrieveAllPayment`
- `PaymentServiceTest#shouldRetrieveEmptyListWhenCallingTheMethodRetrieveAllPayment`
- `PaymentServiceTest#shouldThrowPaymentExceptionWhenCallingTheMethodRetrievePaymentByPayerIdWithInvalidPayerId`
- `PaymentServiceTest#shouldThrowPaymentExceptionWhenCallingTheMethodRetrievePaymentByPayerIdWithNullPayerId`
- `PaymentIntegrationTest`: Testes de integração cobrindo os 3 endpoints via MockMvc e asserções JSONPath.
