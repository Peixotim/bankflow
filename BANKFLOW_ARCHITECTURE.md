# BankFlow --- Documento de Arquitetura

> Documento de referência do projeto. Atualize-o conforme a
> implementação evoluir.

## 1. Visão geral

**BankFlow** é uma API bancária educacional desenvolvida com Java e
Spring Boot. O objetivo é simular operações bancárias e estudar
arquitetura de software, especialmente **CQRS**, persistência separada
para escrita e leitura, mensageria com Kafka e consistência eventual.

### Funcionalidades planejadas

-   Cadastro e consulta de clientes.
-   Criação e consulta de contas.
-   Cadastro e consulta de cartões.
-   Criação e consulta de faturas.
-   Pagamentos totais e parciais de faturas.
-   Histórico de pagamentos.
-   Ledger (registro financeiro/auditoria).
-   Processamento assíncrono de eventos.

## 2. Tecnologias

  Tecnologia                    Responsabilidade
  ----------------------------- -----------------------------
  Java 21                       Linguagem
  Spring Boot 3.5.x             Framework da aplicação
  Spring Web                    API REST
  Spring Data JPA / Hibernate   Persistência
  PostgreSQL                    Bancos de escrita e leitura
  Flyway                        Versionamento do schema
  Apache Kafka                  Mensageria e eventos
  Docker Compose                Ambiente local
  Maven                         Build e dependências
  Testcontainers                Testes de integração

Kafka será integrado à aplicação em uma etapa posterior. Não é
necessário implementar produtor e consumidor antes de concluir a
configuração dos bancos e o fluxo inicial.

## 3. Organização do código

A organização é por domínio de negócio. Cada domínio concentra suas
entidades, repositórios, comandos, consultas e controladores.

``` text
com.bankflow/
├── BankflowApplication.java
│
├── shared/
│   ├── config/
│   ├── exception/
│   └── security/
│
├── customer/
│   ├── entity/
│   ├── repository/
│   ├── command/
│   ├── query/
│   └── controller/
│
├── account/
│   ├── entity/
│   ├── repository/
│   ├── command/
│   ├── query/
│   └── controller/
│
├── card/
│   ├── entity/
│   ├── repository/
│   ├── command/
│   ├── query/
│   └── controller/
│
├── invoice/
│   ├── entity/
│   ├── repository/
│   ├── command/
│   ├── query/
│   └── controller/
│
├── payment/
│   ├── entity/
│   ├── repository/
│   ├── command/
│   ├── query/
│   ├── event/
│   └── controller/
│
├── ledger/
│   ├── entity/
│   └── repository/
│
└── outbox/
    ├── entity/
    ├── repository/
    └── publisher/
```

### Onde ficam os repositories?

Dentro do domínio correspondente. Exemplos:

-   `customer/repository/CustomerWriteRepository`
-   `customer/repository/CustomerReadRepository`
-   `account/repository/AccountWriteRepository`
-   `account/repository/AccountReadRepository`
-   `payment/repository/PaymentWriteRepository`
-   `payment/repository/PaymentReadRepository`

Os repositórios de escrita devem usar o banco `bankflow_write`; os
repositórios de leitura devem usar `bankflow_read`.

**Importante:** não é obrigatório criar os dois repositórios de todos os
domínios desde o primeiro dia. Crie apenas quando houver uma operação de
leitura ou escrita real para implementar.

Os pacotes de repositório precisam ser separados de forma que a
configuração JPA consiga associar cada grupo ao seu
`EntityManagerFactory` e `TransactionManager`. Uma convenção possível é
usar subpacotes `repository.write` e `repository.read` em cada domínio,
caso isso facilite o escaneamento do Spring.

## 4. CQRS: separação de escrita e leitura

CQRS significa **Command Query Responsibility Segregation**: separar
operações que alteram o estado do sistema (Commands) das operações que
consultam dados (Queries).

### Write side

-   Recebe comandos que alteram dados.
-   Aplica regras de negócio.
-   Valida saldo, limites, status e idempotência.
-   Persiste o estado oficial.
-   Registra eventos na tabela Outbox na mesma transação.

Banco: `bankflow_write`.

### Read side

-   Atende consultas.
-   Mantém projeções otimizadas para cada tela ou necessidade de
    consulta.
-   É atualizado a partir dos eventos publicados pelo Write side.
-   Pode apresentar pequeno atraso em relação ao banco de escrita.

Banco: `bankflow_read`.

### Regra fundamental

O banco de leitura é uma projeção e pode estar atrasado. **Nunca use o
Read DB para autorizar uma operação financeira.** Validações como saldo
disponível, limite e prevenção de pagamentos duplicados devem usar os
dados oficiais do Write DB.

