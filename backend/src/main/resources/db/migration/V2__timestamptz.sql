-- V2 : les instants passent en timestamptz (le code Java manipule des OffsetDateTime,
-- serialises en ISO-8601 RFC3339 comme l'exige le contrat d'API)
ALTER TABLE session_cours  ALTER COLUMN ouverture_at  TYPE timestamptz;
ALTER TABLE session_cours  ALTER COLUMN expiration_at TYPE timestamptz;
ALTER TABLE relecture      ALTER COLUMN rendue_at     TYPE timestamptz;
ALTER TABLE relecture      ALTER COLUMN maj_at        TYPE timestamptz;
ALTER TABLE tentative_code ALTER COLUMN echoue_at     TYPE timestamptz;
