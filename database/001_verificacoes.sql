/*
  001_verificacoes.sql  —  SOMENTE LEITURA, nao altera nada.

  Rode antes dos demais scripts. Mostra a estrutura atual e procura dados
  que impediriam as proximas alteracoes. Na aba "Messages", cada verificacao
  diz OK ou PROBLEMA; os resultados com linhas mostram os registros a corrigir.
*/
SET NOCOUNT ON;

PRINT '== Estrutura atual ==';
SELECT TABLE_NAME, COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS
WHERE TABLE_SCHEMA = 'dbo'
ORDER BY TABLE_NAME, ORDINAL_POSITION;

SELECT fk.name AS fk, OBJECT_NAME(fk.parent_object_id) AS tabela,
       COL_NAME(fkc.parent_object_id, fkc.parent_column_id) AS coluna,
       OBJECT_NAME(fk.referenced_object_id) AS referencia,
       fk.delete_referential_action_desc AS ao_excluir
FROM sys.foreign_keys fk
JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
ORDER BY tabela;

SELECT OBJECT_NAME(i.object_id) AS tabela, i.name AS indice, i.is_unique, i.is_primary_key
FROM sys.indexes i
WHERE OBJECTPROPERTY(i.object_id, 'IsUserTable') = 1 AND i.name IS NOT NULL
ORDER BY tabela;

PRINT '';
PRINT '== Verificacoes ==';

-- Toda tabela precisa de chave primaria em id (as FKs apontam para ela)
IF EXISTS (SELECT 1 FROM (VALUES ('Usuario'), ('MainDeck'), ('Deck'), ('Flashcard')) t(nome)
           WHERE OBJECTPROPERTY(OBJECT_ID('dbo.' + t.nome), 'TableHasPrimaryKey') = 0)
    PRINT 'PROBLEMA: alguma tabela principal esta sem chave primaria.'
ELSE PRINT 'OK: chaves primarias';

-- Textos acima dos novos limites (o "+ 'x'" conta espacos no fim)
IF EXISTS (SELECT 1 FROM dbo.MainDeck WHERE LEN(CAST(nome AS NVARCHAR(MAX)) + N'x') - 1 > 50)
   OR EXISTS (SELECT 1 FROM dbo.Deck WHERE LEN(CAST(nome AS NVARCHAR(MAX)) + N'x') - 1 > 50)
   OR EXISTS (SELECT 1 FROM dbo.Flashcard WHERE LEN(CAST(frente AS NVARCHAR(MAX)) + N'x') - 1 > 200
                                             OR LEN(CAST(verso AS NVARCHAR(MAX)) + N'x') - 1 > 200)
BEGIN
    PRINT 'PROBLEMA: textos acima do limite (LearnDeck/Deck 50, frente/verso 200):';
    SELECT 'MainDeck' AS tabela, id, nome AS texto FROM dbo.MainDeck WHERE LEN(CAST(nome AS NVARCHAR(MAX)) + N'x') - 1 > 50
    UNION ALL SELECT 'Deck', id, nome FROM dbo.Deck WHERE LEN(CAST(nome AS NVARCHAR(MAX)) + N'x') - 1 > 50
    UNION ALL SELECT 'Flashcard.frente', id, frente FROM dbo.Flashcard WHERE LEN(CAST(frente AS NVARCHAR(MAX)) + N'x') - 1 > 200
    UNION ALL SELECT 'Flashcard.verso', id, verso FROM dbo.Flashcard WHERE LEN(CAST(verso AS NVARCHAR(MAX)) + N'x') - 1 > 200;
END
ELSE PRINT 'OK: tamanhos de texto';

-- Textos vazios (o novo CHECK exige pelo menos 1 caractere)
IF EXISTS (SELECT 1 FROM dbo.MainDeck WHERE LEN(CAST(nome AS NVARCHAR(MAX))) = 0)
   OR EXISTS (SELECT 1 FROM dbo.Deck WHERE LEN(CAST(nome AS NVARCHAR(MAX))) = 0)
   OR EXISTS (SELECT 1 FROM dbo.Flashcard WHERE LEN(CAST(frente AS NVARCHAR(MAX))) = 0 OR LEN(CAST(verso AS NVARCHAR(MAX))) = 0)
    PRINT 'PROBLEMA: existem nomes, frentes ou versos vazios.'
ELSE PRINT 'OK: textos vazios';

-- E-mails repetidos (impedem o UNIQUE)
IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE email IS NOT NULL
           GROUP BY LOWER(LTRIM(RTRIM(CAST(email AS NVARCHAR(4000))))) HAVING COUNT(*) > 1)
BEGIN
    PRINT 'PROBLEMA: e-mails repetidos:';
    SELECT LOWER(LTRIM(RTRIM(CAST(email AS NVARCHAR(4000))))) AS email, COUNT(*) AS vezes
    FROM dbo.Usuario WHERE email IS NOT NULL
    GROUP BY LOWER(LTRIM(RTRIM(CAST(email AS NVARCHAR(4000))))) HAVING COUNT(*) > 1;
END
ELSE PRINT 'OK: e-mails unicos';

-- Registros orfaos (impedem as FKs)
IF EXISTS (SELECT 1 FROM dbo.MainDeck m WHERE m.usuario_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.Usuario u WHERE u.id = m.usuario_id))
   OR EXISTS (SELECT 1 FROM dbo.Deck d WHERE d.main_deck_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.MainDeck m WHERE m.id = d.main_deck_id))
   OR EXISTS (SELECT 1 FROM dbo.Flashcard f WHERE f.deck_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.Deck d WHERE d.id = f.deck_id))
BEGIN
    PRINT 'PROBLEMA: registros orfaos (apontam para um pai que nao existe):';
    SELECT 'MainDeck sem Usuario' AS caso, m.id FROM dbo.MainDeck m WHERE m.usuario_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.Usuario u WHERE u.id = m.usuario_id)
    UNION ALL SELECT 'Deck sem MainDeck', d.id FROM dbo.Deck d WHERE d.main_deck_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.MainDeck m WHERE m.id = d.main_deck_id)
    UNION ALL SELECT 'Flashcard sem Deck', f.id FROM dbo.Flashcard f WHERE f.deck_id IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.Deck d WHERE d.id = f.deck_id);
END
ELSE PRINT 'OK: sem orfaos';

-- Senhas ainda em texto puro (serao migradas para BCrypt na Fase 2)
SELECT COUNT(*) AS senhas_sem_hash FROM dbo.Usuario WHERE senha IS NOT NULL AND senha NOT LIKE '$2%';
