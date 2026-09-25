# D2 — Modèle de données (V1__init.sql + évolutions V2→V6)

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION_COURS : "contient"
    SESSION_COURS ||--o{ PRESENCE : "recense"
    ETUDIANT ||--o{ PRESENCE : "marque"
    SESSION_COURS ||--o{ EXERCICE : "recoit"
    ETUDIANT ||--o{ EXERCICE : "depose"
    EXERCICE ||--o{ RELECTURE : "fait l objet de (2, issue 25)"
    ETUDIANT ||--o{ RELECTURE : "effectue"
    SESSION_COURS ||--o{ TENTATIVE_CODE : "trace"
    ETUDIANT ||--o| COMPTE : "possède (7.14)"

    PROMOTION {
      bigint id PK
      varchar nom
    }
    ETUDIANT {
      bigint id PK
      varchar prenom "ajouté V3"
      varchar nom
      bigint promotion_id FK
    }
    COMPTE {
      bigint id PK
      varchar login UK "evolution PO 7.14"
      varchar mot_de_passe "BCrypt, jamais en clair"
      varchar role "ETUDIANT ou FORMATEUR"
      bigint etudiant_id FK "NULL pour le formateur"
    }
    SESSION_COURS {
      bigint id PK
      varchar titre
      bigint promotion_id FK
      varchar code
      timestamptz ouverture_at "timestamptz depuis V2"
      timestamptz expiration_at
      boolean cloturee
      timestamptz cloture_at "ajouté V3"
    }
    PRESENCE {
      bigint id PK
      bigint session_id FK
      bigint etudiant_id FK
      varchar source "ETUDIANT ou FORMATEUR (RG13)"
      timestamptz enregistree_at "ajouté V3"
    }
    EXERCICE {
      bigint id PK
      bigint session_id FK
      bigint etudiant_id FK
      varchar lien
      varchar statut "3 états depuis V3 : EN_ATTENTE_RELECTEUR, RELECTEUR_ATTRIBUE, RELU"
      timestamptz depose_at "ajouté V3"
      int nb_relecteurs_attendus "ajouté V5 : 2 (issue 25)"
    }
    RELECTURE {
      bigint id PK
      bigint exercice_id FK "V5 : plus d unicité exercice seul, unicité (exercice_id, relecteur_id)"
      bigint relecteur_id FK "différent du dépositaire (RG10)"
      int note "entier 0-20 (RG12)"
      varchar commentaire "obligatoire à la rendue (7.11)"
      varchar statut "ajouté V4 : EN_ATTENTE ou RENDUE"
      timestamptz attribuee_at "ajouté V3"
      timestamptz rendue_at
      timestamptz maj_at "correction RG16"
    }
    TENTATIVE_CODE {
      bigint id PK
      bigint session_id FK
      bigint etudiant_id FK
      timestamptz echoue_at "RG3 : 5 échecs / 2 min = blocage"
    }
```

Contraintes uniques portées par les migrations : `PRESENCE (session_id, etudiant_id)` (RG13) ·
`EXERCICE (session_id, etudiant_id)` (409 EXERCICE_DEJA_DEPOSE) · `RELECTURE (exercice_id, relecteur_id)`
depuis V5 (RG8 v2 : un étudiant ne relit qu'une fois le même exercice ; deux relecteurs par exercice)
· `SESSION_COURS.code` unique · `COMPTE.login` unique (7.14).
