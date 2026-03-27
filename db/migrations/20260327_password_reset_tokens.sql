USE alertmns;

CREATE TABLE IF NOT EXISTS t_password_reset_token (
  id_prt     BIGINT      NOT NULL AUTO_INCREMENT,
  token      VARCHAR(64) NOT NULL,
  expires_at DATETIME    NOT NULL,
  used_at    DATETIME    NULL,
  id_user    BIGINT      NOT NULL,
  PRIMARY KEY (id_prt),
  UNIQUE KEY uk_prt_token (token),
  CONSTRAINT fk_prt_user
    FOREIGN KEY (id_user) REFERENCES t_utilisateur(id_user)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

CREATE INDEX idx_prt_user ON t_password_reset_token(id_user);
