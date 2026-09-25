/*
  009_codigo_recuperacao.sql

  A recuperacao de senha passa a usar um codigo de 6 digitos enviado por e-mail
  (em vez de um link):
  - dbo.TokenRecuperacao.tentativas: codigos errados digitados; no 5o erro o codigo
    e invalidado.
  - Remove a restricao UNIQUE de token_hash: o codigo e procurado pelo usuario
    (o ultimo pedido pendente), e o mesmo codigo pode se repetir com o tempo.

  Seguro para rodar mais de uma vez.
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF COL_LENGTH('dbo.TokenRecuperacao', 'tentativas') IS NULL
        ALTER TABLE dbo.TokenRecuperacao
            ADD tentativas INT NOT NULL CONSTRAINT DF_TokenRecuperacao_tentativas DEFAULT 0;

    IF OBJECT_ID('dbo.UX_TokenRecuperacao_hash', 'UQ') IS NOT NULL
        ALTER TABLE dbo.TokenRecuperacao DROP CONSTRAINT UX_TokenRecuperacao_hash;

    COMMIT;
    PRINT '009 concluido.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
