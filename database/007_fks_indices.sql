/*
  007_fks_indices.sql

  Hierarquia com exclusao em cascata:
    Usuario -> MainDeck (usuario_id) -> Deck (main_deck_id) -> Flashcard (deck_id)
  Se a FK ja existir sem cascata, ela e recriada com ON DELETE CASCADE.
  Se houver registros orfaos, o script para (rode o 001 para ver quais).

  Deck.usuario_id e Flashcard.usuario_id sao colunas redundantes (o dono ja vem
  pela hierarquia). Se tiverem FK com cascata, essa FK e removida, porque criaria
  dois caminhos de cascata ate a mesma tabela, o que o SQL Server recusa.
  Nenhum dado e apagado; so a constraint.

  Tambem cria indices em todas as FKs. Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
GO

IF OBJECT_ID('tempdb..#garantir_fk_cascata') IS NOT NULL DROP PROCEDURE #garantir_fk_cascata;
GO
CREATE PROCEDURE #garantir_fk_cascata @tabela SYSNAME, @coluna SYSNAME, @referencia SYSNAME
AS
BEGIN
    DECLARE @fk SYSNAME, @acao NVARCHAR(60), @orfaos INT, @sql NVARCHAR(MAX), @msg NVARCHAR(400);
    DECLARE @nome SYSNAME = CONCAT('FK_', @tabela, '_', @coluna);

    SELECT @fk = fk.name, @acao = fk.delete_referential_action_desc
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
    WHERE fk.parent_object_id = OBJECT_ID(CONCAT('dbo.', @tabela))
      AND fk.referenced_object_id = OBJECT_ID(CONCAT('dbo.', @referencia))
      AND COL_NAME(fkc.parent_object_id, fkc.parent_column_id) = @coluna;

    IF @acao = 'CASCADE' RETURN;

    SET @sql = CONCAT(N'SELECT @orfaos = COUNT(*) FROM dbo.', QUOTENAME(@tabela), N' t WHERE t.', QUOTENAME(@coluna),
                      N' IS NOT NULL AND NOT EXISTS (SELECT 1 FROM dbo.', QUOTENAME(@referencia), N' r WHERE r.id = t.', QUOTENAME(@coluna), N')');
    EXEC sp_executesql @sql, N'@orfaos INT OUTPUT', @orfaos = @orfaos OUTPUT;
    IF @orfaos > 0
    BEGIN
        SET @msg = CONCAT(@orfaos, ' registro(s) em dbo.', @tabela, ' apontam para ', @referencia, ' inexistente. Corrija antes de continuar.');
        THROW 50020, @msg, 1;
    END

    IF @fk IS NOT NULL
    BEGIN
        SET @sql = CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' DROP CONSTRAINT ', QUOTENAME(@fk));
        EXEC (@sql);
        SET @nome = @fk;
    END

    SET @sql = CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' WITH CHECK ADD CONSTRAINT ', QUOTENAME(@nome),
                      N' FOREIGN KEY (', QUOTENAME(@coluna), N') REFERENCES dbo.', QUOTENAME(@referencia), N' (id) ON DELETE CASCADE');
    EXEC (@sql);
    PRINT CONCAT('FK com cascata: dbo.', @tabela, '.', @coluna, ' -> ', @referencia);
END
GO

IF OBJECT_ID('tempdb..#remover_fk_cascata') IS NOT NULL DROP PROCEDURE #remover_fk_cascata;
GO
CREATE PROCEDURE #remover_fk_cascata @tabela SYSNAME, @coluna SYSNAME
AS
BEGIN
    DECLARE @fk SYSNAME, @sql NVARCHAR(MAX);
    SELECT @fk = fk.name
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
    WHERE fk.parent_object_id = OBJECT_ID(CONCAT('dbo.', @tabela))
      AND COL_NAME(fkc.parent_object_id, fkc.parent_column_id) = @coluna
      AND fk.delete_referential_action_desc <> 'NO_ACTION';
    IF @fk IS NULL RETURN;

    SET @sql = CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' DROP CONSTRAINT ', QUOTENAME(@fk));
    EXEC (@sql);
    PRINT CONCAT('FK com cascata removida (coluna redundante): dbo.', @tabela, '.', @coluna);
END
GO

IF OBJECT_ID('tempdb..#garantir_indice') IS NOT NULL DROP PROCEDURE #garantir_indice;
GO
-- Cria IX_<tabela>_<coluna> se nenhum indice comecar por essa coluna
CREATE PROCEDURE #garantir_indice @tabela SYSNAME, @coluna SYSNAME
AS
BEGIN
    IF EXISTS (SELECT 1
               FROM sys.index_columns ic
               WHERE ic.object_id = OBJECT_ID(CONCAT('dbo.', @tabela)) AND ic.key_ordinal = 1
                 AND COL_NAME(ic.object_id, ic.column_id) = @coluna)
        RETURN;

    DECLARE @sql NVARCHAR(MAX) = CONCAT(N'CREATE INDEX ', QUOTENAME(CONCAT('IX_', @tabela, '_', @coluna)),
                                        N' ON dbo.', QUOTENAME(@tabela), N' (', QUOTENAME(@coluna), N')');
    EXEC (@sql);
    PRINT CONCAT('Indice criado: dbo.', @tabela, '.', @coluna);
END
GO

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    EXEC #remover_fk_cascata 'Deck', 'usuario_id';
    EXEC #remover_fk_cascata 'Flashcard', 'usuario_id';

    EXEC #garantir_fk_cascata 'MainDeck',  'usuario_id',   'Usuario';
    EXEC #garantir_fk_cascata 'Deck',      'main_deck_id', 'MainDeck';
    EXEC #garantir_fk_cascata 'Flashcard', 'deck_id',      'Deck';

    EXEC #garantir_indice 'MainDeck',  'usuario_id';
    EXEC #garantir_indice 'Deck',      'main_deck_id';
    EXEC #garantir_indice 'Flashcard', 'deck_id';

    COMMIT;
    PRINT '007 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO

DROP PROCEDURE #garantir_fk_cascata;
DROP PROCEDURE #remover_fk_cascata;
DROP PROCEDURE #garantir_indice;
GO
