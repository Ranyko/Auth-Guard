# 🛡️ AuthGuard

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.1.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=docker&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=JSON%20web%20tokens)

AuthGuard é uma API robusta de Autenticação e Autorização projetada com foco em segurança defensiva e boas práticas de arquitetura. Este projeto foi desenvolvido para demonstrar o controle de acesso de ponta a ponta utilizando as ferramentas mais modernas do ecossistema Java.

## ✨ Principais Funcionalidades (MVP)

- **Login e Registro Seguros**: Senhas são criptografadas com `BCrypt` antes de irem para o banco de dados.
- **Tokens JWT**: Geração e validação de JSON Web Tokens utilizando algoritmos HMAC256.
- **RBAC (Role-Based Access Control)**: Controle rigoroso de permissões (`ROLE_ADMIN` e `ROLE_USER`). Usuários sem permissões adequadas recebem erro `403 Forbidden`.
- **Validação Defensiva**: Validação estrita de DTOs (`@Valid`, `@Pattern`, `@NotBlank`) barrando senhas fracas e dados corrompidos.
- **Para-Raios de Exceções**: Utilização de `@ControllerAdvice` para capturar falhas na aplicação e devolver JSONs padronizados ao invés de stacktraces técnicos (Segurança por Obscuridade).
- **Data Seeding**: O banco de dados é populado automaticamente com os Cargos padrão no momento em que a aplicação sobe pela primeira vez.
- **Infraestrutura em Containers**: Ambiente orquestrado via `docker-compose` contendo o banco de dados principal (Postgres) e o banco de sessões (Redis, preparado para a Fase 2).

## 🚀 Como Rodar o Projeto Localmente

### 1. Pré-requisitos
- Java 21+ instalado
- Docker e Docker Compose instalados

### 2. Subindo os Bancos de Dados
Abra o terminal na raiz do projeto e execute:
```bash
docker-compose up -d
```
Isso iniciará os containers do **PostgreSQL** (porta 5432) e do **Redis** (porta 6379).

### 3. Rodando a Aplicação
Com os bancos de dados rodando, você pode iniciar o servidor Spring Boot:
```bash
./mvnw spring-boot:run
```
A API estará disponível em `http://localhost:8080`.

## 🌐 Endpoints Disponíveis

| Método | Endpoint | Protegido? | Descrição |
|--------|----------|------------|-----------|
| `POST` | `/auth/register` | Não | Registra um novo usuário (Requer senha forte) |
| `POST` | `/auth/login` | Não | Retorna o Access Token (JWT) |
| `GET`  | `/admin/painel` | Sim (ADMIN) | Rota secreta protegida via Role-Based Access Control |

## 🏗️ Arquitetura e Decisões Técnicas
- **Security Filter Chain**: Migrado para a nova arquitetura do Spring Security 6, abandonando o obsoleto `WebSecurityConfigurerAdapter`.
- **Filtro Customizado**: Um `OncePerRequestFilter` foi implementado para interceptar requisições, validar assinaturas JWT e injetar os "Roles" do usuário diretamente no `SecurityContextHolder`.
- **Domain Driven**: A aplicação blinda as entidades do banco (`User`) utilizando DTOs para se comunicar com o cliente.
## 🔮 Próximos Passos (Evolução Contínua)
Apesar de ser um MVP totalmente funcional, este projeto está preparado para escalar. Os próximos recursos planejados incluem:
- **Fase 2**: Implementação de *Refresh Tokens* armazenados no Redis (com Rotação e Logout seguro).
- **Fase 4**: Autenticação Multifator (MFA) via TOTP (Google Authenticator).
- **Fase 5**: Rate Limiting e proteção contra Brute Force (travamento de contas).
- **Fase 6**: Audit Logging, registrando o IP e ações sensíveis de cada usuário no banco.

---
*Este é um projeto de estudo desenvolvido com o objetivo de demonstrar habilidades práticas em arquitetura, segurança e boas práticas de desenvolvimento no ecossistema Java/Spring.*