## 5. Bancos de dados

O ambiente local possui dois bancos PostgreSQL independentes.

  Banco                Porta local Finalidade
  ------------------ ------------- -------------------------------
  `bankflow_write`            5432 Estado oficial e transacional
  `bankflow_read`             5433 Projeções para consultas

Cada banco tem seu próprio volume Docker e suas próprias migrations
Flyway.

### Migrations

``` text
src/main/resources/db/migration/
├── write/
│   └── V1__create_write_schema.sql
└── read/
    └── V1__create_read_schema.sql
```

O Hibernate deve validar o schema (`ddl-auto: validate`), não criar ou
alterar tabelas automaticamente. O Flyway é responsável pela evolução do
banco.

## 6. Modelo de dados --- Write DB

### 6.1 Customer

  Campo             Tipo conceitual   Regra
  ----------------- ----------------- ---------------------
  id                UUID              Chave primária
  full_name         VARCHAR(150)      Obrigatório
  email             VARCHAR(180)      Obrigatório e único
  document_number   VARCHAR(20)       Obrigatório e único
  created_at        TIMESTAMPTZ       Data de criação
  updated_at        TIMESTAMPTZ       Última atualização

### 6.2 Account

  Campo            Tipo conceitual   Regra
  ---------------- ----------------- -----------------------------------
  id               UUID              Chave primária
  customer_id      UUID              FK para Customer
  account_number   VARCHAR(30)       Único
  balance          NUMERIC(19,2)     Não pode ser negativo
  status           VARCHAR(20)       ACTIVE, BLOCKED ou CLOSED
  version          BIGINT            Controle otimista de concorrência
  created_at       TIMESTAMPTZ       Data de criação

### 6.3 Card

  Campo              Tipo conceitual   Regra
  ------------------ ----------------- ----------------------------------
  id                 UUID              Chave primária
  account_id         UUID              FK para Account
  last_four_digits   CHAR(4)           Apenas os últimos quatro dígitos
  credit_limit       NUMERIC(19,2)     Limite total
  available_limit    NUMERIC(19,2)     Limite disponível
  status             VARCHAR(20)       ACTIVE, BLOCKED ou CANCELLED
  created_at         TIMESTAMPTZ       Data de criação

Não armazenar PAN completo, CVV ou outros dados sensíveis de cartão
neste projeto.

### 6.4 Invoice

  Campo             Tipo conceitual   Regra
  ----------------- ----------------- ---------------------------------------
  id                UUID              Chave primária
  card_id           UUID              FK para Card
  reference_month   DATE              Mês de referência
  total_amount      NUMERIC(19,2)     Valor total maior que zero
  paid_amount       NUMERIC(19,2)     Entre zero e o total
  due_date          DATE              Vencimento
  status            VARCHAR(20)       OPEN, PARTIALLY_PAID, PAID ou OVERDUE
  created_at        TIMESTAMPTZ       Data de criação

Restrição: combinação de `card_id` e `reference_month` única.

### 6.5 Payment

  Campo             Tipo conceitual   Regra
  ----------------- ----------------- ------------------------------------------
  id                UUID              Chave primária
  invoice_id        UUID              FK para Invoice
  account_id        UUID              FK para Account
  amount            NUMERIC(19,2)     Maior que zero
  status            VARCHAR(20)       PENDING, PROCESSING, COMPLETED ou FAILED
  idempotency_key   VARCHAR(100)      Única
  created_at        TIMESTAMPTZ       Data da solicitação
  completed_at      TIMESTAMPTZ       Data de conclusão, opcional

A chave de idempotência impede que uma mesma solicitação seja processada
duas vezes por causa de retries do cliente.

### 6.6 LedgerEntry

  Campo         Tipo conceitual   Regra
  ------------- ----------------- ----------------------------------
  id            UUID              Chave primária
  account_id    UUID              FK para Account
  payment_id    UUID              Referência opcional para Payment
  entry_type    VARCHAR(20)       DEBIT ou CREDIT
  amount        NUMERIC(19,2)     Maior que zero
  description   VARCHAR(255)      Descrição do lançamento
  created_at    TIMESTAMPTZ       Data do lançamento

O ledger registra movimentos financeiros. Em sistemas financeiros,
lançamentos devem ser tratados como registros históricos: não apagar ou
editar lançamentos para corrigir o passado; registrar lançamentos
compensatórios quando necessário.

