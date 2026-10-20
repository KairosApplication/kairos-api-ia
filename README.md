# Kairos API MongoDB

API independente para os dados de IA de `kairos_ai`, criada com Java 21, Spring Boot 4.0.7, Spring Data MongoDB, Spring Security, Jakarta Validation e Maven Wrapper. A API PostgreSQL foi consultada como referência e permanece intacta.

A modelagem corresponde às 9 collections realmente investigadas. Conversas, execuções, memória, feedback, rastreamentos, guardrails, avaliações e documentos/chunks RAG possuem consulta, criação, atualização parcial e exclusão. Esta API gerencia dados existentes; não executa agentes, inferência, busca vetorial ou download de fontes RAG.

## Executar

Configure JDK 21 em `JAVA_HOME`. O launcher usa o arquivo externo original `~/Downloads/atlas-credentials.env` e não copia credenciais. A URI posterior enviada no chat conectou ao banco; sua senha difere da configuração original do arquivo. Esse arquivo não foi alterado. Para usar a URI válida sem gravá-la, execute e informe-a na entrada oculta:

```powershell
./scripts/run.ps1 -Database kairos_ai -PromptUri -PromptToken -ResolveSrv
```

Informe também um token próprio da API com pelo menos 32 caracteres. Ele será usado em `Authorization: Bearer <token>`. As credenciais do MongoDB não são o token da API. Os valores ficam somente no processo e o launcher restaura o ambiente ao terminar.

`-ResolveSrv` resolve os registros SRV/TXT pelo DNS nativo do Windows, compondo em memória a conexão equivalente com TLS. Isso evita a falha de loopback do resolvedor JNDI observada no Java iniciado pelo Codex empacotado. Não fixa hosts nem desativa validação de certificados. Fora desse ambiente, a URI SRV pode ser usada diretamente, sem esse parâmetro. O HTTP usa Tomcat NIO2, validado neste computador.

Se o arquivo externo já contém uma URI válida:

```powershell
./scripts/run.ps1 -EnvFile 'C:/caminho/credenciais.env' -PromptToken
```

Também é possível exportar as variáveis no processo e executar `./mvnw.cmd spring-boot:run` ou `./mvnw spring-boot:run` no Linux/macOS. As variáveis do processo prevalecem sobre o arquivo externo. Valores entre aspas simples/duplas no arquivo são aceitos. Não use senhas como argumentos de terminal ou em arquivos versionados.

| Variável | Comportamento |
| --- | --- |
| MONGODB_URI | URI com autenticação; obrigatória |
| MONGODB_DATABASE | Banco explícito; launcher usa kairos_ai, configuração Java usa o banco da URI quando omitido |
| APP_ENV_FILE | Importação de arquivo externo definida pelo launcher |
| API_PORT | Porta HTTP, padrão 8081 |
| API_ACCESS_TOKEN | Token Bearer de serviço, mínimo 32 caracteres; vazio bloqueia todas as rotas de dados |
| APP_MONGODB_VERIFY_ON_STARTUP | true por padrão; executa ping na inicialização |
| APP_MONGODB_WRITES_ENABLED | false por padrão; POST/PATCH/DELETE retornam 403 |

As escritas permanecem desabilitadas na configuração padrão. Para experimentar CRUD completo, configure uma URI/banco de testes isolado e autorizado e habilite `APP_MONGODB_WRITES_ENABLED=true`. As collections correspondentes precisam existir nesse banco: a API não cria collections ou índices automaticamente. Os testes automatizados usam mocks e não precisam dessa opção nem do Atlas.

## Endpoints

Todas as rotas de dados exigem Bearer. `GET /health` é público: 200 com `{"status":"UP"}` após ping, ou 503 com `{"status":"DOWN"}`.

| Rota base | Collection | Filtros além de page/size |
| --- | --- | --- |
| /api/v1/conversations | conversations | userId, status |
| /api/v1/feedback | feedback | userId, executionId |
| /api/v1/user-memories | user_memory | userId |
| /api/v1/agent-traces | agent_traces | executionId, status |
| /api/v1/guardrail-events | guardrail_events | executionId |
| /api/v1/judge-evaluations | judge_evaluations | executionId |
| /api/v1/rag-chunks | rag_chunks | documentId |
| /api/v1/ai-executions | ai_executions | userId, conversationId, status |
| /api/v1/rag-documents | rag_documents | active |

Cada rota base oferece:

- GET: listagem paginada; exemplo `/api/v1/ai-executions?page=0&size=20&userId=1`.
- GET /{id}: consulta individual.
- POST: criação, resposta 201 e cabeçalho Location.
- PATCH /{id}: atualização dos campos fornecidos, resposta 200.
- DELETE /{id}: exclusão, resposta 204; dependências impedem excluir pais com 409.

