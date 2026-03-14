USE alertmns;

SET @add_target_type = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 't_notification'
        AND column_name = 'target_type'
    ),
    'SELECT 1',
    'ALTER TABLE t_notification ADD COLUMN target_type VARCHAR(100) NULL AFTER date_creation'
  )
);
PREPARE stmt FROM @add_target_type;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_target_id = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 't_notification'
        AND column_name = 'target_id'
    ),
    'SELECT 1',
    'ALTER TABLE t_notification ADD COLUMN target_id BIGINT NULL AFTER target_type'
  )
);
PREPARE stmt FROM @add_target_id;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @add_target_route = (
  SELECT IF(
    EXISTS(
      SELECT 1
      FROM information_schema.columns
      WHERE table_schema = DATABASE()
        AND table_name = 't_notification'
        AND column_name = 'target_route'
    ),
    'SELECT 1',
    'ALTER TABLE t_notification ADD COLUMN target_route VARCHAR(255) NULL AFTER target_id'
  )
);
PREPARE stmt FROM @add_target_route;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
