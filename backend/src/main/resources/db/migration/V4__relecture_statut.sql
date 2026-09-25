-- V4 : la colonne statut de relecture manque (ajoutee a l'entite au ticket B,
-- oubliee dans V3) — la validation Hibernate ddl-auto=validate l'a attrapee au demarrage.
ALTER TABLE relecture ADD COLUMN IF NOT EXISTS statut VARCHAR(12);
UPDATE relecture SET statut = CASE WHEN rendue_at IS NULL THEN 'EN_ATTENTE' ELSE 'RENDUE' END WHERE statut IS NULL;
ALTER TABLE relecture ALTER COLUMN statut SET NOT NULL;
