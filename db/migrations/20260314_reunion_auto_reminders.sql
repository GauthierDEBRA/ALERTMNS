ALTER TABLE t_reunion
    ADD COLUMN rappel_j1_envoye_at DATETIME NULL AFTER lien_visio,
    ADD COLUMN rappel_1h_envoye_at DATETIME NULL AFTER rappel_j1_envoye_at;
