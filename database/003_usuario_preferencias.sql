/*
  003_usuario_preferencias.sql

  Em dbo.Usuario:
  - modo_noturno, fonte_dislexia, tutorial_concluido: BIT NOT NULL DEFAULT 0
    (usuarios existentes ficam com 0 = desligado / tutorial nao visto)
  - ultimo_deck_id: INT NULL, FK para Deck SEM cascata. Por isso, antes de
    excluir um deck, o backend precisa zerar ultimo_deck_id de quem aponta para
    ele (feito na mesma transacao da exclusao, Fase 3).
  - criado_em: DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME() (UTC)
  - e-mail unico (ignora NULL). Para caber no indice, o e-mail vira NVARCHAR(255)
    se hoje for maior; para se houver e-mail repetido ou maior que 255.

  Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.Usuario', 'modo_noturno') IS NULL
        ALTER TABLE dbo.Usuario ADD modo_noturno BIT NOT NULL CONSTRAINT DF_Usuario_modo_noturno DEFAULT 0;

    IF COL_LENGTH('dbo.Usuario', 'fonte_dislexia') IS NULL
        ALTER TABLE dbo.Usuario ADD fonte_dislexia BIT NOT NULL CONSTRAINT DF_Usuario_fonte_dislexia DEFAULT 0;

    IF COL_LENGTH('dbo.Usuario', 'tutorial_concluido') IS NULL
        ALTER TABLE dbo.Usuario ADD tutorial_concluido BIT NOT NULL CONSTRAINT DF_Usuario_tutorial_concluido DEFAULT 0;

    IF COL_LENGTH('dbo.Usuario', 'ultimo_deck_id') IS NULL
        ALTER TABLE dbo.Usuario ADD ultimo_deck_id INT NULL;

    IF COL_LENGTH('dbo.Usuario', 'criado_em') IS NULL
        ALTER TABLE dbo.Usuario ADD criado_em DATETIME2(0) NOT NULL CONSTRAINT DF_Usuario_criado_em DEFAULT SYSUTCDATETIME();

    -- Comandos que citam colunas criadas acima rodam via EXEC (o SQL Server
    -- valida o lote inteiro antes de executar, quando elas ainda nao existem)
    IF OBJECT_ID('dbo.FK_Usuario_ultimo_deck', 'F') IS NULL
        EXEC ('ALTER TABLE dbo.Usuario ADD CONSTRAINT FK_Usuario_ultimo_deck FOREIGN KEY (ultimo_deck_id) REFERENCES dbo.Deck (id)');

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.Usuario') AND name = 'IX_Usuario_ultimo_deck_id')
        EXEC ('CREATE INDEX IX_Usuario_ultimo_deck_id ON dbo.Usuario (ultimo_deck_id)');

    -- E-mail unico: pula tudo se ja existir um indice/constraint UNIQUE so em email
    IF NOT EXISTS (
        SELECT 1
        FROM sys.indexes i
        JOIN sys.index_columns ic ON ic.object_id = i.object_id AND ic.index_id = i.index_id AND ic.key_ordinal = 1
        WHERE i.object_id = OBJECT_ID('dbo.Usuario') AND i.is_unique = 1
          AND COL_NAME(ic.object_id, ic.column_id) = 'email'
          AND (SELECT COUNT(*) FROM sys.index_columns x
               WHERE x.object_id = i.object_id AND x.index_id = i.index_id AND x.key_ordinal > 0) = 1)
    BEGIN
        IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE email IS NOT NULL
                   GROUP BY LOWER(LTRIM(RTRIM(CAST(email AS NVARCHAR(4000))))) HAVING COUNT(*) > 1)
            THROW 50010, 'Existem e-mails repetidos em Usuario (rode o 001 para ver quais). Corrija antes de continuar.', 1;

        DECLARE @tipo SYSNAME, @tam INT, @nulo VARCHAR(3);
        SELECT @tipo = DATA_TYPE, @tam = CHARACTER_MAXIMUM_LENGTH, @nulo = IS_NULLABLE
        FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'dbo' AND TABLE_NAME = 'Usuario' AND COLUMN_NAME = 'email';

        IF @tipo NOT IN ('nvarchar', 'varchar') OR @tam = -1 OR @tam > 255
        BEGIN
            IF EXISTS (SELECT 1 FROM dbo.Usuario WHERE LEN(CAST(email AS NVARCHAR(MAX)) + N'x') - 1 > 255)
                THROW 50011, 'Existe e-mail com mais de 255 caracteres. Corrija antes de continuar.', 1;
            DECLARE @sql NVARCHAR(200) = CONCAT(N'ALTER TABLE dbo.Usuario ALTER COLUMN email NVARCHAR(255) ',
                                                IIF(@nulo = 'YES', N'NULL', N'NOT NULL'));
            EXEC (@sql);
        END

        EXEC ('CREATE UNIQUE INDEX UX_Usuario_email ON dbo.Usuario (email) WHERE email IS NOT NULL');
    END

    COMMIT;
    PRINT '003 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
