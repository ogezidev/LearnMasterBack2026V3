/*
  002_limites_texto.sql

  - MainDeck.nome e Deck.nome   -> NVARCHAR(50)  + CHECK de 1 a 50 caracteres
  - Flashcard.frente e .verso   -> NVARCHAR(200) + CHECK de 1 a 200 caracteres
  - Flashcard.nome (copia da frente, sera removida na Fase 3) -> pelo menos NVARCHAR(200)
  - Usuario.senha               -> pelo menos NVARCHAR(100), para caber o hash BCrypt

  Seguro para rodar mais de uma vez. Nao trunca nada: se algum texto passar do
  limite, o script para com uma mensagem e desfaz tudo (rode o 001 para ver quais).
  Mantem a coluna como NULL ou NOT NULL, igual ao que ja esta no banco.
*/
SET NOCOUNT ON;
GO

IF OBJECT_ID('tempdb..#ajustar_texto') IS NOT NULL DROP PROCEDURE #ajustar_texto;
GO
-- @so_aumentar = 1: nunca diminui uma coluna que ja e grande o bastante
CREATE PROCEDURE #ajustar_texto @tabela SYSNAME, @coluna SYSNAME, @tamanho INT, @so_aumentar BIT
AS
BEGIN
    DECLARE @tipo SYSNAME, @atual INT, @nulo VARCHAR(3), @maior INT, @sql NVARCHAR(MAX), @msg NVARCHAR(400);

    SELECT @tipo = DATA_TYPE, @atual = CHARACTER_MAXIMUM_LENGTH, @nulo = IS_NULLABLE
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = @tabela AND COLUMN_NAME = @coluna;

    IF @tipo IS NULL
    BEGIN
        SET @msg = CONCAT('Coluna dbo.', @tabela, '.', @coluna, ' nao encontrada.');
        THROW 50000, @msg, 1;
    END

    IF @tipo = 'nvarchar' AND @atual = @tamanho RETURN;
    IF @so_aumentar = 1 AND @tipo IN ('nvarchar', 'varchar') AND (@atual = -1 OR @atual >= @tamanho) RETURN;

    -- O "+ 'x'" faz o LEN contar espacos no fim
    SET @sql = CONCAT(N'SELECT @maior = MAX(LEN(CAST(', QUOTENAME(@coluna), N' AS NVARCHAR(MAX)) + N''x'') - 1) FROM dbo.', QUOTENAME(@tabela));
    EXEC sp_executesql @sql, N'@maior INT OUTPUT', @maior = @maior OUTPUT;

    IF @maior > @tamanho
    BEGIN
        SET @msg = CONCAT('dbo.', @tabela, '.', @coluna, ' tem texto com ', @maior, ' caracteres (limite ', @tamanho, '). Corrija antes de continuar.');
        THROW 50001, @msg, 1;
    END

    SET @sql = CONCAT(N'ALTER TABLE dbo.', QUOTENAME(@tabela), N' ALTER COLUMN ', QUOTENAME(@coluna),
                      N' NVARCHAR(', @tamanho, N') ', IIF(@nulo = 'YES', N'NULL', N'NOT NULL'));
    EXEC (@sql);
    PRINT CONCAT('Alterado: dbo.', @tabela, '.', @coluna, ' de ', @tipo, '(', @atual, ') para NVARCHAR(', @tamanho, ')');
END
GO

SET XACT_ABORT ON;
BEGIN TRY
    BEGIN TRANSACTION;

    EXEC #ajustar_texto 'MainDeck',  'nome',   50,  0;
    EXEC #ajustar_texto 'Deck',      'nome',   50,  0;
    EXEC #ajustar_texto 'Flashcard', 'frente', 200, 0;
    EXEC #ajustar_texto 'Flashcard', 'verso',  200, 0;
    EXEC #ajustar_texto 'Flashcard', 'nome',   200, 1;
    EXEC #ajustar_texto 'Usuario',   'senha',  100, 1;

    IF OBJECT_ID('dbo.CK_MainDeck_nome', 'C') IS NULL
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.MainDeck WHERE LEN(CAST(nome AS NVARCHAR(MAX))) = 0)
            THROW 50002, 'Existe LearnDeck (MainDeck) com nome vazio. Corrija antes de continuar.', 1;
        EXEC (N'ALTER TABLE dbo.MainDeck WITH CHECK ADD CONSTRAINT CK_MainDeck_nome CHECK (LEN(nome) BETWEEN 1 AND 50)');
    END

    IF OBJECT_ID('dbo.CK_Deck_nome', 'C') IS NULL
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.Deck WHERE LEN(CAST(nome AS NVARCHAR(MAX))) = 0)
            THROW 50003, 'Existe Deck com nome vazio. Corrija antes de continuar.', 1;
        EXEC (N'ALTER TABLE dbo.Deck WITH CHECK ADD CONSTRAINT CK_Deck_nome CHECK (LEN(nome) BETWEEN 1 AND 50)');
    END

    IF OBJECT_ID('dbo.CK_Flashcard_frente', 'C') IS NULL
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.Flashcard WHERE LEN(CAST(frente AS NVARCHAR(MAX))) = 0)
            THROW 50004, 'Existe Flashcard com frente vazia. Corrija antes de continuar.', 1;
        EXEC (N'ALTER TABLE dbo.Flashcard WITH CHECK ADD CONSTRAINT CK_Flashcard_frente CHECK (LEN(frente) BETWEEN 1 AND 200)');
    END

    IF OBJECT_ID('dbo.CK_Flashcard_verso', 'C') IS NULL
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.Flashcard WHERE LEN(CAST(verso AS NVARCHAR(MAX))) = 0)
            THROW 50005, 'Existe Flashcard com verso vazio. Corrija antes de continuar.', 1;
        EXEC (N'ALTER TABLE dbo.Flashcard WITH CHECK ADD CONSTRAINT CK_Flashcard_verso CHECK (LEN(verso) BETWEEN 1 AND 200)');
    END

    COMMIT;
    PRINT '002 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO

DROP PROCEDURE #ajustar_texto;
GO
