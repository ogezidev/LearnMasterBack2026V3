/*
  005_avaliacao.sql

  Cria dbo.Avaliacao com o historico completo de avaliacoes. A avaliacao atual
  de um card e a mais recente (maior avaliado_em) daquele usuario.

  As duas FKs ficam SEM cascata: o SQL Server nao aceita dois caminhos de
  exclusao em cascata ate a mesma tabela (Usuario -> Avaliacao direto e
  Usuario -> MainDeck -> Deck -> Flashcard -> Avaliacao). Por isso o backend
  apaga as avaliacoes antes dos flashcards, na mesma transacao (Fase 3).

  Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.Avaliacao', 'U') IS NULL
        CREATE TABLE dbo.Avaliacao (
            id           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Avaliacao PRIMARY KEY,
            usuario_id   INT NOT NULL CONSTRAINT FK_Avaliacao_usuario REFERENCES dbo.Usuario (id),
            flashcard_id INT NOT NULL CONSTRAINT FK_Avaliacao_flashcard REFERENCES dbo.Flashcard (id),
            nivel        VARCHAR(10) NOT NULL CONSTRAINT CK_Avaliacao_nivel CHECK (nivel IN ('dificil', 'bom', 'facil')),
            avaliado_em  DATETIME2(3) NOT NULL CONSTRAINT DF_Avaliacao_avaliado_em DEFAULT SYSUTCDATETIME()
        );

    -- Busca a avaliacao mais recente de cada card do usuario (filtro de Ver todos)
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.Avaliacao') AND name = 'IX_Avaliacao_usuario_flashcard')
        EXEC ('CREATE INDEX IX_Avaliacao_usuario_flashcard ON dbo.Avaliacao (usuario_id, flashcard_id, avaliado_em DESC) INCLUDE (nivel)');

    -- Exclusao das avaliacoes de um card antes de apagar o card
    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.Avaliacao') AND name = 'IX_Avaliacao_flashcard_id')
        EXEC ('CREATE INDEX IX_Avaliacao_flashcard_id ON dbo.Avaliacao (flashcard_id)');

    COMMIT;
    PRINT '005 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
