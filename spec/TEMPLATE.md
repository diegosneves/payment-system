# SPEC-[ID]: [Título Conciso e Descritivo da Feature]

- **Versão:** [ex: 1.0.0]
- **Squad / Domínio:** [ex: Core Banking / Payments]
- **Status:** [Draft | Under Review | Approved | Implemented | Deprecated]
- **Data de Criação:** [YYYY-MM-DD]
- **Última Atualização:** [YYYY-MM-DD]
- **Autores / Responsáveis:** [ex: Diego Neves, Product Team]

---

## 1. Intenção & Contexto de Negócio

### 1.1 Objetivo
*Descreva em 1 ou 2 parágrafos qual é o objetivo de negócio desta funcionalidade e que valor ela entrega ao usuário final ou à organização.*

### 1.2 Problema Resolvido
*Explique a dor que motivou a especificação e por que a solução atual é insuficiente.*

### 1.3 Escopo & Limites
- **Dentro do escopo:** O que esta especificação cobre explicitamente.
- **Fora do escopo:** O que fica postergado para versões futuras ou outros domínios.

---

## 2. Contratos Técnicos

### 2.1 Interface REST / Endpoints
- **Método HTTP:** `POST | GET | PUT | DELETE`
- **Rota:** `/api/...`
- **Content-Type Suportados:** `application/json`

### 2.2 Schema de Entrada (Payload / Request)
```json
{
  "$schema": "http://json-schema.org/draft-07/schema#",
  "type": "object",
  "required": ["campoObrigatorio"],
  "properties": {
    "campoObrigatorio": { "type": "string" }
  }
}
```

*Ou definição correspondente em Java Record:*
```java
public record MinhaRequest(
    @NotNull String campoObrigatorio
) {}
```

### 2.3 Schema de Saída (Sucesso - 200/201)
```json
{
  "type": "object",
  "properties": {
    "id": { "type": "integer" }
  }
}
```

### 2.4 Respostas de Erro Padronizadas (`ApiError`)
| HTTP Status | Causa | Mensagem / Formato |
| :--- | :--- | :--- |
| `400 Bad Request` | Parâmetros malformados | Formato inválido |
| `404 Not Found` | Recurso não localizado | `[Entidade] ID: {id} is not found` |
| `422 Unprocessable Entity` | Violação de regra de negócio | `ApiError` com lista de `ErrorData` |
| `500 Internal Server Error` | Falha inesperada de infraestrutura | Erro interno |

---

## 3. Regras de Negócio & Invariantes

- **RN-01 [Nome da Regra]:** Descrição clara e inequívoca da regra.
- **RN-02 [Restrições de Limites]:** Valores mínimos, máximos ou janelas temporais.
- **RN-03 [Validação de Entradas]:** Regras sintáticas e semânticas.
- **RN-04 [Idempotência & Concorrência]:** Comportamento em caso de reexecução.

---

## 4. Critérios de Aceitação (Given-When-Then / BDD)

Os critérios de aceitação devem ser escritos para servirem como instrução direta na geração de testes automatizados unitários e de integração:

### Cenário 1: [Cenário de Sucesso Principal - Happy Path]
- **Dado que** o sistema está no estado X com os parâmetros válidos Y,
- **Quando** a operação Z for executada,
- **Então** o sistema deve persistir os dados, retornar status HTTP 201/200 e o payload conter o ID gerado.

### Cenário 2: [Cenário de Falha / Violação de Regra de Negócio]
- **Dado que** a entrada infringe a regra RN-02,
- **Quando** a operação Z for solicitada,
- **Então** o sistema deve abortar a persistência, retornar HTTP 422 e a mensagem de erro específica no payload `ApiError`.

### Cenário 3: [Cenário de Borda / Entrada Nula ou Inválida]
- **Dado que** o payload contenha valores nulos nos campos obrigatórios,
- **Quando** o endpoint for acionado,
- **Então** o sistema deve retornar HTTP 400 ou 422.

---

## 5. Rastreabilidade com Código e Test Harness

- **Classes de Domínio:** `model/...`
- **Serviços de Negócio:** `services/...`
- **Controladores Web:** `controllers/...`
- **Validações:** `validators/...`
- **Suíte de Testes:** `*Test.java` (Unitários e de Integração com Testcontainers)