### 6.7 OutboxEvent

  Campo            Tipo conceitual   Regra
  ---------------- ----------------- ------------------------------
  id               UUID              Chave primária
  aggregate_id     UUID              Identificador do agregado
  aggregate_type   VARCHAR(100)      Tipo do agregado
  event_type       VARCHAR(150)      Nome do evento
  payload          JSONB             Conteúdo do evento
  created_at       TIMESTAMPTZ       Data de criação
  published_at     TIMESTAMPTZ       Data de publicação, opcional
  attempts         INTEGER           Tentativas de publicação

A tabela Outbox garante que a alteração de negócio e o registro do
evento sejam gravados na mesma transação do Write DB.

## 7. Modelo de dados --- Read DB

As tabelas abaixo são projeções de consulta. Elas não precisam de chaves
estrangeiras para o Write DB.

### CustomerDashboard

-   `customer_id` UUID --- chave primária.
-   `full_name` VARCHAR(150).
-   `email` VARCHAR(180).
-   `total_accounts` INTEGER.
-   `total_cards` INTEGER.
-   `updated_at` TIMESTAMPTZ.

### AccountBalanceView

-   `account_id` UUID --- chave primária.
-   `customer_id` UUID.
-   `account_number` VARCHAR(30).
-   `balance` NUMERIC(19,2).
-   `account_status` VARCHAR(20).
-   `updated_at` TIMESTAMPTZ.

### CardInvoiceView

-   `invoice_id` UUID --- chave primária.
-   `card_id` UUID.
-   `customer_id` UUID.
-   `last_four_digits` CHAR(4).
-   `reference_month` DATE.
-   `total_amount` NUMERIC(19,2).
-   `paid_amount` NUMERIC(19,2).
-   `remaining_amount` NUMERIC(19,2).
-   `due_date` DATE.
-   `invoice_status` VARCHAR(20).
-   `updated_at` TIMESTAMPTZ.

### PaymentHistoryView

-   `payment_id` UUID --- chave primária.
-   `invoice_id` UUID.
-   `account_id` UUID.
-   `customer_id` UUID.
-   `amount` NUMERIC(19,2).
-   `payment_status` VARCHAR(20).
-   `created_at` TIMESTAMPTZ.
-   `completed_at` TIMESTAMPTZ, opcional.
-   `updated_at` TIMESTAMPTZ.

## 8. Relacionamentos

``` text
Customer 1 ─── N Account
Account  1 ─── N Card
Card     1 ─── N Invoice
Invoice  1 ─── N Payment
Account  1 ─── N Payment
Account  1 ─── N LedgerEntry
Payment  1 ─── N LedgerEntry (conceitualmente)
```

As projeções do Read DB guardam os identificadores necessários para
consulta, sem depender de relacionamentos JPA com entidades do Write DB.

## 9. Fluxo de pagamento de fatura

Fluxo planejado:

1.  Cliente envia `POST /api/invoices/{invoiceId}/payments`.
2.  API recebe o comando e a chave de idempotência.
3.  Command Handler consulta e valida os dados no Write DB.
4.  A aplicação valida conta, saldo, fatura, valor e duplicidade.
5.  Na mesma transação, persiste o pagamento e o evento Outbox.
6.  Um Outbox Publisher publica o evento no Kafka.
7.  Um consumidor processa o evento e atualiza as projeções no Read DB.
8.  Cliente consulta o estado por `GET /api/payments/{paymentId}`.

### Estados e resposta HTTP

O endpoint pode responder `202 Accepted` com status `PROCESSING` quando
o processamento for assíncrono. O recebimento da mensagem pelo Kafka,
por si só, **não significa que o pagamento foi concluído**.

A aplicação só deve marcar o pagamento como `COMPLETED` depois que as
regras e alterações financeiras correspondentes forem efetivamente
concluídas.

## 10. Kafka e eventos

Kafka será usado para transportar eventos de domínio entre o
processamento de escrita e a atualização das projeções.

Evento inicial sugerido:

-   `InvoicePaymentRequested` --- solicitação de pagamento registrada.
-   `InvoicePaymentCompleted` --- pagamento concluído.
-   `InvoicePaymentFailed` --- pagamento recusado ou falhou.

Tópico inicial sugerido: `invoice-payment-events`.

Ambiente local atual: broker único em modo KRaft, sem ZooKeeper. É
adequado para aprendizado, mas não oferece alta disponibilidade nem
tolerância a falha de broker. Não representa uma configuração de
produção.

### Garantias que precisam ser implementadas

-   Outbox Publisher com retry.
-   Consumidores idempotentes.
-   Tratamento de mensagens duplicadas.
-   Estratégia de ordenação por agregado quando necessário.
-   Dead Letter Topic/Queue para mensagens que não possam ser
    processadas.
