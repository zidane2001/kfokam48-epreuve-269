-- V1__init.sql : schema initial — conforme au diagramme D2 (docs/diagrammes/D2-modele-donnees.md)
-- Regles couvertes : RG3 (relecture unique par exercice), RG13 (presence unique), code de session unique

CREATE TABLE promotion (
    id  BIGSERIAL PRIMARY KEY,
    nom VARCHAR(120) NOT NULL
);

CREATE TABLE etudiant (
    id           BIGSERIAL PRIMARY KEY,
    nom          VARCHAR(120) NOT NULL,
    promotion_id BIGINT NOT NULL REFERENCES promotion(id)
);

CREATE TABLE session_cours (
    id            BIGSERIAL PRIMARY KEY,
    titre         VARCHAR(200) NOT NULL,
    promotion_id  BIGINT NOT NULL REFERENCES promotion(id),
    code          VARCHAR(8) NOT NULL UNIQUE,
    ouverture_at  TIMESTAMP NOT NULL,
    expiration_at TIMESTAMP NOT NULL,          -- RG1 : ouverture + 15 min
    cloturee      BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE presence (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session_cours(id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant(id),
    source      VARCHAR(10) NOT NULL CHECK (source IN ('ETUDIANT','FORMATEUR')),  -- RG10
    CONSTRAINT uq_presence UNIQUE (session_id, etudiant_id)                       -- RG13
);

CREATE TABLE exercice (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session_cours(id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant(id),
    lien        VARCHAR(500) NOT NULL,
    statut      VARCHAR(12) NOT NULL DEFAULT 'EN_ATTENTE' CHECK (statut IN ('EN_ATTENTE','RELU')),
    CONSTRAINT uq_exercice UNIQUE (session_id, etudiant_id)                       -- 409 EXERCICE_DEJA_DEPOSE
);

CREATE TABLE relecture (
    id           BIGSERIAL PRIMARY KEY,
    exercice_id  BIGINT NOT NULL UNIQUE REFERENCES exercice(id),                  -- RG3 : un seul relecteur
    relecteur_id BIGINT NOT NULL REFERENCES etudiant(id),
    note         INT,
    commentaire  VARCHAR(2000),
    rendue_at    TIMESTAMP,
    maj_at       TIMESTAMP,                                                       -- RG6 : correction de note
    CONSTRAINT chk_note CHECK (note IS NULL OR (note BETWEEN 0 AND 20 AND note = FLOOR(note)))  -- RG5
);

CREATE TABLE tentative_code (
    id          BIGSERIAL PRIMARY KEY,
    session_id  BIGINT NOT NULL REFERENCES session_cours(id),
    etudiant_id BIGINT NOT NULL REFERENCES etudiant(id),
    echoue_at   TIMESTAMP NOT NULL                                                -- RG12 : 5 echecs / 2 min
);
