# Scripts do banco (SQL Server)

Rode no SSMS, conectado ao banco `LearnMaster`, **nesta ordem**, cada um em uma
janela de consulta (F5). Todos podem ser rodados de novo sem problema: o que ja
existe e pulado.

| Script | O que faz |
|---|---|
| `000_criar_tabelas.sql` | **Só para banco novo (vazio):** recria as tabelas originais. Em banco existente não faz nada |
| `001_verificacoes.sql` | **Só leitura.** Mostra o esquema atual e procura dados que impediriam as alterações |
| `002_limites_texto.sql` | NVARCHAR(50) nos nomes, NVARCHAR(200) em frente/verso, com CHECK; senha com espaço para BCrypt |
| `003_usuario_preferencias.sql` | Preferências, tutorial, último deck e `criado_em` em Usuario; e-mail único |
| `004_criado_em.sql` | `criado_em` em MainDeck, Deck e Flashcard |
| `005_avaliacao.sql` | Tabela Avaliacao (histórico Difícil/Bom/Fácil) |
| `006_tokens.sql` | Tabelas TokenRecuperacao e RefreshToken |
| `007_fks_indices.sql` | FKs em cascata na hierarquia e índices nas FKs |
| `008_remover_colunas_redundantes.sql` | Remove `Flashcard.nome` e `usuario_id` de Deck e Flashcard (o dono vem pela hierarquia) |

## Antes de começar
1. **Faça um backup do banco.**
2. Rode o `001`. Na aba *Messages*, todas as linhas devem dizer `OK`. Se aparecer
   `PROBLEMA`, a aba *Results* mostra quais registros corrigir.

## Se um script falhar
Cada script (do 002 ao 008) roda dentro de uma transação: se der erro, **nada é
alterado**. Leia a mensagem, corrija o dado indicado e rode o mesmo script de novo.

Se o erro citar uma estatística `_WA_Sys_...` ao alterar uma coluna, apague-a com
`DROP STATISTICS dbo.<Tabela>.<nome_da_estatistica>;` e rode o script de novo.

## Depois dos scripts
O backend (a partir da Fase 1) já espera as colunas novas. **Ele só funciona depois
que os scripts 002 a 008 forem aplicados.**

## Regras que o backend respeita
- **Avaliacao não tem cascata** (limite do SQL Server com múltiplos caminhos):
  o backend apaga as avaliações dos cards antes de apagar cards, decks ou LearnDecks,
  na mesma transação (`HierarquiaService`).
- **Usuario.ultimo_deck_id não tem cascata**: o backend o zera antes de apagar o deck
  para o qual ele aponta, também na mesma transação.
- Datas são gravadas em UTC (`SYSUTCDATETIME()`).
