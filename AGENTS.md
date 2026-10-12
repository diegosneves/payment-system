# Instruções para Agentes de IA — Payment System

> **Projeto:** Payment System (Microsserviço de Pagamentos e Assistente RAG)  
> **Arquitetura & Metodologia:** Spec-Driven Development (SDD) com Harness Engineering  
> **Linguagem & Framework:** Java 25 (LTS), Spring Boot 3.5, Spring Data JPA, MySQL 8, Testcontainers, LangChain4j

---

## 🧭 Regra de Ouro (SDD Inegociável)
1. **A Spec é a Verdade Absoluta:** Toda mudança de comportamento, novo endpoint, validação ou regra de negócio **DEVE** começar pela respectiva especificação na pasta [`spec/`](file:///home/diegosneves/Github/pessoal/java/payment-system/spec/).
2. **Nunca altere código de produção sem a spec atualizada:** Nenhuma funcionalidade deve ser criada ou alterada diretamente no código sem a spec correspondente estar criada ou atualizada no mesmo PR/commit.
3. **Leia antes de agir:** Sempre consulte [`spec/README.md`](file:///home/diegosneves/Github/pessoal/java/payment-system/spec/README.md) (matriz de rastreabilidade) e [`spec/HARNESS.md`](file:///home/diegosneves/Github/pessoal/java/payment-system/spec/HARNESS.md) antes de iniciar qualquer modificação.

---

## 🔄 Fluxo Obrigatório de Execução para Agentes
Quando receber uma tarefa para implementar ou alterar uma funcionalidade:

1. **Leitura da Spec:**
   - Leia a spec indicada em `/spec/SPEC-XXX.md`.
   - Extraia os contratos de API (método, rota, request, response, códigos HTTP).
   - Extraia as regras de negócio invariantes (`RN-XX`) e os cenários de aceite *Given-When-Then*.

2. **Testes Primeiro (Spec-to-Test):**
   - Escreva ou atualize os testes unitários (`PaymentServiceTest`, `PaymentLimitValidatorTest`) e de integração (`PaymentIntegrationTest`) a partir dos critérios da spec.
   - Execute os testes e confirme que eles falham pelo motivo esperado (estado vermelho legítimo).

3. **Implementação de Produção:**
   - Implemente o código mínimo estritamente necessário para satisfazer os contratos e fazer os testes passarem.

4. **Execução do Harness & Loop de Auto-Reparo (Self-Healing):**
   - Execute o Harness: `./mvnw clean test` (ou `./mvnw test -Dtest=...`).
   - Se houver falhas: analise o erro, releia a spec e ajuste o código (máximo de 5 tentativas).
   - Se o teste divergir da spec, **a spec manda**. Nunca enfraqueça uma asserção de teste para mascarar um erro de código.

5. **Finalização:**
   - Atualize a matriz de rastreabilidade e o status no [`spec/README.md`](file:///home/diegosneves/Github/pessoal/java/payment-system/spec/README.md).
   - Apresente um resumo claro dos arquivos criados e modificados.

---

## 🛡️ Restrições Técnicas & Padrões Arquiteturais
- **DTOs:** Sempre use Java Records imutáveis (`dto/*Request.java`, `dto/*Response.java`).
- **Encapsulamento JPA:** Nunca exponha entidades JPA (`model/Payment.java`) diretamente nas respostas das controllers. Use mapeamento para Records.
- **Tratamento de Erros:** Mantenha o formato padrão `ApiError(String message, List<ErrorData> errors)` mapeado no `GlobalExceptionHandler`.
- **Zonas Horárias:** Para datas de negócio brasileiras, utilize o helper `DataConverter.BRAZIL` (`America/Sao_Paulo`).
- **Git & Commits:** Não faça commits ou pushes a menos que o usuário solicite explicitamente.

---

## ⚡ Comandos Úteis do Harness
- **Todos os testes do projeto (requer Docker ativo para Testcontainers):**
  ```bash
  ./mvnw clean test
  ```
- **Testes unitários rápidos de regras e limites:**
  ```bash
  ./mvnw test -Dtest=PaymentLimitValidatorTest,PaymentServiceTest
  ```
- **Testes de integração de API (MockMvc + Testcontainers MySQL):**
  ```bash
  ./mvnw test -Dtest=PaymentIntegrationTest
  ```
- **Testes de persistência e agregações SQL:**
  ```bash
  ./mvnw test -Dtest=PaymentRepositoryTest
  ```
