# Alfred - Financial Intelligence Core

## Visão Geral
O Alfred é um motor de inteligência financeira integrado ao WhatsApp, desenvolvido para
automatizar o controle de gastos e receitas. O projeto foca em fornecer uma experiência de
gestão financeira sem fricção, utilizando um parser customizado para converter mensagens de
texto em registros transacionais estruturados diretamente pelo chat.

## Arquitetura e Design
A aplicação foi construída utilizando os princípios da **Arquitetura Hexagonal (Ports &
Adapters)**, garantindo o desacoplamento total entre o domínio (regras de negócio) e as
tecnologias de infraestrutura.

*   **Domain Layer:** Contém as entidades core como `Transaction`, `MonthlyLimit` e
         `Category`, utilizando imutabilidade para garantir a integridade dos dados.
*   **Application Layer:** Implementa os casos de uso do sistema, como o registro de
         despesas (`RegisterExpenseService`) e a consulta de saldo (`CheckBalanceService`).
*   **Infrastructure Layer:** Adaptadores de entrada para Webhooks do WhatsApp (via **WAHA
         API**) e adaptadores de saída para persistência em **PostgreSQL**.
         
## Stack Tecnológica
*   **Linguagem:** Java 17
*   **Framework Principal:** Spring Boot 3.x
*   **Persistência:** PostgreSQL, Spring Data JPA e Hibernate
*   **Comunicação Externa:** Spring WebFlux (WebClient) para chamadas assíncronas à API do
    WhatsApp
*   **Infraestrutura:** Docker e Docker Compose
*   **Utilitários:** Project Lombok e Bean Validation

## Funcionalidades Principais
*   **Command Parser Inteligente:** Identificação automática de valores, categorias e
    intenções (despesa/receita) a partir de texto livre enviado via WhatsApp.
*   **Gestão Transacional:** Registro de histórico financeiro com persistência ACID no
    PostgreSQL.
*   **Controle de Teto de Gastos:** Sistema para definição e monitoramento de limites
    mensais por categoria.
*   **Consulta de Saldo:** Cálculo dinâmico do status financeiro através de use cases
    isolados.

## Configuração do Ambiente

### Requisitos
*   JDK 17 ou superior
*   Docker Desktop
*   Maven 3.8+

### Execução
1.  Inicie a infraestrutura necessária (Banco de Dados):
```bash
docker-compose up -d
```
2.  Configure as credenciais e URLs de conexão no arquivo `application.properties`.
3.  Execute a aplicação via Maven:
```bash
./mvnw spring-boot:run
```

## Decisões de Engenharia
A escolha pela **Arquitetura Hexagonal** permite que as regras de cálculo financeiro sejam
preservadas contra mudanças em tecnologias externas. O uso de **BigDecimal** em todas as
operações monetárias assegura a precisão decimal necessária para sistemas financeiros,
eliminando erros de arredondamento comuns em tipos de ponto flutuante.

**Nota Técnica:**
Este projeto prioriza a **separação de responsabilidades** e a **clareza do domínio**,
estabelecendo uma base sólida para a implementação futura de testes automatizados e logs de
auditoria.
