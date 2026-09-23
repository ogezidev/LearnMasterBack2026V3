# Scripts do banco (SQL Server)

Rode no SSMS, conectado ao banco `LearnMaster`, **nesta ordem**, cada um em uma
janela de consulta (F5). Todos podem ser rodados de novo sem problema: o que ja
existe e pulado.

| Script | O que faz |
|---|---|
| `001_verificacoes.sql` | **Só leitura.** Mostra o esquema atual e procura dados que impediriam as alterações |
| `002_limites_texto.sql` | NVARCHAR(50) nos nomes, NVARCHAR(200) em frente/verso, com CHECK; senha com espaço para BCrypt |
| `003_usuario_preferencias.sql` | Preferências, tutorial, último deck e `criado_em` em Usuario; e-mail único |
| `004_criado_em.sql` | `criado_em` em MainDeck, Deck e Flashcard |
| `005_avaliacao.sql` | Tabela Avaliacao (histórico Difícil/Bom/Fácil) |
| `006_tokens.sql` | Tabelas TokenRecuperacao e RefreshToken |
| `007_fks_indices.sql` | FKs em cascata na hierarquia e índices nas FKs |

## Antes de começar
1. **Faça um backup do banco.**
2. Rode o `001`. Na aba *Messages*, todas as linhas devem dizer `OK`. Se aparecer
   `PROBLEMA`, a aba *Results* mostra quais registros corrigir.

## Se um script falhar
Cada script (do 002 ao 007) roda dentro de uma transação: se der erro, **nada é
alterado**. Leia a mensagem, corrija o dado indicado e rode o mesmo script de novo.

Se o erro citar uma estatística `_WA_Sys_...` ao alterar uma coluna, apague-a com
`DROP STATISTICS dbo.<Tabela>.<nome_da_estatistica>;` e rode o script de novo.

## Depois dos scripts
O backend (a partir da Fase 1) já espera as colunas novas. **Ele só funciona depois
que os scripts 002 a 007 forem aplicados.**

## Regras que o backend precisa respeitar
- **Avaliacao não tem cascata** (limite do SQL Server com múltiplos caminhos):
  apague as avaliações dos cards antes de apagar cards, decks, LearnDecks ou o usuário.
- **Usuario.ultimo_deck_id não tem cascata**: zere-o antes de apagar o deck
  para o qual ele aponta.
- Datas são gravadas em UTC (`SYSUTCDATETIME()`).
