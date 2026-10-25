-- Foto de perfil (uma por conta de autenticacao, valida para paciente, medico e atendente).
-- Fica em tabela propria para nao carregar os bytes toda vez que a conta for lida (login, JWT).
CREATE TABLE foto_perfil (
    usuario_auth_id BIGINT       NOT NULL PRIMARY KEY,
    conteudo        BYTEA        NOT NULL,
    tipo            VARCHAR(50)  NOT NULL,
    versao          BIGINT       NOT NULL,
    CONSTRAINT fk_foto_perfil_auth_users FOREIGN KEY (usuario_auth_id) REFERENCES auth_users (id) ON DELETE CASCADE
);
