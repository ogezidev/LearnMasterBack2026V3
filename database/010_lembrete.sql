/*
  010_lembrete.sql

  Lembretes de estudo do aplicativo mobile (sincronizados entre aparelhos).
  - data e horario sao a hora local do usuario (o celular agenda no fuso dele).
  - frequencia: UMA_VEZ (usa data), DIARIO, SEMANAL (usa dias_semana).
  - dias_semana: mascara de bits, segunda = 1, terca = 2, quarta = 4 ... domingo = 64.
  - deck_id e opcional e sem cascata (o SQL Server nao permite um segundo caminho de
    cascata ate Lembrete): ao excluir um deck, o backend limpa deck_id antes.
  - Excluir o usuario apaga os lembretes dele (cascata, caminho unico).

  Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.Lembrete', 'U') IS NULL
        CREATE TABLE dbo.Lembrete (
            id          INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Lembrete PRIMARY KEY,
            usuario_id  INT NOT NULL CONSTRAINT FK_Lembrete_usuario REFERENCES dbo.Usuario (id) ON DELETE CASCADE,
            deck_id     INT NULL CONSTRAINT FK_Lembrete_deck REFERENCES dbo.Deck (id),
            titulo      NVARCHAR(60) NOT NULL,
            frequencia  VARCHAR(10) NOT NULL,
            data        DATE NULL,
            horario     TIME(0) NOT NULL,
            dias_semana TINYINT NOT NULL CONSTRAINT DF_Lembrete_dias_semana DEFAULT 0,
            ativo       BIT NOT NULL CONSTRAINT DF_Lembrete_ativo DEFAULT 1,
            criado_em   DATETIME2(0) NOT NULL CONSTRAINT DF_Lembrete_criado_em DEFAULT SYSUTCDATETIME(),
            CONSTRAINT CK_Lembrete_titulo CHECK (LEN(titulo) BETWEEN 1 AND 60),
            CONSTRAINT CK_Lembrete_dias CHECK (dias_semana BETWEEN 0 AND 127),
            CONSTRAINT CK_Lembrete_frequencia CHECK (
                (frequencia = 'UMA_VEZ' AND data IS NOT NULL)
                OR frequencia = 'DIARIO'
                OR (frequencia = 'SEMANAL' AND dias_semana > 0))
        );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.Lembrete') AND name = 'IX_Lembrete_usuario')
        EXEC ('CREATE INDEX IX_Lembrete_usuario ON dbo.Lembrete (usuario_id)');

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.Lembrete') AND name = 'IX_Lembrete_deck')
        EXEC ('CREATE INDEX IX_Lembrete_deck ON dbo.Lembrete (deck_id)');

    COMMIT;
    PRINT '010 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
