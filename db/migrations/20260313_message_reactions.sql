SET @reaction_table_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.TABLES
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_reaction_message'
);
SET @reaction_table_sql := IF(
    @reaction_table_exists = 0,
    'CREATE TABLE t_reaction_message (
        id_reaction BIGINT NOT NULL AUTO_INCREMENT,
        emoji VARCHAR(16) NOT NULL,
        date_reaction DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
        id_message BIGINT NOT NULL,
        id_user BIGINT NOT NULL,
        PRIMARY KEY (id_reaction),
        CONSTRAINT uq_reaction_message_user_emoji UNIQUE (id_message, id_user, emoji),
        CONSTRAINT fk_react_message FOREIGN KEY (id_message) REFERENCES t_message(id_message) ON UPDATE CASCADE ON DELETE CASCADE,
        CONSTRAINT fk_react_user FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user) ON UPDATE CASCADE ON DELETE CASCADE
    ) ENGINE=InnoDB',
    'SELECT 1'
);
PREPARE reaction_table_stmt FROM @reaction_table_sql;
EXECUTE reaction_table_stmt;
DEALLOCATE PREPARE reaction_table_stmt;

SET @reaction_idx_message_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_reaction_message'
      AND INDEX_NAME = 'idx_reaction_message_msg'
);
SET @reaction_idx_message_sql := IF(
    @reaction_idx_message_exists = 0,
    'CREATE INDEX idx_reaction_message_msg ON t_reaction_message(id_message)',
    'SELECT 1'
);
PREPARE reaction_idx_message_stmt FROM @reaction_idx_message_sql;
EXECUTE reaction_idx_message_stmt;
DEALLOCATE PREPARE reaction_idx_message_stmt;

SET @reaction_idx_user_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_reaction_message'
      AND INDEX_NAME = 'idx_reaction_message_user'
);
SET @reaction_idx_user_sql := IF(
    @reaction_idx_user_exists = 0,
    'CREATE INDEX idx_reaction_message_user ON t_reaction_message(id_user)',
    'SELECT 1'
);
PREPARE reaction_idx_user_stmt FROM @reaction_idx_user_sql;
EXECUTE reaction_idx_user_stmt;
DEALLOCATE PREPARE reaction_idx_user_stmt;
