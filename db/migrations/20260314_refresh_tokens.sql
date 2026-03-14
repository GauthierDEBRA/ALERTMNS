USE alertmns;

CREATE TABLE IF NOT EXISTS t_refresh_token (
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
) ENGINE=InnoDB;

CREATE INDEX idx_refresh_token_user ON t_refresh_token(id_user, expires_at);
