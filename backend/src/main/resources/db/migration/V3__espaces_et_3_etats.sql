-- V3 : alignement sur le cahier des charges v2 (7.6/7.8 : 3 etats d'exercice)
-- + horodatages attendus par les espaces formateur/etudiant + prenom/nom separe

ALTER TABLE etudiant ADD COLUMN prenom VARCHAR(80);
UPDATE etudiant SET prenom = CASE WHEN position(' ' IN nom) > 0 THEN split_part(nom, ' ', 1) ELSE nom END,
                     nom = CASE WHEN position(' ' IN nom) > 0 THEN substring(nom FROM position(' ' IN nom) + 1) ELSE nom END;
ALTER TABLE etudiant ALTER COLUMN prenom SET NOT NULL;

ALTER TABLE presence ADD COLUMN enregistree_at timestamptz;
UPDATE presence SET enregistree_at = now();
ALTER TABLE presence ALTER COLUMN enregistree_at SET NOT NULL;

ALTER TABLE exercice ALTER COLUMN statut TYPE VARCHAR(24);
ALTER TABLE exercice DROP CONSTRAINT IF EXISTS exercice_statut_check;
UPDATE exercice SET statut = CASE
    WHEN statut = 'RELU' THEN 'RELU'
    WHEN EXISTS (SELECT 1 FROM relecture r WHERE r.exercice_id = exercice.id) THEN 'RELECTEUR_ATTRIBUE'
    ELSE 'EN_ATTENTE_RELECTEUR' END;
ALTER TABLE exercice ADD CONSTRAINT exercice_statut_check
    CHECK (statut IN ('EN_ATTENTE_RELECTEUR', 'RELECTEUR_ATTRIBUE', 'RELU'));

ALTER TABLE exercice ADD COLUMN depose_at timestamptz;
UPDATE exercice SET depose_at = now();
ALTER TABLE exercice ALTER COLUMN depose_at SET NOT NULL;

ALTER TABLE relecture ADD COLUMN attribuee_at timestamptz;
UPDATE relecture SET attribuee_at = now();
ALTER TABLE relecture ALTER COLUMN attribuee_at SET NOT NULL;

ALTER TABLE session_cours ADD COLUMN cloture_at timestamptz;
