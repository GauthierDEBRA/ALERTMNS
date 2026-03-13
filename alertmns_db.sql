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
  nom           VARCHAR(100)   NOT NULL,
  type_structure VARCHAR(50)   NULL COMMENT 'Agence, Service, Equipe...',
  PRIMARY KEY (id_structure)
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
  nom        VARCHAR(100)  NOT NULL COMMENT 'ex: #general',
  est_prive  TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0 = public, 1 = privé',
  PRIMARY KEY (id_canal)
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
--  7. t_lecture_etat  (association suit_lecture)
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
--  8. t_pointage
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
--  9. t_reunion
-- ============================================================
CREATE TABLE t_reunion (
  id_reunion   BIGINT        NOT NULL AUTO_INCREMENT,
  titre        VARCHAR(150)  NOT NULL,
  description  TEXT          NULL,
  date_prevue  DATETIME      NOT NULL,
  id_createur  BIGINT        NOT NULL COMMENT 'Utilisateur organisateur',
  PRIMARY KEY (id_reunion),
  CONSTRAINT fk_reu_createur
    FOREIGN KEY (id_createur) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB COMMENT='Réunions planifiées';


-- ============================================================
--  10. t_participant_reunion  (association participe)
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
--  11. t_notification
-- ============================================================
CREATE TABLE t_notification (
  id_notif  BIGINT        NOT NULL AUTO_INCREMENT,
  type      VARCHAR(50)   NOT NULL COMMENT 'message, reunion, absence...',
  contenu   VARCHAR(255)  NOT NULL,
  is_lu     TINYINT(1)    NOT NULL DEFAULT 0 COMMENT '0 = non lu, 1 = lu',
  id_user   BIGINT        NOT NULL,
  PRIMARY KEY (id_notif),
  CONSTRAINT fk_notif_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB COMMENT='Notifications envoyées aux utilisateurs';


-- ============================================================
--  INDEX supplémentaires (performances)
-- ============================================================
CREATE INDEX idx_message_canal    ON t_message(id_canal);
CREATE INDEX idx_message_user     ON t_message(id_user);
CREATE INDEX idx_message_date     ON t_message(date_envoi);
CREATE INDEX idx_pointage_user    ON t_pointage(id_user);
CREATE INDEX idx_notif_user_lu    ON t_notification(id_user, is_lu);
CREATE INDEX idx_reunion_date     ON t_reunion(date_prevue);


-- ============================================================
--  STRUCTURES DE BASE
--  Les utilisateurs et canaux par défaut sont créés au démarrage
--  de Spring Boot via DataInitializer (mots de passe BCrypt réels)
-- ============================================================

INSERT INTO t_structure (nom, type_structure) VALUES
  ('Direction Générale',  'Direction'),
  ('Agence Paris',        'Agence'),
  ('Equipe Dev',          'Equipe');
