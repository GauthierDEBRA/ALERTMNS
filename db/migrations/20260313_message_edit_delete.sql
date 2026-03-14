SET @msg_edit_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_message'
      AND COLUMN_NAME = 'date_modification'
);
SET @msg_edit_sql := IF(
    @msg_edit_exists = 0,
    'ALTER TABLE t_message ADD COLUMN date_modification DATETIME NULL AFTER date_envoi',
    'SELECT 1'
);
PREPARE msg_edit_stmt FROM @msg_edit_sql;
EXECUTE msg_edit_stmt;
DEALLOCATE PREPARE msg_edit_stmt;

SET @msg_deleted_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_message'
      AND COLUMN_NAME = 'is_deleted'
);
SET @msg_deleted_sql := IF(
    @msg_deleted_exists = 0,
    'ALTER TABLE t_message ADD COLUMN is_deleted TINYINT(1) NOT NULL DEFAULT 0 AFTER date_modification',
    'SELECT 1'
);
PREPARE msg_deleted_stmt FROM @msg_deleted_sql;
EXECUTE msg_deleted_stmt;
DEALLOCATE PREPARE msg_deleted_stmt;

UPDATE t_message
SET is_deleted = COALESCE(is_deleted, 0);
