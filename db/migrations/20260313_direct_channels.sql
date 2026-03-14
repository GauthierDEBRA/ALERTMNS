SET @type_canal_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_canal'
      AND COLUMN_NAME = 'type_canal'
);
SET @type_canal_sql := IF(
    @type_canal_exists = 0,
    'ALTER TABLE t_canal ADD COLUMN type_canal VARCHAR(30) NOT NULL DEFAULT ''canal'' AFTER est_prive',
    'SELECT 1'
);
PREPARE type_canal_stmt FROM @type_canal_sql;
EXECUTE type_canal_stmt;
DEALLOCATE PREPARE type_canal_stmt;

UPDATE t_canal
SET type_canal = 'canal'
WHERE type_canal IS NULL OR type_canal = '';
