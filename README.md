# Colégio Sapiens

Aplicação Spring Boot com interface web e API para gestão dos cadastros escolares.

## Requisitos

- JDK 21 e Maven 3.9 ou superior.
- MySQL em execução e uma base de dados dedicada à aplicação.

## Primeira execução

1. Crie uma base vazia no MySQL, por exemplo:

   ```sql
   CREATE DATABASE colegio_sapiens CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

2. Configure as variáveis de ambiente para a ligação MySQL e para a conta inicial. Em PowerShell, por exemplo:

   ```powershell
   $env:DB_URL = 'jdbc:mysql://localhost:3306/colegio_sapiens?connectionTimeZone=UTC'
   $env:DB_USER = 'utilizador_mysql'
   $env:DB_PASSWORD = 'palavra-passe_mysql'
   $env:ADMIN_USER = 'admin'
   $env:ADMIN_PASSWORD = 'uma-palavra-passe-forte'
   ```

   O utilizador MySQL precisa de permissões para criar e alterar tabelas durante a inicialização e para ler e escrever dados durante a utilização. `PORT` e `ADMIN_NAME` são opcionais.

3. **Apenas para uma base nova e vazia**, inicialize as tabelas com as migrações Flyway:

   ```powershell
   mvn spring-boot:run "-Dspring-boot.run.arguments=--spring.flyway.enabled=true"
   ```

   O Flyway aplica os scripts em `src/main/resources/db/migration` e a aplicação cria a conta administrativa inicial. As migrações não estão ativas por padrão no ambiente local, para preservar bases existentes. Uma base já preenchida sem histórico Flyway requer backup, comparação do esquema e um plano de migração específico; não faça baseline automático.

4. Nas execuções seguintes, inicie normalmente com `mvn spring-boot:run`. A configuração padrão apenas valida o esquema, sem o criar ou modificar. Abra <http://localhost:8080> (ou a porta configurada).

As credenciais iniciais só são usadas ao criar a conta: alterar `ADMIN_PASSWORD` posteriormente não muda a palavra-passe de uma conta existente. O bootstrap só cria uma conta quando a tabela de utilizadores está vazia; se já existir algum utilizador, não cria outra conta, mesmo que não haja administrador. A aplicação possui valores padrão de desenvolvimento para o administrador e a ligação à base de dados; configure credenciais próprias antes de a disponibilizar. A API usa autenticação HTTP Basic, pelo que deve ser disponibilizada através de HTTPS em ambientes reais.

## Publicar no Render

1. Publique o projeto no GitHub, incluindo `render.yaml`, `Dockerfile`, `.dockerignore` e `src/main/resources/application-render.properties`. Não publique palavras-passe.
2. Prepare uma base MySQL 8 persistente, acessível pelo Render. A base local `localhost` do seu computador não é acessível a partir do serviço. Pode usar um fornecedor externo ou seguir o [guia do Render para MySQL](https://render.com/docs/deploy-mysql); consulte preços, backups e requisitos de rede antes de criar recursos. Para dados existentes, faça backup e migre-os separadamente.
3. No Render, selecione **New > Blueprint** e ligue o repositório. O `render.yaml` define um serviço Docker que fornece a interface e a API, com health check em `/api/health`. O plano gratuito é destinado a testes; consulte as suas limitações e custos antes de usar outro plano ou dados reais. A base MySQL é configurada separadamente e pode ter custos.
4. Ao criar o Blueprint, preencha os valores solicitados; `SPRING_PROFILES_ACTIVE` já está definido no ficheiro. Pode ajustar as variáveis na página **Environment** do serviço:

   | Variável | Valor |
   | --- | --- |
   | `SPRING_PROFILES_ACTIVE` | `render` |
   | `DB_URL` | URL JDBC fornecida para a base, por exemplo `jdbc:mysql://servidor:3306/colegio_sapiens?connectionTimeZone=UTC` |
   | `DB_USER` | Utilizador MySQL da aplicação |
   | `DB_PASSWORD` | Palavra-passe desse utilizador |
   | `ADMIN_USER` | Nome da conta inicial de administrador |
   | `ADMIN_PASSWORD` | Palavra-passe inicial forte e exclusiva |
   | `ADMIN_NAME` | Nome do administrador (opcional) |
   | `ADMIN_BOOTSTRAP_ENABLED` | `true` apenas para criar a primeira conta; por padrão é `false` no Render |

   Use os requisitos de TLS e a URL do fornecedor da base de dados. O Render define `PORT` automaticamente. Não coloque segredos no GitHub. O perfil `render` exige as variáveis de ligação e não usa as credenciais locais do administrador. `ADMIN_NAME` e `ADMIN_BOOTSTRAP_ENABLED` podem ser adicionados na página Environment.
5. Para uma **base nova e vazia**, configure temporariamente `ADMIN_BOOTSTRAP_ENABLED=true` na página Environment e faça deploy. Use uma única instância durante a inicialização. O Flyway criará as tabelas e o bootstrap criará a conta inicial. Depois de confirmar o acesso, remova `ADMIN_BOOTSTRAP_ENABLED` e faça novo deploy. Não configure `SPRING_JPA_HIBERNATE_DDL_AUTO=update`: o Flyway trata as alterações e o Hibernate valida o esquema. Para uma base existente sem histórico Flyway, pare e prepare uma migração após backup e comparação do esquema; não ative baseline automático.
6. Aguarde o deploy e abra a URL HTTPS fornecida pelo Render. Verifique os logs se houver falha de ligação ou validação da base.

O bootstrap não é um mecanismo de recuperação: se já existirem utilizadores, não cria uma nova conta administrativa. Alterar `ADMIN_PASSWORD` no Render não altera a senha de uma conta já criada. Antes de usar dados reais, configure backups, proteção contra tentativas repetidas de autenticação no perímetro (abrangendo todos os endpoints com HTTP Basic), monitorização e uma política de acesso, retenção e privacidade. A aplicação ainda não implementa limitação de tentativas. Avalie também as limitações e custos do plano.

Todos os clientes da API devem enviar `X-Requested-With: XMLHttpRequest` em pedidos POST, PUT, PATCH e DELETE. A interface já o faz. Essa proteção depende de não permitir CORS a origens não confiáveis; não habilite CORS indiscriminadamente.

## Testes

Execute `mvn test`. Os testes normais usam H2 em memória. O teste de integração MySQL é opcional e exige uma **base de testes nova, vazia e descartável**, e as variáveis `RUN_MYSQL_TESTS=true`, `MYSQL_TEST_URL`, `MYSQL_TEST_USER` e `MYSQL_TEST_PASSWORD`. O Flyway cria tabelas e regista migrações nessa base. Nunca o execute contra dados de produção. O workflow do GitHub executa também este teste numa instância MySQL 8 isolada.
