-- V6 : evolution decidee par le PO (hors perimetre initial CDC 3.2)
-- "il est normal que sur une plateforme chacun ait ses identifiants pour la securite"
-- Les comptes (formateur de demo + un compte par etudiant) sont crees par DemoDataLoader :
-- les mots de passe doivent etre haches BCrypt cote Java, jamais generes en SQL.
CREATE TABLE compte (
    id              BIGSERIAL PRIMARY KEY,
    login           VARCHAR(120) NOT NULL UNIQUE,
    mot_de_passe    VARCHAR(200) NOT NULL,           -- BCrypt, jamais en clair
    role            VARCHAR(12)  NOT NULL CHECK (role IN ('ETUDIANT','FORMATEUR')),
    etudiant_id     BIGINT REFERENCES etudiant(id)   -- NULL pour le formateur
);
