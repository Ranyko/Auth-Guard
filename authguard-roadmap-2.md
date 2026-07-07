# 🛡️ AuthGuard — Roadmap Completo de Desenvolvimento

> API de autenticação e autorização em Java + Spring Boot, com JWT, Redis, MFA e audit logging.
> Objetivo: peça central do portfólio pra vaga de dev backend com pegada de segurança.

---

## 🎯 Visão Geral do Projeto

**O que é:** uma API de autenticação corporativa "de verdade" — não é só login/cadastro básico. É o tipo de sistema que empresa séria usa: tokens curtos, refresh rotativo, MFA, rate limiting, auditoria de tudo que acontece.

**Por que isso importa pro portfólio:** qualquer um faz um CRUD com JWT. Poucos juniors mostram que entendem *por que* token expira rápido, *por que* refresh token precisa rotacionar, *por que* logar tentativas de login é importante. Isso é o que separa "sei usar Spring Security" de "entendo segurança".

**Stack principal:**
- Java 21 + Spring Boot 3.x
- Spring Security 6 (arquitetura baseada em filtros, não WebSecurityConfigurerAdapter que tá deprecated)
- PostgreSQL (dados persistentes: users, roles, audit logs)
- Redis (cache de sessão, blacklist de tokens, rate limiting)
- JWT (jjwt ou nimbus-jose-jwt)
- Docker + Docker Compose (subir tudo junto: app, postgres, redis)
- Testcontainers (testes de integração reais, não mockados)
- Swagger/OpenAPI (documentação)

---

## 📐 Arquitetura em Alto Nível

```
Cliente → [Rate Limiter] → [Filtro JWT] → Controller → Service → Repository → Postgres
                                              ↓
                                        Audit Logger → Postgres
                                              ↓
                                     Redis (sessão/blacklist/cache)
```

Camadas:
- **Controller**: só recebe request e devolve response. Sem lógica de negócio.
- **Service**: regra de negócio (validar senha, gerar token, checar MFA).
- **Repository**: acesso a dados (Spring Data JPA).
- **Security**: filtros customizados, `UserDetailsService`, `PasswordEncoder`.
- **DTO**: nunca expor entidade JPA direto na API.

---

## 🗺️ Fases de Desenvolvimento

### **Fase 0 — Setup e Fundação** (1-2 dias)

Objetivo: ambiente rodando, projeto versionado, estrutura pronta.

- [ ] Criar projeto no Spring Initializr (Web, Security, JPA, PostgreSQL Driver, Validation, Redis)
- [ ] Configurar `docker-compose.yml` com Postgres + Redis
- [ ] Estruturar pacotes: `config`, `controller`, `service`, `repository`, `model`, `dto`, `security`, `exception`, `util`
- [ ] Configurar `application.yml` com profiles (`dev`, `test`, `prod`)
- [ ] Subir o projeto vazio, confirmar conexão com banco
- [ ] Configurar `.gitignore` decente (nada de `.env` ou `application-secrets.yml` indo pro Git)
- [ ] Primeiro commit com README inicial

**Entregável:** projeto sobe com `docker-compose up`, conecta no banco, sem funcionalidade ainda.

---

### **Fase 1 — Cadastro e Login Básico** (3-4 dias)

Objetivo: fluxo de autenticação funcionando, sem token ainda — só a base.

- [ ] Entidade `User` (id, email, senha hash, nome, roles, enabled, createdAt)
- [ ] Entidade `Role` (USER, ADMIN — relação many-to-many com User)
- [ ] `PasswordEncoder` com BCrypt (nunca, jamais, guardar senha em texto puro)
- [ ] Endpoint `POST /auth/register` — validação de email, senha forte (regex ou biblioteca), DTO de entrada/saída
- [ ] Endpoint `POST /auth/login` — autentica via `AuthenticationManager`
- [ ] Tratamento de exceções global com `@ControllerAdvice` (não deixar stacktrace vazar pro cliente)
- [ ] Validação de payload com Bean Validation (`@Valid`, `@NotBlank`, `@Email`)

**Conceito-chave pra entender, não só copiar:** por que BCrypt e não SHA-256 puro? (salt automático, custo computacional ajustável contra brute-force).

**Entregável:** dá pra registrar usuário e logar, retornando sucesso/erro — ainda sem token JWT.

---

### **Fase 2 — JWT: Access Token + Refresh Token** (4-5 dias)

Essa é a fase que mais separa junior de "sabe o que tá fazendo".

