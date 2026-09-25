# Journal — Presence55 (kfokam48-epreuve-269)

## Étape 1 — Analyse et conception
**Fait :** cahier des charges (v1 puis v2 du PO : 18 EF, 20 RG, 20 ENF), 4 diagrammes Mermaid (D1–D3 + D4 bonus), contrat d'API complété (les 5 opérations imposées à la lettre + opérations ajoutées), backlog de 15 tickets en issues GitHub avec priorités et renvois EFx/RGx, commit `[JALON] analyse` posé avant tout code.
**Bloqué :** ~15 min sur la clé dupliquée `/api/relectures/{id}` dans le contrat (post écrasé par le put) — détecté en chargeant le YAML en Python, fusionné. Contradictions Q10/Q15 tranchées en faveur de Q10 (~10 min).
**IA :** a proposé le découpage en tickets, retenu après relecture titre par titre ; vérifications faites en machine (YAML chargé, pas lu à l'œil).

## Étape 2 — Première version (v0.1)
**Fait :** backend complet (squelette + sessions/présence + exercices/relecteur + relectures + tableau + espaces formateur/étudiant), frontend Next.js portant le design fourni par le PO, branché sur l'API réelle. Une branche + une PR par ticket (1, 3, 4, 5, 6, A, B, C, qa-fix, v4), issues fermées par les merges. 4 migrations Flyway (V1–V4). 29 tests verts dont 8 parcours utilisateurs complets ; 2 tests E2E réels backend+front contre PostgreSQL.
**Bloqué :** ~20 min sur le blocage RG3 (échecs rollbackés avec la requête — corrigé par transaction REQUIRES_NEW) ; ~15 min sur une colonne manquante attrapée par ddl-auto=validate (V4) ; token GitHub sans droit PR (PR créées à la main par le PO).
**IA :** a écrit code et tests ; chaque livrable vérifié par exécution réelle (compilation, tests H2, démarrage contre Neon, parcours curl, build Next.js). 4 vrais bugs trouvés et corrigés grâce aux tests (mauvais repository, blocage jamais persisté, DTO trompé d'id, statut HTTP erroné).

## Étape 3 — Enveloppe
*(à venir)*

## Étape 4 — Version finale (v1.0)
*(à venir)*

## Étape 5 — Épreuve Git
*(à venir)*
