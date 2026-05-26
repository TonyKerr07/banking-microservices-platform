-- ================================================================
-- Inicialização dos bancos de dados por serviço
-- Cada microserviço tem seu próprio database
-- ================================================================

CREATE DATABASE accounts_db;
CREATE DATABASE transfers_db;
CREATE DATABASE boletos_db;
CREATE DATABASE pix_db;

-- Permissões para o usuário da aplicação
GRANT ALL PRIVILEGES ON DATABASE accounts_db  TO banking_user;
GRANT ALL PRIVILEGES ON DATABASE transfers_db TO banking_user;
GRANT ALL PRIVILEGES ON DATABASE boletos_db   TO banking_user;
GRANT ALL PRIVILEGES ON DATABASE pix_db       TO banking_user;