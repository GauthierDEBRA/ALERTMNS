START TRANSACTION;

SET @good_structure_id := (
    SELECT id_structure
    FROM t_structure
    WHERE nom = 'Direction Générale'
    ORDER BY id_structure
    LIMIT 1
);

SET @legacy_structure_id := (
    SELECT id_structure
    FROM t_structure
    WHERE nom = 'Direction GÃ©nÃ©rale'
    ORDER BY id_structure
    LIMIT 1
);

UPDATE t_structure
SET nom = 'Direction Générale'
WHERE @legacy_structure_id IS NOT NULL
  AND @good_structure_id IS NULL
  AND id_structure = @legacy_structure_id;

SET @good_structure_id := (
    SELECT id_structure
    FROM t_structure
    WHERE nom = 'Direction Générale'
    ORDER BY id_structure
    LIMIT 1
);

SET @legacy_structure_id := (
    SELECT id_structure
    FROM t_structure
    WHERE nom = 'Direction GÃ©nÃ©rale'
    ORDER BY id_structure
    LIMIT 1
);

UPDATE t_utilisateur
SET id_structure = @good_structure_id
WHERE @good_structure_id IS NOT NULL
  AND @legacy_structure_id IS NOT NULL
  AND id_structure = @legacy_structure_id;

DELETE FROM t_structure
WHERE @good_structure_id IS NOT NULL
  AND @legacy_structure_id IS NOT NULL
  AND id_structure = @legacy_structure_id;

COMMIT;
