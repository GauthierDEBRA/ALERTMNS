-- ============================================================
--  ALERTMNS — Script de création de la base de données
--  Projet 2024/2025 — Metz Numeric School
--  SGBD : MySQL 8+ / MariaDB 10.5+
-- ============================================================

CREATE DATABASE IF NOT EXISTS alertmns
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE alertmns;

-- ============================================================
--  1. t_structure
-- ============================================================
CREATE TABLE t_structure (
  id_structure  BIGINT         NOT NULL AUTO_INCREMENT,
  nom           VARCHAR(150)   NOT NULL,
  type_structure VARCHAR(50)   NULL COMMENT 'Agence, Service, Equipe...',
  PRIMARY KEY (id_structure),
  UNIQUE KEY uk_structure_nom (nom)
) ENGINE=InnoDB COMMENT='Structures (agences, services, équipes)';


-- ============================================================
--  2. t_utilisateur
-- ============================================================
CREATE TABLE t_utilisateur (
  id_user      BIGINT         NOT NULL AUTO_INCREMENT,
  nom          VARCHAR(100)   NOT NULL,
  prenom       VARCHAR(100)   NOT NULL,
  email        VARCHAR(150)   NOT NULL,
  mdp          VARCHAR(255)   NOT NULL COMMENT 'Mot de passe haché (bcrypt/Argon2)',
  role         VARCHAR(50)    NOT NULL COMMENT 'Admin, RH, Responsable, Collaborateur...',
  msg_absence  VARCHAR(255)   NULL     COMMENT 'Message affiché en cas d\'absence',
  is_active    TINYINT(1)     NOT NULL DEFAULT 1 COMMENT '1 = actif, 0 = désactivé',
  avatar_url   VARCHAR(255)   NULL     COMMENT 'Photo de profil hébergée dans /uploads',
  notif_reunion TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1 = notifications de réunion activées',
  notif_message TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1 = notifications de message activées',
  notif_absence TINYINT(1)    NOT NULL DEFAULT 1 COMMENT '1 = notifications d\'absence activées',
  id_structure BIGINT         NULL,
  PRIMARY KEY (id_user),
  UNIQUE KEY uk_email (email),
  CONSTRAINT fk_user_structure
    FOREIGN KEY (id_structure) REFERENCES t_structure(id_structure)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Comptes utilisateurs';


-- ============================================================
--  3. t_canal
-- ============================================================
CREATE TABLE t_canal (
  id_canal   BIGINT        NOT NULL AUTO_INCREMENT,
  nom        VARCHAR(150)  NOT NULL COMMENT 'ex: #general',
  est_prive  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0 = public, 1 = privé',
  type_canal VARCHAR(30)   NOT NULL DEFAULT 'canal' COMMENT 'canal, direct',
  PRIMARY KEY (id_canal),
  UNIQUE KEY uk_canal_nom (nom)
) ENGINE=InnoDB COMMENT='Canaux de discussion';


-- ============================================================
--  4. t_membre_canal  (association est_membre)
-- ============================================================
CREATE TABLE t_membre_canal (
  id_user   BIGINT       NOT NULL,
  id_canal  BIGINT       NOT NULL,
  role      VARCHAR(50)  NOT NULL DEFAULT 'membre' COMMENT 'admin, membre...',
  PRIMARY KEY (id_user, id_canal),
  CONSTRAINT fk_mc_user
    FOREIGN KEY (id_user)  REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_mc_canal
    FOREIGN KEY (id_canal) REFERENCES t_canal(id_canal)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Membres d\'un canal (association est_membre)';


-- ============================================================
--  5. t_message
-- ============================================================
CREATE TABLE t_message (
  id_message  BIGINT    NOT NULL AUTO_INCREMENT,
  contenu     TEXT      NOT NULL,
  date_envoi  DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  date_modification DATETIME NULL,
  is_deleted  TINYINT(1) NOT NULL DEFAULT 0,
  id_user     BIGINT    NOT NULL,
  id_canal    BIGINT    NOT NULL,
  PRIMARY KEY (id_message),
  CONSTRAINT fk_msg_user
    FOREIGN KEY (id_user)  REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE RESTRICT,
  CONSTRAINT fk_msg_canal
    FOREIGN KEY (id_canal) REFERENCES t_canal(id_canal)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Messages envoyés dans les canaux';


-- ============================================================
--  6. t_piece_jointe
-- ============================================================
CREATE TABLE t_piece_jointe (
  id_pj       BIGINT        NOT NULL AUTO_INCREMENT,
  nom_fichier VARCHAR(255)  NOT NULL COMMENT 'Nom d\'origine du fichier',
  url         VARCHAR(500)  NOT NULL COMMENT 'Chemin de stockage',
  id_message  BIGINT        NOT NULL,
  PRIMARY KEY (id_pj),
  CONSTRAINT fk_pj_message
    FOREIGN KEY (id_message) REFERENCES t_message(id_message)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Pièces jointes attachées aux messages';


-- ============================================================
--  7. t_reaction_message
-- ============================================================
CREATE TABLE t_reaction_message (
  id_reaction   BIGINT      NOT NULL AUTO_INCREMENT,
  emoji         VARCHAR(16) NOT NULL,
  date_reaction DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_message    BIGINT      NOT NULL,
  id_user       BIGINT      NOT NULL,
  PRIMARY KEY (id_reaction),
  CONSTRAINT uq_reaction_message_user_emoji UNIQUE (id_message, id_user, emoji),
  CONSTRAINT fk_react_message
    FOREIGN KEY (id_message) REFERENCES t_message(id_message)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_react_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Réactions emoji sur les messages';


-- ============================================================
--  8. t_lecture_etat  (association suit_lecture)
-- ============================================================
CREATE TABLE t_lecture_etat (
  id_user           BIGINT  NOT NULL,
  id_canal          BIGINT  NOT NULL,
  dernier_id_msg_lu BIGINT  NULL COMMENT 'NULL si aucun message encore lu',
  PRIMARY KEY (id_user, id_canal),
  CONSTRAINT fk_le_user
    FOREIGN KEY (id_user)  REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_le_canal
    FOREIGN KEY (id_canal) REFERENCES t_canal(id_canal)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Suivi de lecture par utilisateur et canal';


-- ============================================================
--  9. t_pointage
-- ============================================================
CREATE TABLE t_pointage (
  id_pointage  BIGINT    NOT NULL AUTO_INCREMENT,
  date_debut   DATETIME  NOT NULL COMMENT 'Heure d\'arrivée (précision minute)',
  date_fin     DATETIME  NULL     COMMENT 'NULL = pointage en cours',
  id_user      BIGINT    NOT NULL,
  PRIMARY KEY (id_pointage),
  CONSTRAINT fk_poi_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Pointages (temps de travail)';


-- ============================================================
--  10. t_reunion
-- ============================================================
CREATE TABLE t_reunion (
  id_reunion   BIGINT        NOT NULL AUTO_INCREMENT,
  titre        VARCHAR(200)  NOT NULL,
  description  TEXT          NULL,
  date_prevue  DATETIME      NOT NULL,
  duree_minutes INT          NOT NULL DEFAULT 60,
  lieu         VARCHAR(255)  NULL,
  lien_visio   VARCHAR(500)  NULL,
  rappel_j1_envoye_at DATETIME NULL,
  rappel_1h_envoye_at DATETIME NULL,
  rappel_30m_envoye_at DATETIME NULL,
  id_createur  BIGINT        NOT NULL COMMENT 'Utilisateur organisateur',
  PRIMARY KEY (id_reunion),
  CONSTRAINT fk_reu_createur
    FOREIGN KEY (id_createur) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Réunions planifiées';


-- ============================================================
--  11b. t_audit_log
-- ============================================================
CREATE TABLE t_audit_log (
  id_audit_log BIGINT       NOT NULL AUTO_INCREMENT,
  action       VARCHAR(120) NOT NULL,
  target_type  VARCHAR(120) NOT NULL,
  target_id    BIGINT       NULL,
  details      TEXT         NULL,
  created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  id_actor     BIGINT       NULL,
  PRIMARY KEY (id_audit_log),
  CONSTRAINT fk_audit_actor
    FOREIGN KEY (id_actor) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE SET NULL
) ENGINE=InnoDB COMMENT='Journal d audit des actions sensibles admin et RH';


-- ============================================================
--  11c. t_refresh_token
-- ============================================================
CREATE TABLE t_refresh_token (
  id_refresh_token BIGINT       NOT NULL AUTO_INCREMENT,
  token_hash       VARCHAR(64)  NOT NULL,
  expires_at       DATETIME     NOT NULL,
  created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  last_used_at     DATETIME     NULL,
  is_revoked       TINYINT(1)   NOT NULL DEFAULT 0,
  id_user          BIGINT       NOT NULL,
  PRIMARY KEY (id_refresh_token),
  UNIQUE KEY uk_refresh_token_hash (token_hash),
  CONSTRAINT fk_refresh_token_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Refresh tokens persistés pour les sessions longues';


-- ============================================================
--  12. t_participant_reunion  (association participe)
-- ============================================================
CREATE TABLE t_participant_reunion (
  id_reunion  BIGINT       NOT NULL,
  id_user     BIGINT       NOT NULL,
  statut      VARCHAR(50)  NOT NULL DEFAULT 'attente' COMMENT 'attente, accepte, refuse',
  PRIMARY KEY (id_reunion, id_user),
  CONSTRAINT fk_pr_reunion
    FOREIGN KEY (id_reunion) REFERENCES t_reunion(id_reunion)
    ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT fk_pr_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Participants à une réunion (association participe)';


-- ============================================================
--  12. t_notification
-- ============================================================
CREATE TABLE t_notification (
  id_notif  BIGINT        NOT NULL AUTO_INCREMENT,
  type      VARCHAR(100)  NOT NULL COMMENT 'message, reunion, absence...',
  contenu   TEXT          NOT NULL,
  is_lu     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0 = non lu, 1 = lu',
  date_creation DATETIME  NOT NULL DEFAULT CURRENT_TIMESTAMP,
  target_type VARCHAR(100) NULL,
  target_id BIGINT NULL,
  target_route VARCHAR(255) NULL,
  id_user   BIGINT        NOT NULL,
  PRIMARY KEY (id_notif),
  CONSTRAINT fk_notif_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Notifications envoyées aux utilisateurs';


-- ============================================================
--  INDEX supplémentaires (performances)
-- ============================================================
CREATE INDEX idx_mc_canal_user    ON t_membre_canal(id_canal, id_user);
CREATE INDEX idx_message_canal_date ON t_message(id_canal, date_envoi);
CREATE INDEX idx_message_canal_message ON t_message(id_canal, id_message);
CREATE INDEX idx_message_user     ON t_message(id_user);
CREATE INDEX idx_reaction_message_msg ON t_reaction_message(id_message);
CREATE INDEX idx_reaction_message_user ON t_reaction_message(id_user);
CREATE INDEX idx_pointage_user_date ON t_pointage(id_user, date_debut);
CREATE INDEX idx_pointage_user_fin ON t_pointage(id_user, date_fin);
CREATE INDEX idx_notif_user_notif ON t_notification(id_user, id_notif);
CREATE INDEX idx_notif_user_lu_notif ON t_notification(id_user, is_lu, id_notif);
CREATE INDEX idx_refresh_token_user ON t_refresh_token(id_user, expires_at);
CREATE INDEX idx_reunion_createur ON t_reunion(id_createur);
CREATE INDEX idx_reunion_date     ON t_reunion(date_prevue);
CREATE INDEX idx_pr_user_reunion  ON t_participant_reunion(id_user, id_reunion);


-- ============================================================
--  STRUCTURES DE BASE
--  Les utilisateurs et canaux par défaut sont créés au démarrage
--  de Spring Boot via DataInitializer (mots de passe BCrypt réels)
-- ============================================================

INSERT INTO t_structure (nom, type_structure) VALUES
  ('Direction Générale',  'Direction'),
  ('Agence Paris',        'Agence'),
  ('Equipe Dev',          'Equipe');