-   Observabilidade de falhas e atrasos.

Kafka não é a fonte da verdade financeira. O Write DB continua sendo a
fonte oficial do estado.

## 11. Regras de integridade e concorrência

-   Valores monetários devem usar `BigDecimal` no Java e `NUMERIC(19,2)`
    no PostgreSQL.
-   Identificadores devem usar UUID.
-   Datas com horário devem ser armazenadas com timezone (`TIMESTAMPTZ`)
    e representadas com `OffsetDateTime` ou `Instant`.
-   Datas de calendário, como vencimento e mês de referência, devem usar
    `LocalDate`.
-   O saldo não pode ficar negativo.
-   Pagamentos devem ser positivos e não podem exceder o valor em aberto
    da fatura.
-   A chave de idempotência deve ser única.
-   Operações concorrentes na mesma conta/fatura precisam de controle
    transacional e de concorrência (`@Version` ou bloqueio pessimista,
    conforme o caso).
-   Não confiar em dados do Read DB para validar comandos financeiros.

## 12. Endpoints planejados

  Método   Endpoint                         Responsabilidade
  -------- -------------------------------- -------------------------------
  POST     `/api/customers`                 Criar cliente
  GET      `/api/customers/{id}`            Consultar cliente
  POST     `/api/accounts`                  Criar conta
  GET      `/api/accounts/{id}`             Consultar conta
  POST     `/api/cards`                     Criar cartão
  GET      `/api/cards/{id}`                Consultar cartão
  GET      `/api/invoices/{id}`             Consultar fatura
  POST     `/api/invoices/{id}/payments`    Solicitar pagamento
  GET      `/api/payments/{id}`             Consultar status do pagamento
  GET      `/api/customers/{id}/payments`   Consultar histórico

Os endpoints são uma proposta inicial e podem mudar conforme o domínio
for implementado.

## 13. Configuração local

Serviços Docker:

  Serviço            Container                     Porta local
  ------------------ --------------------------- -------------
  PostgreSQL Write   `bankflow-postgres-write`            5432
  PostgreSQL Read    `bankflow-postgres-read`             5433
  Kafka              `bankflow-kafka`                     9092

Conexões: - Aplicação executada na máquina: PostgreSQL em
`localhost:5432` e `localhost:5433`; Kafka em `localhost:9092`. -
Aplicação executada na mesma rede Docker Compose: Kafka pelo endereço
`kafka:29092`.

Credenciais devem vir de variáveis de ambiente. Não versionar `.env` com
senhas reais. Manter `.env.example` com valores ilustrativos.

## 14. Ordem sugerida de implementação

1.  Subir os containers e validar os dois PostgreSQL e o Kafka.
2.  Configurar dois DataSources, dois EntityManagerFactories e dois
    TransactionManagers.
3.  Configurar os repositórios de escrita e leitura para os bancos
    corretos.
4.  Executar e validar as migrations Flyway dos dois bancos.
5.  Implementar Customer e Account no Write DB.
6.  Implementar consultas e projeções básicas no Read DB.
7.  Implementar Card e Invoice.
8.  Implementar Payment com transação, idempotência e concorrência.
9.  Implementar Ledger.
10. Implementar Outbox Publisher.
11. Integrar Kafka e criar consumidores idempotentes.
12. Criar testes unitários e de integração com Testcontainers.
13. Adicionar observabilidade, segurança e documentação da API.

## 15. Evoluções futuras

Depois do fluxo principal funcionando, evoluir gradualmente:

-   Redis para cache ou controle auxiliar, sem substituir a fonte
    oficial de dados.
-   Kafka com múltiplos brokers e replicação em ambiente apropriado.
-   Retry com backoff e Dead Letter Topic.
-   Outbox Publisher mais robusto e monitorado.
-   Métricas, tracing distribuído e logs estruturados.
-   Testes de carga e análise de gargalos.
-   Separação em microsserviços apenas se houver justificativa
    arquitetural.

## 16. Decisões arquiteturais importantes

1.  CQRS separa responsabilidades de escrita e leitura; não significa
    obrigatoriamente microsserviços.
2.  Write DB é a fonte oficial dos dados.
3.  Read DB contém projeções eventualmente consistentes.
4.  Kafka transporta eventos, mas não confirma sozinho a conclusão de
    uma operação.
5.  Outbox evita o problema de gravar no banco e falhar ao publicar o
    evento.
6.  Idempotência e controle de concorrência são requisitos essenciais em
    pagamentos.
7.  A arquitetura deve crescer conforme a necessidade; não implementar
    complexidade futura antes de validar o fluxo básico.
