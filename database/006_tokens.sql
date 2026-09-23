/*
  006_tokens.sql

  - dbo.TokenRecuperacao: redefinicao de senha. Guarda so o hash SHA-256 do
    token (64 caracteres hex), expira em 30 min e vale uma vez (usado_em).
    criado_em serve para limitar pedidos repetidos por e-mail.
  - dbo.RefreshToken: sessao "continuar logado". Guarda so o hash; o logout
    preenche revogado_em.

  Excluir o usuario apaga os tokens dele (cascata, caminho unico).
  Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.TokenRecuperacao', 'U') IS NULL
        CREATE TABLE dbo.TokenRecuperacao (
            id         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_TokenRecuperacao PRIMARY KEY,
            usuario_id INT NOT NULL CONSTRAINT FK_TokenRecuperacao_usuario REFERENCES dbo.Usuario (id) ON DELETE CASCADE,
            token_hash CHAR(64) NOT NULL CONSTRAINT UX_TokenRecuperacao_hash UNIQUE,
            expira_em  DATETIME2(0) NOT NULL,
            usado_em   DATETIME2(0) NULL,
            criado_em  DATETIME2(0) NOT NULL CONSTRAINT DF_TokenRecuperacao_criado_em DEFAULT SYSUTCDATETIME()
        );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.TokenRecuperacao') AND name = 'IX_TokenRecuperacao_usuario')
        EXEC ('CREATE INDEX IX_TokenRecuperacao_usuario ON dbo.TokenRecuperacao (usuario_id, criado_em)');

    IF OBJECT_ID('dbo.RefreshToken', 'U') IS NULL
        CREATE TABLE dbo.RefreshToken (
            id          INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_RefreshToken PRIMARY KEY,
            usuario_id  INT NOT NULL CONSTRAINT FK_RefreshToken_usuario REFERENCES dbo.Usuario (id) ON DELETE CASCADE,
            token_hash  CHAR(64) NOT NULL CONSTRAINT UX_RefreshToken_hash UNIQUE,
            persistente BIT NOT NULL CONSTRAINT DF_RefreshToken_persistente DEFAULT 0,
            expira_em   DATETIME2(0) NOT NULL,
            revogado_em DATETIME2(0) NULL,
            criado_em   DATETIME2(0) NOT NULL CONSTRAINT DF_RefreshToken_criado_em DEFAULT SYSUTCDATETIME()
        );

    IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE object_id = OBJECT_ID('dbo.RefreshToken') AND name = 'IX_RefreshToken_usuario')
        EXEC ('CREATE INDEX IX_RefreshToken_usuario ON dbo.RefreshToken (usuario_id)');

    COMMIT;
    PRINT '006 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
