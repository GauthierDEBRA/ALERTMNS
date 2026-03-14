ALTER TABLE t_reunion
    ADD COLUMN duree_minutes INT NOT NULL DEFAULT 60 AFTER date_prevue,
    ADD COLUMN lieu VARCHAR(255) NULL AFTER duree_minutes,
    ADD COLUMN lien_visio VARCHAR(500) NULL AFTER lieu;

ALTER TABLE t_notification
    ADD COLUMN date_creation DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP AFTER is_lu;
