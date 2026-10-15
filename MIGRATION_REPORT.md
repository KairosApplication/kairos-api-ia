# Comparação com a API PostgreSQL

Data: 10/10/2026. Referência: commit 86ea01d. Projeto MongoDB independente no diretório kairos-api-mongodb.

Após a investigação confirmar somente dados de IA, o usuário autorizou expressamente “Criar API para os dados de IA”. O resultado segue esse escopo. Nenhum arquivo da API PostgreSQL foi alterado e nenhuma collection comercial foi inventada.

## Estrutura real

| Collection | Documentos amostrados | Particularidades |
| --- | --- | --- |
| conversations | 2 | Datas BSON/string, message_id ObjectId/string, summary opcional |
| feedback | 5 | comment string/null, datas BSON/string |
| user_memory | 2 | Memórias embutidas, importance inteira/fracionária, datas BSON/string |
| agent_traces | 40 | input/output heterogêneos, métricas e model opcionais/nulos |
| guardrail_events | 32 | Referência a execução, datas BSON/string |
| judge_evaluations | 25 | Score e booleanos, datas BSON/string |
| rag_chunks | 2 | Referência a documento, embedding de números |
| ai_executions | 35 | Referência a conversa, métricas/resposta final opcionais/nulas |
| rag_documents | 1 | Metadados de fonte, active booleano |

Os números são amostras, não contagens totais. _id foi ObjectId e user_id BSON int32 em todos os casos observados. A inspeção encontrou somente o índice _id_ (único implicitamente) e nenhum validator. Evidência sanitizada: docs/MONGODB_SCHEMA.json. sample_mflix também estava acessível, mas é uma base de demonstração e não foi utilizada.

## Referências verificadas por leitura

| Referência | Registros examinados | Sem documento correspondente |
| --- | --- | --- |
| ai_executions.conversation_id → conversations | 35 | 0 |
| feedback.execution_id → ai_executions | 5 | 1 |
| agent_traces.execution_id → ai_executions | 40 | 2 |
| guardrail_events.execution_id → ai_executions | 32 | 1 |
| judge_evaluations.execution_id → ai_executions | 25 | 1 |
| rag_chunks.document_id → rag_documents | 2 | 2 |

Essas referências órfãs foram registradas e preservadas. A API exige pais existentes nas novas referências e protege exclusões de pais com dependentes; não altera documentos durante investigação nem oferece cascata.

## Funcionalidades comparadas

| Referência PostgreSQL | Nova API MongoDB |
| --- | --- |
| Controllers, services, repositories, DTOs, validação e erros | Mantida arquitetura em camadas, adaptada a documentos reais e Spring Data MongoDB |
| CRUD e consultas por ID/filtros | Implementados para as 9 collections de IA, com paginação limitada |
| IDs relacionais Long | ObjectId hexadecimal; userId numérico int32 preservado |
| Regras de vínculo e restrições de exclusão | Verificação de referências reais e conflitos antes de operações |
| Health | /health com ping, 200/503 e resposta sanitizada |
| Usuários, clientes, empresas, funcionários e cargos | Fora do novo domínio autorizado; não há collections correspondentes |
| Categorias, setores, produtos e localizações | Fora do domínio; saídas de agentes não são cadastros comerciais |
| Estoque, inventários, reposições e alertas comerciais | Fora do domínio; nenhum schema presumido |
| Compras, cálculo do total e recomendações comerciais | Fora do domínio; nenhuma estrutura de compras identificada |
| Login/sessão, CPF/email únicos, senha codificada e roles comerciais | Sem suporte de dados no MongoDB; autenticação Bearer de serviço, stateless |
| Cache/sessões Redis e transações relacionais | Não transportados para o novo domínio |
| Documentação de contratos | README e docs/API_CONTRACTS.md; inventário original em docs/REFERENCE_CONTRACTS.md |

Regras comerciais de preço/CPF/cargos não se aplicam aos documentos de IA. Regras novas como rating 1–5, score 0–1 e valores não negativos são políticas de entrada da API, não validators comprovados do banco. importance não é limitada a 1 porque a amostra inclui 5. Estados permanecem abertos. RAG é CRUD de dados; não implementa busca vetorial ou geração de embeddings.

## Conexão e execução

A URI posterior enviada pelo usuário conectou com TLS a kairos_ai. O arquivo externo original contém senha diferente e não foi modificado. O launcher pode receber a URI válida por entrada oculta, usando o mesmo arquivo externo para as demais configurações.

O Java iniciado dentro do aplicativo empacotado apresentou falha de loopback no JNDI SRV/TXT e no Tomcat NIO. Foi validada conexão equivalente montada em memória pelos registros DNS, com TLS, e Tomcat NIO2. O launcher oferece resolução DNS nativa opcional. Não houve bypass de IPs, alteração de permissões ou desativação de validação de certificados.

## Revisão final

Build aprovado com 54 testes Java e 4 testes Python. O JAR executável foi gerado. Java leu e converteu documentos das 9 collections; /health e as 9 listagens HTTP retornaram 200 com token temporário. A configuração usada nessa verificação manteve escritas desabilitadas. Nenhuma credencial apareceu nos logs capturados.

Os testes de escrita usam mocks; não provam escrita integrada em banco isolado. Não houve escrita no Atlas. A investigação não criou índices/collections nem reparou vínculos. Os 244 arquivos versionados PostgreSQL foram comparados por SHA-256 com a linha de base.
