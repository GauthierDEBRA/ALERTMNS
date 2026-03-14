CREATE TABLE t_audit_log (
    id_audit_log BIGINT NOT NULL AUTO_INCREMENT,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(120) NOT NULL,
    target_id BIGINT NULL,
    details TEXT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_actor BIGINT NULL,
    PRIMARY KEY (id_audit_log),
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (id_actor) REFERENCES t_utilisateur(id_user)
        ON UPDATE CASCADE ON DELETE SET NULL
);
