/*
  000_criar_tabelas.sql  —  SÓ PARA BANCO NOVO (VAZIO)

  Recria as quatro tabelas originais do LearnMaster no formato antigo
  (Usuario, MainDeck, Deck, Flashcard). Depois rode normalmente os scripts
  001 a 008, que levam essas tabelas ao formato final.

  Em um banco que já tem as tabelas, este script não faz nada (cada tabela
  só é criada se ainda não existir).
*/
SET NOCOUNT ON;
SET XACT_ABORT ON;
GO

BEGIN TRY
    BEGIN TRANSACTION;

    IF OBJECT_ID('dbo.Usuario', 'U') IS NULL
        CREATE TABLE dbo.Usuario (
            id    INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Usuario PRIMARY KEY,
            email NVARCHAR(255) NOT NULL,
            nome  NVARCHAR(100) NOT NULL,
            senha NVARCHAR(100) NOT NULL
        );

    IF OBJECT_ID('dbo.MainDeck', 'U') IS NULL
        CREATE TABLE dbo.MainDeck (
            id         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_MainDeck PRIMARY KEY,
            nome       NVARCHAR(255) NOT NULL,
            usuario_id INT NOT NULL
        );

    IF OBJECT_ID('dbo.Deck', 'U') IS NULL
        CREATE TABLE dbo.Deck (
            id           INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Deck PRIMARY KEY,
            nome         NVARCHAR(255) NOT NULL,
            main_deck_id INT NOT NULL,
            usuario_id   INT NULL
        );

    IF OBJECT_ID('dbo.Flashcard', 'U') IS NULL
        CREATE TABLE dbo.Flashcard (
            id         INT IDENTITY(1,1) NOT NULL CONSTRAINT PK_Flashcard PRIMARY KEY,
            nome       NVARCHAR(255) NULL,
            frente     NVARCHAR(255) NOT NULL,
            verso      NVARCHAR(255) NOT NULL,
            deck_id    INT NOT NULL,
            usuario_id INT NULL
        );

    COMMIT;
    PRINT '000 concluido. Agora rode 001 a 008.';
END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0 ROLLBACK;
    THROW;
END CATCH;
GO