- [ ] Gerar **Access Token** JWT (curta duração: 15 min) com claims (userId, roles, exp)
- [ ] Gerar **Refresh Token** (duração maior: 7 dias), armazenado no Redis (não no JWT em si)
- [ ] Endpoint `POST /auth/refresh` — troca refresh token válido por novo par de tokens
- [ ] **Rotação de refresh token**: a cada refresh, o token antigo é invalidado e um novo é emitido (evita replay attack)
- [ ] Filtro JWT customizado (`OncePerRequestFilter`) que intercepta requests e valida o token
- [ ] Endpoint `POST /auth/logout` — invalida o refresh token (remove do Redis) e adiciona access token numa blacklist até expirar
- [ ] Configurar `SecurityFilterChain` (Spring Security 6, sem `WebSecurityConfigurerAdapter`)

**Conceito-chave:** por que dois tokens e não um só? Access token curto minimiza janela de ataque se vazar; refresh token fica seguro no servidor (Redis) e pode ser revogado a qualquer momento — coisa que um JWT sozinho não permite (JWT é stateless por natureza).

**Entregável:** login retorna access + refresh token. Rotas protegidas exigem token válido. Logout revoga sessão de verdade.

---

### **Fase 3 — RBAC (Controle de Acesso por Papel)** (2-3 dias)

- [ ] Anotações `@PreAuthorize("hasRole('ADMIN')")` em endpoints sensíveis
- [ ] Endpoint admin-only: `GET /admin/users` (listar todos os usuários)
- [ ] Endpoint admin-only: `PATCH /admin/users/{id}/disable` (desativar conta)
- [ ] Testar que usuário comum recebe 403 em rota admin
- [ ] Documentar matriz de permissões (quem pode fazer o quê) no README

**Entregável:** sistema diferencia claramente USER de ADMIN, com testes provando isso.

---

### **Fase 4 — MFA (Autenticação Multifator)** (5-6 dias)

A parte mais "sexy" pro portfólio — mostra que você vai além do básico.

- [ ] Implementar TOTP (Time-based One-Time Password) com biblioteca tipo `java-otp` ou `GoogleAuth`
- [ ] Endpoint `POST /auth/mfa/setup` — gera secret e retorna QR code (compatível com Google Authenticator)
- [ ] Endpoint `POST /auth/mfa/verify` — confirma o código de 6 dígitos e ativa MFA na conta
- [ ] Alterar fluxo de login: se MFA ativo, login retorna um "token temporário" que só serve pra validar o código MFA, não pra acessar recursos
- [ ] Endpoint `POST /auth/mfa/validate` — recebe token temporário + código TOTP, retorna os tokens de verdade
- [ ] Endpoint `POST /auth/mfa/disable` — desativa MFA (exigindo senha novamente, por segurança)
- [ ] Gerar códigos de backup (recovery codes) pra quando o usuário perde o segundo fator

**Conceito-chave:** por que TOTP e não SMS? TOTP não depende de operadora, funciona offline, e SMS é vulnerável a SIM swapping.

**Entregável:** login com MFA funcional, testável com app tipo Google Authenticator/Authy.

---

### **Fase 5 — Rate Limiting e Proteção contra Brute Force** (2-3 dias)

- [ ] Rate limiter no endpoint de login usando Redis (ex: bucket4j ou implementação própria com `INCR` + `EXPIRE`)
- [ ] Bloqueio temporário de conta após N tentativas falhas (ex: 5 tentativas → bloqueio de 15 min)
- [ ] Rate limiting global por IP nos endpoints públicos (`/auth/*`)
- [ ] Resposta padronizada `429 Too Many Requests` com header `Retry-After`

**Conceito-chave:** rate limiting não é só "travar depois de X tentativas" — é sobre não dar dica nenhuma pro atacante (mesma mensagem de erro pra "usuário não existe" e "senha errada", por exemplo).

**Entregável:** brute force fica inviável na prática; testes simulando ataque de força bruta passam.

---

### **Fase 6 — Audit Logging** (3-4 dias)

Esse é o diferencial que recrutador de segurança vai notar.

- [ ] Entidade `AuditLog` (id, userId, ação, IP, userAgent, timestamp, sucesso/falha, detalhes)
- [ ] Interceptar e logar: login (sucesso/falha), logout, mudança de senha, ativação/desativação de MFA, ações admin
- [ ] Usar `AOP` (`@Aspect`) pra capturar eventos sem poluir os services com código de log espalhado
- [ ] Endpoint `GET /admin/audit-logs` com paginação e filtros (por usuário, por período, por tipo de ação)
- [ ] Garantir que dados sensíveis (senha, tokens) NUNCA entram no log

**Conceito-chave:** audit log é sobre rastreabilidade forense — se rolar um incidente, dá pra reconstruir o que aconteceu.

**Entregável:** toda ação sensível fica registrada, consultável e paginada.

---

### **Fase 7 — Testes** (4-5 dias, mas espalhe ao longo do projeto)

Não deixa pra fazer tudo no final — mas se já chegou aqui sem testar tudo, hora de fechar a cobertura.

