# D2 — Modèle de données (cohérent avec `backend/src/main/resources/db/migration/V1__init.sql`)

```mermaid
erDiagram
    PROMOTION ||--o{ ETUDIANT : "regroupe"
    PROMOTION ||--o{ SESSION_COURS : "contient"
    SESSION_COURS ||--o{ PRESENCE : "recense"
    ETUDIANT ||--o{ PRESENCE : "marque"
    SESSION_COURS ||--o{ EXERCICE : "recoit"
    ETUDIANT ||--o{ EXERCICE : "depose"
    EXERCICE ||--o| RELECTURE : "fait l objet de"
    ETUDIANT ||--o| RELECTURE : "effectue au plus une"
    SESSION_COURS ||--o{ TENTATIVE_CODE : "trace"

    PROMOTION {
      bigint id PK
      varchar nom
    }
    ETUDIANT {
      bigint id PK
      varchar nom
      bigint promotion_id FK
    }
    SESSION_COURS {
      bigint id PK
      varchar titre
      bigint promotion_id FK
      varchar code
      timestamp ouverture_at
      timestamp expiration_at
      boolean cloturee
    }
    PRESENCE {
      bigint id PK
      bigint session_id FK
      bigint etudiant_id FK
      varchar source "ETUDIANT ou FORMATEUR (RG10)"
    }
    EXERCICE {
      bigint id PK
      bigint session_id FK
      bigint etudiant_id FK
      varchar lien
      varchar statut "EN_ATTENTE ou RELU (RG7)"
    }
    RELECTURE {
      bigint id PK
      bigint exercice_id FK "unique (RG3)"
      bigint relecteur_id FK "different du depositaire (RG2)"
      int note "entier 0-20 (RG5)"
      varchar commentaire
      timestamp rendue_at
      timestamp maj_at "correction RG6"
    }
    TENTATIVE_CODE {
      bigint id PK
      bigint session_id FK
      bigint etudiant_id FK
      timestamp echoue_at "RG12 : 5 echecs = blocage 2 min"
    }
```

Contraintes uniques portées par les migrations : `PRESENCE (session_id, etudiant_id)` (RG13) · `EXERCICE (session_id, etudiant_id)` (409 EXERCICE_DEJA_DEPOSE) · `RELECTURE.exercice_id` unique (RG3) · `SESSION_COURS.code` unique.
