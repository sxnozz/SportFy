# Sportfy API

Backend desenvolvido para o gerenciamento de eventos esportivos, rastreamento de métricas de performance individuais e interações sociais. Projeto estruturado originalmente como Trabalho de Conclusão de Curso (TCC) no IFSul.

## Stack Tecnológica

O projeto utiliza um ecossistema Java moderno, focando em segurança e eficiência de mapeamento de dados.

*   **Java 21**
*   **Spring Boot 3.5.6** (Web, Data JPA)
*   **MySQL 8** (Banco de dados relacional)
*   **Gradle** (Automação de build e gestão de dependências)
*   **Spring Security (BCrypt)** (Hashing e segurança de credenciais)

## Arquitetura e Funcionalidades

*   **Gestão de Identidade:** Autenticação baseada em sessão com senhas criptografadas (BCrypt).
*   **Mapeamento Objeto-Relacional:** Utilização de Hibernate/JPA para sincronização de entidades e controle de integridade referencial.
*   **Regras de Negócio Isoladas:** Validação rigorosa de estado de eventos (ex: bloqueio de inscrições ou comentários em eventos expirados).
*   **Isolamento de Ambiente:** Credenciais de banco de dados injetadas via variáveis de ambiente, eliminando hardcoding no controle de versão.

## Como Executar Localmente

### 1. Requisitos
*   JDK 21 instalado e configurado no PATH.
*   Servidor MySQL rodando na porta `3306`.

### 2. Provisionamento do Banco de Dados
Não é necessário utilizar migrations complexas para testes locais. Execute o script DDL fornecido para estruturar as tabelas:
1. Abra seu cliente MySQL (Workbench, DBeaver, ou CLI).
2. Execute o conteúdo do arquivo `schema.sql` localizado na raiz deste repositório.

### 3. Injeção de Variáveis e Inicialização
O projeto exige que a senha do banco de dados seja fornecida pelo ambiente através da variável `DB_PASSWORD`.

**Via Terminal (PowerShell):**
```powershell
# Clone o repositório
git clone https://github.com/sxnozz/SportFy.git
cd SportFy

# Declare a credencial e inicie o servidor embutido (Tomcat)
$env:DB_PASSWORD="sua_senha_do_mysql"; .\gradlew bootRun