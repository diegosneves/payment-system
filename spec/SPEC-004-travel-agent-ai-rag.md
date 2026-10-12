# SPEC-004: Assistente Conversacional de Viagens com IA e RAG Local

- **Versão:** 1.0.0
- **Squad / Domínio:** AI & Intelligent Assistants
- **Status:** Implemented
- **Data de Criação:** 2026-10-12
- **Última Atualização:** 2026-10-12
- **Autores / Responsáveis:** Diego Neves

---

## 1. Intenção & Contexto de Negócio

### 1.1 Objetivo
Disponibilizar uma interface conversacional inteligente (*Travel Agent Assistant*) capaz de atender dúvidas sobre turismo e roteiros de viagem utilizando Inteligência Artificial Generativa enriquecida com **Retrieval-Augmented Generation (RAG)** em modelo local (*on-premises*).

### 1.2 Problema Resolvido
Grandes Modelos de Linguagem (LLMs) genéricos sofrem de alucinações e desconhecem especificidades regionais ou diretrizes restritas de negócio. O uso de RAG local via LangChain4j garante respostas fundamentadas (*grounded*) em documentos curados, com privacidade de dados, custo zero de tokens e conformidade às diretrizes locais.

### 1.3 Escopo & Limites
- **Dentro do escopo:**
  - Endpoint REST textual para interação síncrona com o agente.
  - Ingestão automática de documentos Markdown a partir do classpath (`rag/`).
  - Segmentação recursiva de texto e geração de embeddings locais com modelo `nomic-embed-text`.
  - Inferência conversacional via Ollama com modelo `qwen2.5-coder:7b`.
- **Fora do escopo:**
  - Histórico persistido de conversas por sessão (chat memory multi-turn em banco relacional).
  - Streaming SSE (Server-Sent Events) de tokens.

---

## 2. Contratos Técnicos

### 2.1 Interface REST
- **Método HTTP:** `POST`
- **Rota:** `/api/travel-agent`
- **Consumes:** `text/plain`
- **Produces:** `text/plain`

### 2.2 Exemplo de Entrada (Raw String)
```text
Quais são as principais atrações de Gramado?
```

### 2.3 Exemplo de Resposta (HTTP 200 OK - Raw String)
```text
Gramado, na Serra Gaúcha, é muito conhecida pelo tradicional Natal Luz, pelas fábricas de chocolate artesanal, além de paisagens naturais e opções de trilhas.
```

### 2.4 Respostas de Erro Padronizadas
| HTTP Status | Causa | Resposta |
| :--- | :--- | :--- |
| `400 Bad Request` | Corpo da mensagem inválido ou nulo | Bad Request |
| `500 Internal Server Error` | Ollama offline ou falha na inferência | Internal Server Error |

---

## 3. Arquitetura de IA & Regras de Negócio

```
                      ┌────────────────────────────────────────┐
                      │ Base de Documentos: /rag/*.md         │
                      └──────────────────┬─────────────────────┘
                                         │ Ingestão no Startup
                                         ▼
                             DocumentSplitters.recursive
                             (chunk: 300, overlap: 30)
                                         │
                                         ▼
┌──────────────┐                  EmbeddingModel (Ollama: nomic-embed-text)
│   Usuário    │                         │
│  (Pergunta)  │                         ▼
└──────┬───────┘              InMemoryEmbeddingStore
       │                                 ▲
       ▼                                 │ Recuperação por similaridade
POST /api/travel-agent ──────────> ContentRetriever
                                         │
                                         ▼
                              ChatLanguageModel (Ollama: qwen2.5-coder:7b)
                                         │
                                         ▼
                               Resposta Fundamentada
```

- **RN-01 [Ingestão Automática no Startup]:**
  - Durante a inicialização da aplicação, a classe de configuração carrega todos os documentos presentes no diretório de recursos `rag/` (`ClassPathDocumentLoader.loadDocuments("rag")`).
  - Os documentos são fragmentados com estratégia recursiva configurável:
    - Tamanho máximo do segmento (`rag.max-segment-size`): **300 caracteres** (padrão).
    - Sobreposição (`rag.max-overlap-size`): **30 caracteres** (padrão).
  - Os vetores são indexados no `InMemoryEmbeddingStore`.
- **RN-02 [Integração LangChain4j @AiService]:**
  - A interface `TravelAgentAssistant` é declarada como `@AiService`, vinculando automaticamente o modelo de linguagem ao `ContentRetriever`.
- **RN-03 [Parâmetros de Inferência]:**
  - Modelo de Chat: `qwen2.5-coder:7b` (temperatura 0.7, timeout 120s).
  - Modelo de Embeddings: `nomic-embed-text` (timeout 60s).
- **RN-04 [Diretrizes de Conhecimento Turístico]:**
  - Se questionado sobre **Sapucaia do Sul - RS**, o assistente deve esclarecer a ausência de pontos turísticos e sugerir Gramado - RS ou Porto Alegre.
  - Se questionado sobre **Gramado - RS**, deve destacar a Serra Gaúcha, Natal Luz, fábricas de chocolate e paisagens naturais, nunca sugerindo Sapucaia do Sul.

---

## 4. Critérios de Aceitação (Given-When-Then / BDD)

### Cenário 1: Consulta sobre destino coberto pela base RAG
- **Dado que** a base RAG contém o documento com fatos sobre Gramado - RS,
- **Quando** o usuário enviar uma mensagem perguntando sobre atividades em Gramado,
- **Então** o assistente deve responder mencionando o Natal Luz ou fábricas de chocolate,
- **E** o código de resposta deve ser HTTP 200 OK em texto plano.

### Cenário 2: Consulta sobre cidade com recomendação de redirecionamento
- **Dado que** o documento estabelece que Sapucaia do Sul não possui pontos turísticos,
- **Quando** o usuário perguntar o que visitar em Sapucaia do Sul,
- **Então** o assistente deve orientar a visita a Gramado ou Porto Alegre.

### Cenário 3: Resiliência e Timeouts
- **Dado que** a requisição de inferência exija até 120 segundos,
- **Quando** o Ollama processar a resposta,
- **Então** o cliente não deve sofrer timeout prematuro da camada HTTP.

---

## 5. Rastreabilidade com Código e Test Harness

### Código de Produção
- `br.com.neves.paymentsystem.controllers.TravelAgentController#chat`
- `br.com.neves.paymentsystem.ia.TravelAgentAssistant`
- `br.com.neves.paymentsystem.configuration.ia.RagConfiguration`
- `src/main/resources/rag/contexto-teste.md`
- `src/main/resources/application.properties` (seção LangChain4j & RAG)
