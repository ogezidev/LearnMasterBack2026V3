/*
  008_remover_colunas_redundantes.sql

  Remove colunas que so duplicavam informacao:
  - Flashcard.nome        (copia da frente)
  - Deck.usuario_id       (o dono vem de Deck -> MainDeck -> Usuario)
  - Flashcard.usuario_id  (o dono vem de Flashcard -> Deck -> MainDeck -> Usuario)

  Antes de remover cada coluna, apaga o que depende dela (FKs, indices, CHECKs,
  DEFAULTs e estatisticas criadas a mao). Rode depois do 007.
  Seguro para rodar mais de uma vez: coluna que ja nao existe e pulada.

  Atencao: o backend da Fase 3 em diante ja nao usa essas colunas. Se Flashcard.nome
  for NOT NULL, criar cards falha ate este script ser aplicado.
*/
SET NOCOUNT ON;
GO

IF OBJECT_ID('tempdb..#remover_coluna') IS NOT NULL DROP PROCEDURE #remover_coluna;
GO
CREATE PROCEDURE #remover_coluna @tabela SYSNAME, @coluna SYSNAME
AS
BEGIN
    DECLARE @obj INT = OBJECT_ID(CONCAT('dbo.', @tabela));
    DECLARE @col INT = COLUMNPROPERTY(@obj, @coluna, 'ColumnId');
    DECLARE @sql NVARCHAR(MAX) = N'';

    IF @col IS NULL
    BEGIN
        PRINT CONCAT('Ja removida: dbo.', @tabela, '.', @coluna);
        RETURN;
    END

    -- FKs que usam a coluna
    SELECT @sql += CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' DROP CONSTRAINT ', QUOTENAME(fk.name), N';')
    FROM sys.foreign_keys fk
    JOIN sys.foreign_key_columns fkc ON fkc.constraint_object_id = fk.object_id
    WHERE fkc.parent_object_id = @obj AND fkc.parent_column_id = @col;

    -- Indices que usam a coluna (menos a chave primaria)
    SELECT @sql += CONCAT(N'DROP INDEX ', QUOTENAME(i.name), N' ON dbo.', QUOTENAME(@tabela), N';')
    FROM sys.indexes i
    WHERE i.object_id = @obj AND i.is_primary_key = 0 AND i.is_unique_constraint = 0
      AND EXISTS (SELECT 1 FROM sys.index_columns ic
                  WHERE ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.column_id = @col);

    -- CHECKs e DEFAULTs da coluna
    SELECT @sql += CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' DROP CONSTRAINT ', QUOTENAME(c.name), N';')
    FROM sys.check_constraints c
    WHERE c.parent_object_id = @obj AND c.parent_column_id = @col;

    SELECT @sql += CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' DROP CONSTRAINT ', QUOTENAME(d.name), N';')
    FROM sys.default_constraints d
    WHERE d.parent_object_id = @obj AND d.parent_column_id = @col;

    -- Estatisticas criadas a mao sobre a coluna
    SELECT @sql += CONCAT(N'DROP STATISTICS dbo.', QUOTENAME(@tabela), N'.', QUOTENAME(s.name), N';')
    FROM sys.stats s
    JOIN sys.stats_columns sc ON sc.object_id = s.object_id AND sc.stats_id = s.stats_id
    WHERE s.object_id = @obj AND sc.column_id = @col AND s.user_created = 1;

    SET @sql += CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' DROP COLUMN ', QUOTENAME(@coluna), N';');
    EXEC (@sql);
    PRINT CONCAT('Removida: dbo.', @tabela, '.', @coluna);
END
GO

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    EXEC #remover_coluna 'Flashcard', 'nome';
    EXEC #remover_coluna 'Flashcard', 'usuario_id';
    EXEC #remover_coluna 'Deck',      'usuario_id';

    COMMIT;
    PRINT '008 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO

DROP PROCEDURE #remover_coluna;
GO
