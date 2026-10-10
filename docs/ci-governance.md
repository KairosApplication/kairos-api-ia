# CI e ruleset Kairos

Stack confirmada nos manifests das branches de entrega: Java 21, Spring Boot 4.0.7, Maven, MongoDB; ferramentas auxiliares Python e PowerShell.

O CI roda em push, PR (inclusive entre as branches empilhadas) e execução manual. Os checks obrigatórios são `Build and test`, `Analyze Java` e `Secret scan`.

- Maven Wrapper executa `clean verify`; publica relatórios Surefire. Os testes existentes usam mocks/configuração isolada, sem credenciais Atlas fornecidas pelo CI.
- CodeQL analisa Java sem build. Gitleaks verifica o histórico e oculta valores encontrados.
- Se os scripts estiverem presentes, executa seus testes Python e faz parse de PowerShell, sem executar os launchers.
- A main inicialmente contém somente LICENSE. Sem pom.xml e sem src, os jobs informam estado de bootstrap: isso não é evidência de testes Java aprovados. Quando a aplicação entrar, Maven e CodeQL passam a ser executados. src sem pom.xml falha.

`.github/rulesets/ruleset-kairos.json` é o payload de configuração, não um mecanismo de aplicação automática. O ruleset precisa existir nas configurações GitHub do repositório.

Origem: `KairosApplication/kairos-springboot`, ruleset `ruleset-kairos` (23551386). Mantém proteção contra exclusão/force-push, três aprovações, checks atualizados com a base, CodeQL, qualidade de código e revisão Copilot. Sem bypass. Os checks PostgreSQL foram substituídos pelos checks reais desta API; não há banco PostgreSQL neste repo.

Não há publicação de JAR, CD, Dependabot Maven ou auto-merge nesta configuração. As PRs existentes não são incorporadas automaticamente.
