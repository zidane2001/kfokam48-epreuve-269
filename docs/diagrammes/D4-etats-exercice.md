# D4 — États-transitions du cycle de vie d'un exercice (bonus)

```mermaid
stateDiagram-v2
    [*] --> EN_ATTENTE: POST /api/exercices → 201 { id, statut } + assignation de DEUX relecteurs parmi les présents (issue #25)
    EN_ATTENTE --> EN_ATTENTE: PUT /api/exercices/{id} → 200 (RG9 : lien remplacé, personne n'a relu)
    EN_ATTENTE --> EN_ATTENTE: PUT /api/relectures/{id} refusé 409 si déjà rendue
    EN_ATTENTE --> RELU: toutes les relectures rendues → RELU (issue #25, RG22) ; note provisoire si une seule rendue (RG21)
    EN_ATTENTE --> EN_ATTENTE: aucun relecteur disponible → reste visible EN_ATTENTE dans le tableau (RG7)
    RELU --> RELU: PUT /api/relectures/{id} → 200 correction de note (RG6, session non clôturée)
    RELU --> RELU: correction refusée 409 SESSION_CLOTUREE après clôture (RG11)
    RELU --> [*]: clôture de session → note définitive (Q15 tranché §7)
```
