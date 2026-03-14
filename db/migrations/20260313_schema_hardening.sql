ALTER TABLE t_structure
    MODIFY nom VARCHAR(150) NOT NULL,
    ADD CONSTRAINT uk_structure_nom UNIQUE (nom);

ALTER TABLE t_canal
    MODIFY nom VARCHAR(150) NOT NULL,
    ADD CONSTRAINT uk_canal_nom UNIQUE (nom);

ALTER TABLE t_reunion
    MODIFY titre VARCHAR(200) NOT NULL;

ALTER TABLE t_notification
    MODIFY type VARCHAR(100) NOT NULL,
    MODIFY contenu TEXT NOT NULL;

ALTER TABLE t_message
    DROP INDEX idx_message_canal,
    DROP INDEX idx_message_date,
    ADD INDEX idx_message_canal_date (id_canal, date_envoi),
    ADD INDEX idx_message_canal_message (id_canal, id_message);

ALTER TABLE t_pointage
    DROP INDEX idx_pointage_user,
    ADD INDEX idx_pointage_user_date (id_user, date_debut),
    ADD INDEX idx_pointage_user_fin (id_user, date_fin);

ALTER TABLE t_notification
    DROP INDEX idx_notif_user_lu,
    ADD INDEX idx_notif_user_notif (id_user, id_notif),
    ADD INDEX idx_notif_user_lu_notif (id_user, is_lu, id_notif);

ALTER TABLE t_membre_canal
    ADD INDEX idx_mc_canal_user (id_canal, id_user);

ALTER TABLE t_reunion
    ADD INDEX idx_reunion_createur (id_createur);

ALTER TABLE t_participant_reunion
    ADD INDEX idx_pr_user_reunion (id_user, id_reunion);
