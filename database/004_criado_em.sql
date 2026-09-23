/*
  004_criado_em.sql

  Adiciona criado_em DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME() (UTC) em
  MainDeck, Deck e Flashcard. Registros existentes recebem a data de agora.
  Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.MainDeck', 'criado_em') IS NULL
        ALTER TABLE dbo.MainDeck ADD criado_em DATETIME2(0) NOT NULL CONSTRAINT DF_MainDeck_criado_em DEFAULT SYSUTCDATETIME();

    IF COL_LENGTH('dbo.Deck', 'criado_em') IS NULL
        ALTER TABLE dbo.Deck ADD criado_em DATETIME2(0) NOT NULL CONSTRAINT DF_Deck_criado_em DEFAULT SYSUTCDATETIME();

    IF COL_LENGTH('dbo.Flashcard', 'criado_em') IS NULL
        ALTER TABLE dbo.Flashcard ADD criado_em DATETIME2(0) NOT NULL CONSTRAINT DF_Flashcard_criado_em DEFAULT SYSUTCDATETIME();

    COMMIT;
    PRINT '004 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