- [ ] Testes unitários dos services (JUnit 5 + Mockito) — lógica de negócio isolada
- [ ] Testes de integração com **Testcontainers** (sobe Postgres e Redis reais em container pra testar de verdade, não com H2 fake)
- [ ] Testes do fluxo completo de autenticação (register → login → refresh → logout)
- [ ] Testes de segurança: tentar acessar rota admin sem permissão, token expirado, token adulterado
- [ ] Cobertura mínima sugerida: 70-80% nas camadas de service e security

**Entregável:** suite de testes que roda com `mvn test`, incluindo cenários de ataque simulado.

---

### **Fase 8 — Documentação, Deploy e Polimento pro Portfólio** (3-4 dias)

- [ ] Documentação da API com **SpringDoc OpenAPI** (Swagger UI acessível em `/swagger-ui.html`)
- [ ] README completo: o que é, como rodar (`docker-compose up`), arquitetura (com diagrama), decisões técnicas explicadas, prints do Swagger
- [ ] Diagrama de arquitetura (pode usar draw.io, Excalidraw ou até um Mermaid no próprio README)
- [ ] Variáveis sensíveis via `.env` (nunca hardcoded), com `.env.example` documentado
- [ ] Deploy de demonstração (Render, Railway, ou Fly.io — todos têm free tier decente pra Java)
- [ ] Collection do Postman/Insomnia exportada e versionada no repo, pra quem for testar rapidinho
- [ ] Vídeo curto (2-3 min) demonstrando o fluxo, se quiser capricho extra pro LinkedIn

**Entregável:** repositório que impressiona um recrutador técnico em 5 minutos de leitura.

---

## 📁 Estrutura de Pastas Sugerida

```
authguard/
├── src/main/java/com/rani/authguard/
│   ├── config/          # SecurityConfig, RedisConfig, SwaggerConfig
│   ├── controller/      # AuthController, AdminController, MfaController
│   ├── service/         # AuthService, TokenService, MfaService, AuditService
│   ├── repository/      # UserRepository, AuditLogRepository
│   ├── model/           # User, Role, AuditLog (entidades JPA)
│   ├── dto/             # RegisterRequest, LoginResponse, etc
│   ├── security/        # JwtFilter, JwtUtil, UserDetailsServiceImpl
│   ├── exception/       # GlobalExceptionHandler, exceções customizadas
│   └── aspect/          # AuditAspect (AOP)
├── src/test/java/...    # espelha a estrutura acima
├── docker-compose.yml
├── .env.example
└── README.md
```

---

## 🔑 Lista de Endpoints (resumo final)

| Método | Rota | Protegida? | Descrição |
|---|---|---|---|
| POST | `/auth/register` | Não | Cadastro |
| POST | `/auth/login` | Não | Login (retorna tokens ou token temp se MFA ativo) |
| POST | `/auth/refresh` | Não* | Troca refresh token por novo par |
| POST | `/auth/logout` | Sim | Revoga sessão |
| POST | `/auth/mfa/setup` | Sim | Gera QR code TOTP |
| POST | `/auth/mfa/verify` | Sim | Confirma ativação do MFA |
| POST | `/auth/mfa/validate` | Não* | Valida código MFA no login |
| POST | `/auth/mfa/disable` | Sim | Desativa MFA |
| GET | `/admin/users` | Sim (ADMIN) | Lista usuários |
| PATCH | `/admin/users/{id}/disable` | Sim (ADMIN) | Desativa conta |
| GET | `/admin/audit-logs` | Sim (ADMIN) | Consulta logs |

*Não exige access token, mas exige refresh token ou token temporário válido.

---

## ⏱️ Estimativa de Tempo Total

- Ritmo tranquilo (fim de semana + algumas noites): **6-8 semanas**
- Ritmo intenso (dedicação diária): **3-4 semanas**

Não corre demais — o valor desse projeto não é terminar rápido, é conseguir explicar cada decisão numa entrevista técnica.

---

## 🎤 Perguntas que Você Precisa Saber Responder no Fim

Se alguém te perguntar isso numa entrevista e você travar, é sinal de que copiou sem entender:

1. Por que access token é curto e refresh token é longo?
2. Onde fica armazenado o refresh token e por quê?
3. O que impede alguém de reusar um refresh token roubado?
4. Como o BCrypt protege contra rainbow tables?
5. Por que TOTP funciona sem internet no momento da validação?
6. O que é um filtro (`OncePerRequestFilter`) e onde ele entra na cadeia do Spring Security?
7. Por que audit log não pode conter senha nem token, mesmo criptografado?

---

## 📌 Próximo Passo Imediato

Começa pela **Fase 0**. Não pula pra JWT sem ter o cadastro/login básico rodando e testado — a tentação de ir direto pra parte "legal" é grande, mas base mal feita quebra tudo lá na frente.

Bora? 🚀
