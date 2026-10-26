# App Saúde — Back-end

API REST do **App Saúde** (TCC): agendamento e acompanhamento de consultas entre **pacientes**, **médicos** e **atendentes**.

Front-end (Angular): [appSaudeFrontEnd](https://github.com/joaopedrofuhr45-cmd/appSaudeFrontEnd)

## Stack

Java 17 · Spring Boot 4 · Spring Security + JWT · Spring Data JPA · Flyway · PostgreSQL (produção) · H2 (desenvolvimento) · Maven

## Como funciona

```
Navegador (Angular)
   │  cookie HttpOnly com o JWT (withCredentials)
   ▼
JwtAuthenticationFilter ──► SecurityConfig (regras por perfil)
   │
   ▼
controller ──► service ──► repository ──► banco (migrations Flyway)
```

- **Perfis (`role`)**: `USUARIO` (paciente), `MEDICO` e `ATENDENTE`. Todas as contas ficam em `auth_users`; cada perfil tem sua tabela com os dados próprios.
- **Login** por e-mail e senha (ou Google). O JWT vai num cookie `HttpOnly`, nunca no corpo da resposta.
- **Médicos e atendentes** não se cadastram: são criados direto no banco. Só o paciente tem cadastro (`POST /auth/cadastro`).
- **Foto de perfil** guardada no banco (`foto_perfil`), JPG/PNG/WebP até 2 MB, com o tipo conferido pelos bytes do arquivo.
- **Limite de tentativas de login**: 5 falhas por IP + e-mail (e 30 por IP) em 15 min bloqueiam com `429` e `Retry-After`.

Organização do código (`src/main/java/com/example/appsaudebackend`):

```
modules/
  auth/        login, cadastro, JWT, limite de tentativas
  paciente/    perfil, consultas e busca de pacientes
  medico/      perfil e agenda do médico, listagem de médicos/especialidades
  atendente/   perfil do atendente
  consulta/    regras de agendamento, cancelamento e finalização
  perfil/      foto de perfil (vale para os três perfis)
shared/
  exception/   erros de negócio e GlobalExceptionHandler
```

## Rodando localmente

Pré-requisito: JDK 17.

1. Copie `.env.example` para `.env` e ajuste (principalmente o `JWT_SECRET`).
2. Rode:
   ```bash
   ./mvnw spring-boot:run
   ```
3. A API sobe em `http://localhost:8080` com o banco H2 em memória (perfil `dev`). Console do H2 em `/h2-console`.

No Windows (PowerShell), sem `.env`: `$env:JWT_SECRET="seu-segredo-longo"; ./mvnw spring-boot:run`

Testes: `./mvnw test`

## Variáveis de ambiente

| Variável | Para quê | Padrão |
|---|---|---|
| `JWT_SECRET` | Chave de assinatura do JWT (obrigatória) | — |
| `JWT_EXPIRATION` | Validade do token em ms | `86400000` (24 h) |
| `CORS_ALLOWED_ORIGINS` | Origem(ns) do front, separadas por vírgula, sem `/` no fim | `http://localhost:4200` |
| `COOKIE_SECURE` | Cookie só via HTTPS | `false` |
| `COOKIE_SAME_SITE` | `Lax`, ou `None` se front e back estão em domínios diferentes (exige `COOKIE_SECURE=true`) | `Lax` |
| `GOOGLE_CLIENT_ID` | Login com Google | vazio |
| `SPRING_PROFILES_ACTIVE` | `dev` (H2) ou `prod` (Postgres) | `dev` |
| `SPRING_DATASOURCE_URL` / `_USERNAME` / `_PASSWORD` | Banco de produção | — |
| `LOGIN_MAX_TENTATIVAS_CONTA` / `LOGIN_MAX_TENTATIVAS_IP` / `LOGIN_JANELA_MINUTOS` | Limite de tentativas de login | `5` / `30` / `15` |

Em produção, o schema é gerenciado só pelo Flyway (`ddl-auto: validate`): novas tabelas e colunas entram como migrations em `src/main/resources/db/migration`. Mais detalhes de deploy em [DEPLOYMENT.md](DEPLOYMENT.md).

## Endpoints

| Método e rota | Quem acessa |
|---|---|
| `POST /auth/login`, `POST /auth/google`, `POST /auth/cadastro`, `GET /auth/csrf` | Público |
| `POST /auth/logout`, `GET /auth/me` | Logado |
| `GET /consultas` | Atendente |
| `POST /consultas` | Paciente, atendente |
| `GET /consultas/{id}` | Paciente, médico, atendente |
| `PATCH /consultas/{id}/cancelar` | Paciente, atendente |
| `PATCH /consultas/{id}/finalizar` | Médico |
| `GET /medicos`, `GET /especialidades` | Paciente, médico, atendente |
| `GET /pacientes` | Atendente |
| `/pacientes/me` (`GET`, `PUT`), `PATCH /pacientes/me/senha`, `/pacientes/me/foto` (`PUT`, `GET`, `DELETE`) | Paciente |
| `GET /pacientes/me/consultas`, `GET /pacientes/me/consultas/historico` | Paciente |
| `/medicos/me` (`GET`, `PUT`), `PATCH /medicos/me/senha`, `/medicos/me/foto`, `GET /medicos/me/consultas?data=` | Médico |
| `/atendentes/me` (`GET`, `PUT`), `PATCH /atendentes/me/senha`, `/atendentes/me/foto` | Atendente |

## CSRF

O JWT vai em cookie, então o CSRF fica **ligado**. Toda requisição que altera dados (`POST`, `PUT`, `PATCH`, `DELETE`) precisa mandar o token no header `X-XSRF-TOKEN`, junto com o cookie `XSRF-TOKEN`. Só `login`, `google` e `cadastro` ficam de fora (acontecem antes de existir sessão).

1. O front chama `GET /auth/csrf` (com `withCredentials`). A resposta traz `{ "headerName": "X-XSRF-TOKEN", "token": "..." }` e o cookie `XSRF-TOKEN` (HttpOnly) é salvo.
2. Nas requisições seguintes, manda o `token` no header indicado.
3. Sem o header (ou com valor diferente do cookie) a resposta é `403`.

Como o front e o back ficam em domínios diferentes, o JS do front não consegue ler o cookie do back de qualquer jeito. Por isso o cookie é HttpOnly e o token vem no corpo de `/auth/csrf`.

Erros seguem um formato único (`timestamp`, `status`, `error`, `message`, `path`).

## Testes

`./mvnw verify` roda tudo. Os testes de integração ficam em `src/test/java/.../integracao` e sobem a aplicação inteira (segurança, JWT em cookie, CSRF, JPA em H2): autenticação, CSRF e o fluxo de consultas por perfil.

## Limitações conhecidas

- A troca de senha **não exige a senha atual**: quem estiver com a sessão aberta consegue trocá-la. Uma alternativa mais segura é confirmar a troca por e-mail.
- O limite de tentativas de login fica **na memória** de uma instância (zera ao reiniciar). Com várias instâncias, troque por Redis ou por uma tabela no banco.
- A foto de perfil fica no banco; para muitos usuários, o ideal é um armazenamento de arquivos (S3 ou similar).
