-- V5 : issue #25 - changement de besoin du client (enveloppe etape 3)
-- "chaque exercice est relu par deux pairs differents, la note retenue est la
--  moyenne des deux. Si un seul a rendu, sa note s'affiche mais provisoire."
-- Casse Q6/RG8 (un seul relecteur). Migration AJOUTEE, V1-V4 jamais modifiees.
-- La base existante (donnees de demo) doit survivre : aucune perte de donnees.

-- 1) Un exercice peut avoir PLUSIEURS relectures : suppression de l'unicite RG8
ALTER TABLE relecture DROP CONSTRAINT IF EXISTS relecture_exercice_id_key;

-- 2) Un etudiant ne relit qu'une fois le meme exercice (nouvelle contrainte RG8 v2)
ALTER TABLE relecture ADD CONSTRAINT uq_relecture_exercice_relecteur
    UNIQUE (exercice_id, relecteur_id);

-- 3) Un exercice a exactement 2 relecteurs : on garde la trace du nombre attendu
ALTER TABLE exercice ADD COLUMN nb_relecteurs_attendus INT NOT NULL DEFAULT 1;
UPDATE exercice SET nb_relecteurs_attendus = 2;
ALTER TABLE exercice ALTER COLUMN nb_relecteurs_attendus SET DEFAULT 2;
