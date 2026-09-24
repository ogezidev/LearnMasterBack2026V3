# LearnMaster — Backend (tccv2)

API da plataforma de flashcards LearnMaster (TCC).

- **Stack:** Java 17, Spring Boot 4, Spring Data JPA, Spring Security (BCrypt + JWT), Spring Mail
- **Banco:** SQL Server
- **Frontend:** repositório `LearnMasterCursor` (React + Vite), em `http://localhost:5173`

## 1. Banco de dados

Rode no SSMS, em ordem, os scripts de `database/` (**faça um backup antes**).
O `database/LEIAME.md` explica cada um. Resumo:

| Script | O que faz |
|---|---|
| `001_verificacoes.sql` | Só leitura: mostra o esquema e procura dados que impediriam as alterações |
| `002` a `008` | Limites de texto, preferências do usuário, `criado_em`, tabelas `Avaliacao`, `TokenRecuperacao` e `RefreshToken`, FKs em cascata e remoção de colunas redundantes |

Os scripts podem ser rodados mais de uma vez e desfazem tudo se algo der errado.
**O backend só funciona depois dos scripts 002 a 008.**

## 2. Configuração (fora do Git)

Copie `local.properties.example` para `local.properties`, na raiz do projeto, e preencha:

| Variável | Para quê |
|---|---|
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexão com o SQL Server |
| `JWT_SECRET` | Chave do token de acesso (mínimo 32 caracteres aleatórios) |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USER`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP para a recuperação de senha. No Gmail: `smtp.gmail.com`, `587` e uma **senha de app**. Com `MAIL_HOST` vazio, o link aparece só no log (útil para testar) |
| `FRONTEND_URL` | Origem liberada no CORS e base do link de recuperação (padrão `http://localhost:5173`) |
| `COOKIE_SECURE`, `COOKIE_SAMESITE` | Opcionais. Padrão `true` e `Strict`. Com frontend e backend em domínios diferentes, use `SameSite=None` |

Em vez do arquivo, podem ser usadas variáveis de ambiente com os mesmos nomes.

## 3. Rodar

No IntelliJ, rode `Tccv2Application`. Pelo terminal, com Maven instalado: `mvn spring-boot:run`.
A API sobe em `http://localhost:8080`.

## API

Todas as rotas, menos `/auth/**`, exigem `Authorization: Bearer <token de acesso>`.
Erros sempre voltam como `{ "mensagem": "..." }`.

| Rota | Descrição |
|---|---|
| `POST /auth/cadastro`, `POST /auth/login` | Criam a sessão: token de acesso (15 min) + refresh token em cookie httpOnly (30 dias com "continuar logado"; senão, até fechar o navegador) |
| `POST /auth/refresh`, `POST /auth/logout` | Renovam (o refresh anterior é invalidado) e encerram a sessão no servidor |
| `POST /auth/recuperar`, `POST /auth/redefinir` | Recuperação de senha: resposta igual exista ou não o e-mail; token com hash no banco, 30 min, uso único, até 3 pedidos a cada 15 min |
| `GET / PATCH /usuarios/me` | Dados da conta (sem senha) e troca de nome |
| `PUT /usuarios/me/email`, `PUT /usuarios/me/senha` | Exigem a senha atual; trocar a senha encerra as outras sessões |
| `PATCH /usuarios/me/preferencias` | Modo noturno, fonte para dislexia e tutorial concluído |
| `PUT /usuarios/me/ultimo-deck` | Último deck estudado |
| `/maindecks`, `/decks`, `/flashcards` | CRUD da hierarquia. Só acessa dados do próprio usuário; excluir apaga os filhos e as avaliações numa transação |
| `POST /flashcards/lote` | Cria de 1 a 5 cards de uma vez (todos ou nenhum) |
| `POST /avaliacoes`, `GET /avaliacoes/atuais` | Avaliação Difícil/Bom/Fácil (histórico completo) e a mais recente de cada card |

## Segurança

- Senhas com BCrypt. Contas antigas com senha em texto puro são convertidas no próximo login.
- Limite de 5 tentativas de login erradas por e-mail a cada 15 min.
- Credenciais e segredos fora do código (`local.properties` está no `.gitignore`).
