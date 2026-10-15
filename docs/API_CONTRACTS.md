# Contratos de dados de IA

Base: /api/v1. JSON camelCase. Todos os exemplos abaixo são sintéticos; IDs de exemplo precisam ser substituídos por documentos existentes no ambiente isolado antes de criar referências.

Cada recurso oferece GET na base, GET /{id}, POST, PATCH /{id} e DELETE /{id}. POST requer os campos indicados como obrigatórios; PATCH aceita qualquer subconjunto dos campos mutáveis, sem anular os obrigatórios. Não existe PUT de substituição completa.

| Recurso | Campos obrigatórios no POST | Outros campos mutáveis |
| --- | --- | --- |
| conversations | userId: inteiro positivo; status: string não vazia | messages: array; summary: string; summarizedCount: inteiro >=0; createdAt, updatedAt, summaryUpdatedAt: datas |
| feedback | userId; executionId: ObjectId; rating: inteiro 1–5 | comment: string/null; createdAt: data |
| user-memories | userId | memories: array; updatedAt: data |
| agent-traces | executionId; agentName: string; input/output: objetos; latencyMs: inteiro >=0; status: string | startedAt, completedAt: datas; model: string/null; estimatedTokens: inteiro >=0/null; estimatedCostUsd: número >=0/null |
| guardrail-events | executionId; guardrailType, action, reason: strings | category: string/null; createdAt: data |
| judge-evaluations | executionId; judgeAgent: string; score: número 0–1; grounded, hallucinationDetected: booleanos; decision, justification: strings | createdAt: data |
| rag-chunks | documentId: ObjectId; chunkIndex: inteiro >=0; content: string; embedding: array de números finitos | Nenhum |
| ai-executions | userId; conversationId: ObjectId; input, status: strings | agentsUsed: array de strings; startedAt, completedAt: datas; finalResponse: string/null; totalLatencyMs: inteiro >=0/null; estimatedCostUsd: número >=0/null |
| rag-documents | title, description, sourceType, sourceUrl: strings; active: booleano | createdAt: data |

Strings obrigatórias precisam conter ao menos um caractere não branco. userId usa int32; valores fracionários e números em strings são rejeitados. Datas de entrada devem ser ISO-8601 com fuso, por exemplo 2026-10-10T12:00:00Z. Datas opcionais aceitam null; createdAt/updatedAt/startedAt ausentes na criação recebem a hora atual. messages, memories e agentsUsed ausentes na criação recebem [].

As políticas rating 1–5 e score 0–1 são validações desta API, não constraints identificadas no banco. Status, categorias, tipos e decisões são strings abertas. Embeddings não possuem dimensão fixa comprovada; a API não presume uma dimensão nem um modelo gerador.

## Objetos embutidos

messages contém objetos com sender/content obrigatórios, messageId opcional (string) e timestamp opcional. Se ausentes, messageId é gerado como ObjectId e timestamp recebe a hora atual. A leitura também suporta messageId legado BSON ObjectId.

memories contém objetos com type/content obrigatórios, importance obrigatória e >=0, memoryId opcional (ObjectId hexadecimal), createdAt/updatedAt opcionais. IDs/datas ausentes são gerados. importance aceita 5: o banco não comprova uma escala restrita a 0–1.

Arrays não aceitam null nem elementos null; use [] para limpar. Objetos embutidos de arrays enviados devem ser completos porque PATCH substitui o array inteiro.

input/output de agent-traces preservam objetos heterogêneos. PATCH aplica folhas aninhadas por $set, mantendo chaves não enviadas. Um patch contendo somente objetos vazios é rejeitado. Chaves vazias, com ponto, iniciadas por $ ou contendo NUL são rejeitadas.

Limites de entrada: strings até 200.000 caracteres, arrays até 4.096 elementos, objetos dinâmicos até 1.000 campos e profundidade máxima 12. Não se aceita alteração de id/_id, nomes snake_case nem campos desconhecidos.

## Exemplos de criação

### Conversation

```json
{"userId":1,"status":"ACTIVE","messages":[{"sender":"USER","content":"Pergunta de exemplo"}]}
```

### AI execution

```json
{"userId":1,"conversationId":"000000000000000000000001","input":"Pergunta de exemplo","status":"IN_PROGRESS","agentsUsed":["agent-example"]}
```

### Feedback

```json
{"userId":1,"executionId":"000000000000000000000002","rating":5,"comment":"Exemplo sintético"}
```

### User memory

```json
{"userId":1,"memories":[{"type":"PREFERENCE","content":"Respostas curtas","importance":0.8}]}
```

### Agent trace

```json
{"executionId":"000000000000000000000002","agentName":"agent-example","input":{"question":"Exemplo"},"output":{"answer":"Exemplo"},"latencyMs":20,"status":"SUCCESS"}
```

### Guardrail event

```json
{"executionId":"000000000000000000000002","guardrailType":"INPUT_VALIDATION","action":"ALLOW","reason":"Exemplo de validação"}
```

### Judge evaluation

```json
{"executionId":"000000000000000000000002","judgeAgent":"judge-example","score":0.9,"grounded":true,"hallucinationDetected":false,"decision":"ALLOW","justification":"Exemplo sintético"}
```

### RAG document

```json
{"title":"Documento exemplo","description":"Descrição sintética","sourceType":"WEB","sourceUrl":"https://example.com/document","active":true}
```

### RAG chunk

```json
{"documentId":"000000000000000000000003","chunkIndex":0,"content":"Trecho sintético","embedding":[0.1,0.2,0.3]}
```

sourceUrl é armazenada; não há busca/download automático. Exemplos não representam valores completos observados na investigação.

## Respostas e atualização

Documentos retornam id hexadecimal e campos conhecidos da modelagem, sem _id, campos internos do driver ou _class. Datas BSON são serializadas como ISO e strings legadas permanecem strings. Campos desconhecidos na raiz são preservados no banco, mas não expostos no contrato. Dados dinâmicos input/output fazem parte da resposta autenticada.

Exemplo de PATCH de execução:

```json
{"status":"COMPLETED","finalResponse":"Resposta exemplo","completedAt":"2026-10-10T12:00:00Z"}
```

Exemplo de PATCH de trace:

```json
{"output":{"answer":"Resposta atualizada"}}
```

Essa operação altera output.answer e preserva outras chaves de output. Não existe append implícito em messages/memories; envie o array completo desejado.

Referências são verificadas ao criar ou mudar o vínculo/userId. Excluir conversa referenciada por execução, execução com feedback/traces/guardrails/avaliações ou documento RAG com chunks retorna 409. A API não impõe unicidade não observada, como um único feedback por execução ou um único chunkIndex por documento.

Erros possuem code/message e não incluem payload, URI ou mensagem bruta do driver. Veja README para autenticação, filtros e paginação.