IDs são strings hexadecimais de 24 caracteres. A listagem retorna `{"items":[],"page":0,"size":20,"hasNext":false}`, ordenada por _id; size vai de 1 a 100 e page de 0 a 10000. Filtros desconhecidos retornam 400. Não há contagem total automática.

O JSON usa camelCase; o banco mantém snake_case. Consulte [docs/API_CONTRACTS.md](docs/API_CONTRACTS.md) para campos, exemplos e regras. Erros retornam code/message: 400 para entrada inválida, 401 para token ausente/incorreto, 403 para escritas desabilitadas, 404 para recurso/documento ausente, 409 para conflito e 503 para indisponibilidade MongoDB.

## Modelagem e regras

Os modelos Spring Data são específicos de cada collection. userId mantém BSON int32; IDs de referência usam ObjectId. Datas existentes podem ser BSON Date ou string: leituras preservam strings legadas e apresentam BSON Date como ISO-8601. Novas datas devem ser ISO-8601 com fuso e são persistidas como BSON Date. messageId legado aceita string ou ObjectId, sem migração de dados.

PATCH usa $set, preserva campos não enviados e campos desconhecidos existentes. Objetos input/output são atualizados pelos caminhos enviados; arrays são substituídos integralmente. Arrays não aceitam null; use [] para esvaziar. Campos opcionais aceitam null. Campos obrigatórios não podem ser anulados. Não se altera id/_id e campos desconhecidos de entrada são rejeitados.

Novas referências precisam existir. userId deve coincidir com a conversa/execução referenciada quando aplicável. Há referências órfãs nos dados atuais; consultas continuam funcionando e alterações sem mudar a referência não exigem reparação. Não há exclusão em cascata. As verificações entre collections não são transações e não garantem integridade contra alterações concorrentes de outros clientes.

Token Bearer concede acesso de serviço aos recursos: não representa login de usuário ou autorização por proprietário. Não foi criada collection de usuários, nem presumido que userId corresponde a um cadastro neste banco. Estados e categorias são strings abertas, porque a amostra não estabelece enumerações completas.

## Estrutura

- config: conexão, seleção do banco, segurança, servidor HTTP e probe de leitura.
- controller: health e rotas HTTP.
- service: validação, referências, CRUD e limites.
- repository: operações MongoTemplate em collections fixas.
- model: documentos e objetos embutidos observados.
- dto/request e mapper: contratos JSON e conversão dos tipos BSON.
- exception: erros sanitizados.
- scripts: launcher e ferramenta auxiliar de investigação.
- docs: schema observado, contratos e comparação com a referência.

Não há dependência de arquivos PostgreSQL, JPA, Flyway ou Redis. TLS e validação de hostname permanecem ativos. Logs do driver são desativados e erros não retornam URI, senha ou mensagens brutas do driver.

## Testes e investigação

```powershell
./mvnw.cmd -B -ntp verify
python -m unittest discover -s scripts -p 'test_*.py' -v
```

54 testes Java passaram, incluindo os 9 recursos, contratos HTTP com segurança real, validações, consultas e updates capturados do repository, referências, exclusões e conversão de datas/IDs. Quatro testes Python passaram. Escritas são simuladas; não houve teste de escrita no Atlas. Conexão Java, conversão dos 9 modelos e consultas HTTP das 9 rotas foram verificadas no banco real somente com leitura.

Python/PyMongo serviram apenas à investigação auxiliar de schema e ao diagnóstico de conexão; a API e o launcher não dependem de Python:

```powershell
python -m pip install -r scripts/requirements.txt
python scripts/inspect_mongodb.py --database kairos_ai
```

O script usa o mesmo arquivo externo, aceita URI exportada no processo, examina até 40 documentos por collection e mostra campos/tipos/ocorrências, sem valores de documentos. Veja [docs/MONGODB_SCHEMA.json](docs/MONGODB_SCHEMA.json), [MIGRATION_REPORT.md](MIGRATION_REPORT.md) e [docs/VALIDATION.md](docs/VALIDATION.md).

<!-- docs-bot:start -->
## Documentação

- [Contratos de dados de IA](docs/API_CONTRACTS.md)
- [Contratos da API PostgreSQL de referência](docs/REFERENCE_CONTRACTS.md)
- [Evidências de validação](docs/VALIDATION.md)
- [CI e ruleset Kairos](docs/ci-governance.md)
<!-- docs-bot:end -->
