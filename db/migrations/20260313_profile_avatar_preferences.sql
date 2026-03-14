SET @avatar_column_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_utilisateur'
      AND COLUMN_NAME = 'avatar_url'
);
SET @avatar_sql := IF(
    @avatar_column_exists = 0,
    'ALTER TABLE t_utilisateur ADD COLUMN avatar_url VARCHAR(255) NULL AFTER is_active',
    'SELECT 1'
);
PREPARE avatar_stmt FROM @avatar_sql;
EXECUTE avatar_stmt;
DEALLOCATE PREPARE avatar_stmt;

SET @notif_reunion_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_utilisateur'
      AND COLUMN_NAME = 'notif_reunion'
);
SET @notif_reunion_sql := IF(
    @notif_reunion_exists = 0,
    'ALTER TABLE t_utilisateur ADD COLUMN notif_reunion TINYINT(1) NOT NULL DEFAULT 1 AFTER avatar_url',
    'SELECT 1'
);
PREPARE notif_reunion_stmt FROM @notif_reunion_sql;
EXECUTE notif_reunion_stmt;
DEALLOCATE PREPARE notif_reunion_stmt;

SET @notif_message_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_utilisateur'
      AND COLUMN_NAME = 'notif_message'
);
SET @notif_message_sql := IF(
    @notif_message_exists = 0,
    'ALTER TABLE t_utilisateur ADD COLUMN notif_message TINYINT(1) NOT NULL DEFAULT 1 AFTER notif_reunion',
    'SELECT 1'
);
PREPARE notif_message_stmt FROM @notif_message_sql;
EXECUTE notif_message_stmt;
DEALLOCATE PREPARE notif_message_stmt;

SET @notif_absence_exists := (
    SELECT COUNT(*)
    FROM INFORMATION_SCHEMA.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 't_utilisateur'
      AND COLUMN_NAME = 'notif_absence'
);
SET @notif_absence_sql := IF(
    @notif_absence_exists = 0,
    'ALTER TABLE t_utilisateur ADD COLUMN notif_absence TINYINT(1) NOT NULL DEFAULT 1 AFTER notif_message',
    'SELECT 1'
);
PREPARE notif_absence_stmt FROM @notif_absence_sql;
EXECUTE notif_absence_stmt;
DEALLOCATE PREPARE notif_absence_stmt;

UPDATE t_utilisateur
SET notif_reunion = COALESCE(notif_reunion, 1),
    notif_message = COALESCE(notif_message, 1),
    notif_absence = COALESCE(notif_absence, 1);
