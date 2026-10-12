# 🧭 Guia Definitivo: Spec-Driven Development (SDD)
> **Do Zero ao Avançado na Engenharia de Software Orientada por Especificações**  
> *Baseado na análise e boas práticas de SDD (Spec-Driven Development), IA Generativa e Engenharia de Software Moderna.*

---

## 📑 Sumário
1. [Introdução & Mudança de Paradigma](#1-introdução--mudança-de-paradigma)
2. [Comparativo de Abordagens (Code-Driven vs TDD vs BDD vs SDD)](#2-comparativo-de-abordagens)
3. [O Risco do "Waterfall Automatizado"](#3-o-risco-do-waterfall-automatizado)
4. [Anatomia de uma Especificação Viva (*Spec*)](#4-anatomia-de-uma-especificação-viva-spec)
5. [Os Três Níveis de Adoção Técnica da Spec](#5-os-três-níveis-de-adoção-técnica-da-spec)
6. [Spec Maturity Model (Os 5 Níveis Organizacionais)](#6-spec-maturity-model-os-5-níveis-organizacionais)
7. [Ferramentas e Frameworks Emergentes](#7-ferramentas-e-frameworks-emergentes)
8. [Governança, CI/CD e Métricas de Impacto](#8-governança-cicd-e-métricas-de-impacto)
9. [A Fronteira Agêntica e Harness Engineering](#9-a-fronteira-agêntica-e-harness-engineering)
10. [Checklist Prático para Começar Hoje](#10-checklist-prático-para-começar-hoje)

---

## 1. Introdução & Mudança de Paradigma

### O Problema do Modelo Tradicional
Na maioria dos times, o código-fonte concentra todo o conhecimento e as decisões de implementação. Com isso:
- **Documentação desatualizada:** Wikis, Confluence e PRDs tornam-se obsoletos dias após a primeira sprint.
- **Silos de conhecimento:** As regras reais do negócio vivem "na cabeça" de desenvolvedores-chave.
- **Bugs e retrabalho:** Falhas de alinhamento entre Produto e Engenharia geram refatorações constantes.

```
Modelo Tradicional (Code-Driven):
Requisito (Texto estático) ───> [ Código = Única Fonte da Verdade ]
                                   │
                                   └──> Documentação desatualizada / Silos

Modelo SDD (Spec-Driven):
Intenção de Negócio ───> [ Spec Estruturada = Única Fonte da Verdade ]
                              │
                              ├───> Geração de Código (via IA)
                              ├───> Geração de Testes Unitários e Integração
                              └───> Documentação Contínua & Contratos Vivos
```

### O que é Spec-Driven Development?
O **Spec-Driven Development (SDD)** é uma abordagem na qual **especificações estruturadas e versionadas (*specs*)** orientam todo o ciclo de vida do software (SDLC) — do levantamento de requisitos ao deploy —, atuando como a principal fonte de verdade interpretável tanto por humanos quanto por IAs e agentes autônomos.

---

## 2. Comparativo de Abordagens

| Abordagem | Fonte da Verdade | Foco Principal | Benefícios | Limitações |
| :--- | :--- | :--- | :--- | :--- |
| **Code-Driven** | Código-fonte | Implementação e descoberta técnica | Flexibilidade para protótipos e PoCs | Baixa previsibilidade, silos de conhecimento e documentação morta |
| **TDD** *(Test-Driven)* | Casos de Teste | Qualidade e validação de engenharia | Redução de bugs estruturais de baixo nível | Foco estritamente técnico; pode se distanciar das metas de negócio |
| **BDD** *(Behavior-Driven)* | Critérios de Aceite | Alinhamento de negócio via linguagem ubíqua | Excelente comunicação entre Produto e Engenharia | Os testes **validam** o sistema, mas **não o constroem** |
| **SDD** *(Spec-Driven)* | **Especificações (*Specs*)** | **Intenção + Requisitos estruturados + Automação** | **Escala com IA e geração direta de artefatos** | Exige alta maturidade técnica e governança contínua |

> 📌 **A evolução:** O SDD representa a evolução natural do BDD na era dos Large Language Models (LLMs). A especificação deixa de ser apenas um validador de testes e vira o **motor executável** da aplicação.

---

## 3. O Risco do "Waterfall Automatizado"

Um dos maiores erros ao adotar IA e SDD é tratar a especificação como um artefato descartável:
1. O desenvolvedor escreve uma spec inicial.
2. A IA gera o código.
3. Quando as regras mudam, o desenvolvedor altera **apenas** o código e abandona a spec.

> ⚠️ **Atenção:** Sem disciplina contínua, o SDD vira um **waterfall automatizado**, acelerando a criação de débito técnico. Toda alteração no comportamento do sistema deve ser refletida primeiro ou simultaneamente na spec.

---

## 4. Anatomia de uma Especificação Viva (*Spec*)

Uma especificação no contexto do SDD precisa ser clara para pessoas de produto e suficientemente estruturada para agentes de IA.

### Estrutura Recomendada
1. **Cabeçalho de Governança:** ID, Versão semântica, Responsável/Squad, Status (*Draft, Approved, Deprecated*).
2. **Intenção & Contexto de Negócio:** Qual problema resolve e o objetivo de negócio.
3. **Contratos Técnicos:** Schemas JSON, contratos OpenAPI, tipos TypeScript ou definições de dados.
4. **Regras de Negócio & Casos de Borda:** Comportamento esperado em sucesso, falhas, permissões e idempotência.
5. **Critérios de Aceite Auditáveis:** Cenários verificáveis (estilo *Given-When-Then*).

### Exemplo de Template de Spec (`/specs/billing/create-subscription.md`)

```markdown
# SPEC-042: Criação de Assinatura Recorrente
- Versão: 1.1.0
- Squad: Monetização
- Status: Aprovado
- Data: 2026-10-12

## 1. Intenção
Permitir que usuários façam upgrade para o plano Pro com cobrança recorrente via cartão de crédito.

## 2. Contrato de Entrada (JSON Schema)
```json
{
  "type": "object",
  "required": ["customerId", "planId", "paymentToken"],
  "properties": {
    "customerId": { "type": "string", "format": "uuid" },
    "planId": { "type": "string", "enum": ["pro_monthly", "pro_annual"] },
    "paymentToken": { "type": "string" }
  }
}
```

## 3. Regras de Negócio
1. O usuário não pode possuir mais de uma assinatura ativa simultaneamente (retornar HTTP 409 Conflict).
2. Se o gateway de pagamento recusar a cobrança, registrar log de auditoria e retornar HTTP 402 Payment Required.
3. Após sucesso no gateway, gravar o registro no banco com status `active` e publicar o evento `subscription.created` no broker de eventos.

## 4. Critérios de Aceitação

- [ ] **Cenário 1 (Sucesso):** Dados válidos e cartão aprovado -> Retornar HTTP 201 com id da assinatura e status "active".
- [ ] **Cenário 2 (Idempotência):** Reenvio com mesmo `Idempotency-Key` -> Retornar status 200 com payload idêntico sem duplicar cobrança.
- [ ] **Cenário 3 (Cartão Recusado):** Gateway retorna recusa -> Retornar HTTP 402 e não salvar registro ativo no banco.


---

## 5. Os Três Níveis de Adoção Técnica da Spec

```
[ Nível 1: Spec-First ] ───> [ Nível 2: Spec-Anchored ] ───> [ Nível 3: Spec-as-Source ]
Prompt rico para IA           Spec e Código coexistem        Spec é a única fonte editada;
para kick-off de código       e evoluem juntos no Git        IA gera e mantém o código
```

1. **Spec-First (Ponto de Partida):**
   - A engenharia padroniza a intenção e os critérios de aceite antes de codificar.
   - A spec serve como contexto enriquecido (prompt rico) para copilotos gerarem o scaffolding inicial.

2. **Spec-Anchored (Especificação Ancorada):**
   - As specs passam a residir no repositório de código (ex: pasta `/specs/`).
   - Quando a feature evolui, a spec e o código evoluem no mesmo Pull Request. Serve de base contínua para geração e manutenção de testes.

3. **Spec-as-Source (Especificação como Código-Fonte):**
   - O ápice do modelo. Humanos editam prioritariamente as especificações.
   - Pipelines automatizados e agentes autônomos de IA derivam, compilam e mantêm o código de produção a partir da spec.

---

## 6. Spec Maturity Model (Os 5 Níveis Organizacionais)

```
┌────────────────────────────────────────────────────────────────────────┐
│ Nível 5: Organização Orientada por Intenção (Fronteira Agêntica)       │
├────────────────────────────────────────────────────────────────────────┤
│ Nível 4: Specs Executáveis (IA gera código e artefatos diretamente)    │
├────────────────────────────────────────────────────────────────────────┤
│ Nível 3: Integração com Engenharia (Specs conectadas a testes e CI/CD) │
├────────────────────────────────────────────────────────────────────────┤
│ Nível 2: Specs Estruturadas por IA (Templates padronizados sem ruído)  │
├────────────────────────────────────────────────────────────────────────┤
│ Nível 1: Documentação Estática (Requisitos tradicionais desconectados) │
└────────────────────────────────────────────────────────────────────────┘
```

- **Nível 1 — Documentação Estática:** Requisitos tradicionais no Jira ou Notion, desconectados da realidade do código.
- **Nível 2 — Specs Estruturadas por IA:** Adoção de modelos de linguagem para eliminar ambiguidades, definir schemas e estruturar critérios de aceite claros.
- **Nível 3 — Integração com Engenharia:** Specs conectadas a testes de contrato, mocks e pipelines de CI/CD.
- **Nível 4 — Specs Executáveis:** Automação e IA geram serviços inteiros, rotas, schemas e baterias de testes diretamente da spec.
- **Nível 5 — Organização Orientada por Intenção:** Operação suportada por agentes autônomos que realizam manutenção contínua, correção de falhas (*self-healing*) e evolução com validação humana em pontos estratégicos (*Human-in-the-Loop*).

---

## 7. Ferramentas e Frameworks Emergentes

- **[Spec-kit](https://github.com/github/spec-kit):** Framework open-source focado em templates padronizados para estruturar especificações reutilizáveis por humanos e LLMs.
- **[Kiro](https://kiro.dev/):** Plataforma de orquestração que liga especificações estruturadas a agentes de IA para criação de APIs e serviços de backend.
- **[Tessl](https://tessl.io/):** Framework de engenharia de software orientado por specs formais para geração e manutenção controlada por IA.
- **[Lovable](https://lovable.dev/):** Ferramenta impulsionadora do conceito de *Vibe Coding*, onde intenções e especificações em alto nível geram produtos completos rapidamente.

---

## 8. Governança, CI/CD e Métricas de Impacto

### Boas Práticas de Governança
- **Versionamento no Git:** Specs tratadas como código de produção (revisões de PR, branch rules, tags de release).
- **Code Review das Specs:** Product Managers e Arquitetos revisam a spec antes ou em conjunto com a implementação.
- **Human-in-the-Loop (HITL):** Aceleração de geração com IA sem abrir mão de revisão humana em segurança, performance e compliance.

### Métricas para Comprovar o Sucesso
- 📉 **Redução do Lead Time:** Menor tempo entre o desenho da feature e a entrega em produção.
- 📉 **Queda no Rework Rate:** Menos código reescrito por causa de mal-entendidos de negócio.
- 📉 **Redução de Bugs em Produção:** Regras de negócio validadas previamente por testes gerados a partir da spec.
- 📈 **Taxa de Cobertura de Specs:** Percentual de módulos do sistema regidos por specs vivas.
- 📈 **Percentual de Código Derivado:** Volume de código boilerplate e testes gerados automaticamente.

---

## 9. A Fronteira Agêntica e Harness Engineering

Na fronteira da IA Agêntica (Nível 5 do Maturity Model), surge o conceito de **Harness Engineering**:
- O agente de IA não opera solto no repositório sem supervisão.
- Constrói-se um **Harness** (ambiente com testes automatizados, linters, validadores de schemas e guardrails de segurança).
- A **Spec** é o mapa que guia o agente; o **Harness** é o trilho que impede o agente de introduzir regressões ou violações arquiteturais.
- **Self-Healing:** Quando um teste falha, o próprio harness repassa o erro e a spec de volta ao agente para que ele faça o auto-reparo antes do merge.

---

## 10. Checklist Prático para Começar Hoje

- [ ] **Passo 1: Crie o diretório `/specs` no seu repositório**
- [ ] **Passo 2: Defina um template base** (Intenção, Schemas, Regras de Negócio e Casos de Aceite).
- [ ] **Passo 3: Comece com uma feature isolada** (ex: um novo endpoint de API ou fluxo de cadastro).
- [ ] **Passo 4: Use a spec como input para o Copiloto/IA** e exija que a IA crie primeiro a suíte de testes com base nos critérios de aceite.
- [ ] **Passo 5: Defina a regra de ouro do time:** Nenhuma mudança de comportamento de software entra em produção sem atualizar a respectiva spec no Git.
