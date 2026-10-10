# Evidências de validação

Data: 10/10/2026.

- JDK 21.0.11, Maven 3.9.16, Spring Boot 4.0.7.
- mvn -B -ntp verify: BUILD SUCCESS; 54 testes Java, zero falhas/erros; JAR executável gerado.
- Testes HTTP: contexto Spring, DispatcherServlet e filtros reais, com repository mockado; autenticação, status, JSON inválido e 9 recursos.
- Testes de serviço: criação, consultas, updates parciais, campos preservados, referências, conflitos, arrays, dados nulos, tipos heterogêneos e escritas desabilitadas.
- Testes do repository: Query/Update capturados, $set sem upsert, paginação limitada e recusa de criação implícita de collection.
- Python unittest: 4 testes da investigação com documentos sintéticos e mocks.
- Java no Atlas: ping e conversão dos 9 modelos por MongoConverter aprovados, amostra limitada; somente leituras.
- JAR com Tomcat NIO2: health e 9 listagens autenticadas retornaram 200; size=1; cada listagem leu um documento.
- Token temporário gerado só no processo de teste. Escritas desabilitadas. Processo HTTP encerrado após a verificação.
- Logs capturados não continham a URI, senha ou token usados.
- Conexão Java equivalente à SRV foi resolvida em memória, usando os hosts/opções de SRV/TXT reais e TLS. Contornou somente a incompatibilidade local do JNDI com loopback no processo empacotado.
- Launcher PowerShell com entrada oculta, restauração do ambiente e resolver DNS opcional. Nenhuma URI real persistida no projeto.
- 244 arquivos versionados PostgreSQL comparados com SHA-256 da linha de base; referência intacta.
- Nenhuma escrita, exclusão, migração ou criação de índice/collection foi executada no Atlas.

As operações de escrita foram testadas com mocks. Docker estava sem daemon ativo e não havia banco isolado autorizado disponível para comprovar CRUD integrado. Não se usou o banco real como substituto. O schema registrado é uma amostra e não comprova que todos os documentos possuem os mesmos tipos.
