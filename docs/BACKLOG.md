# Backlog — miroir local des issues GitHub

Priorités : **Must** (étape 2, avant jalon v0.1) · **Should** (étape 4, avant v1.0) · **Could** (si le temps le permet).
Chaque issue renvoie aux EFx/RGx du cahier des charges. Une branche par ticket, une PR par branche, fermée par commit (`closes #n`).

| # | Titre (résultat attendu) | Priorité | Renvois | Critères d'acceptation |
|---|---|---|---|---|
| 1 | Le projet démarre chez un tiers : squelette backend + frontend + docker compose | Must | EF1, RNF4 | `./mvnw spring-boot:run` et `npm run dev` tournent depuis un clone vierge ; Flyway V1 commitée ; données de démo chargées |
| 2 | L'étudiant marque sa présence avec le code | Must | EF2, RG1, RG12, RG13 | 201 nominal ; 400 CODE_INCONNU ; 409 DEJA_PRESENT ; 410 CODE_EXPIRE ; 429 après 5 échecs ; test d'intégration vert |
| 3 | Le formateur ouvre une session et obtient un code expirant à H+15 | Must | EF1, RG1 | 201 avec expirationAt = ouvertureAt + 15 min ; 400 si champ manquant |
| 4 | L'étudiant dépose le lien de son exercice et un relecteur est assigné | Must | EF3, EF4, RG2, RG3, RG4 | 201 { id, statut } ; 400 LIEN_INVALIDE ; 409 EXERCICE_DEJA_DEPOSE ; relecteur = présent ≠ déposant ; sans autre présent → reste EN_ATTENTE |
| 5 | Le relecteur rend sa note et son commentaire | Must | EF5, RG5, RG2 | 200 ; 400 note hors 0–20 ou non entière ; 403 AUTO_RELECTURE ; 409 RELECTURE_DEJA_RENDUE ; test unitaire sur RG5 |
| 6 | Le formateur consulte le tableau par promotion | Must | EF7, RG7, Q16 | 200 avec presences, exercicesDeposes, moyenne, relecturesEnAttente ; 404 promotion inconnue ; moyenne calculée côté API (F3) |
| 7 | L'écran étudiant permet présence + dépôt d'exercice | Must | EF2, EF4, F2 | saisie code, affichage des erreurs { code, message }, dépôt du lien ; couche API dédiée ; états chargement/erreur |
| 8 | L'écran relecteur liste les relectures à faire et permet de noter | Must | EF5, F2 | liste depuis GET /api/relectures-a-faire ; note 0–20 + commentaire ; erreurs affichées |
| 9 | L'écran formateur ouvre une session et affiche le tableau | Must | EF1, EF7, F2 | ouverture → code affiché ; tableau rafraîchi ; aucune moyenne recalculée côté front |
| 10 | Le formateur clôture une session | Should | EF11, RG11 | 200 ; 409 déjà clôturée ; après clôture : correction de note refusée, dépôt refusé |
| 11 | Le relecteur corrige sa note avant clôture | Should | EF6, RG6 | PUT → 200 ; 409 SESSION_CLOTUREE après clôture ; l'affichage étudiant suit la dernière version |
| 12 | Le formateur ajoute une présence à la main (source FORMATEUR) | Should | EF8, RG10 | 201 source=FORMATEUR ; visible « ajouté par le formateur » dans le tableau |
| 13 | L'étudiant voit ses notes reçues sans le nom du relecteur | Should | EF9, Q8 | GET relectures-recues : note + commentaire, relecteurId jamais exposé |
| 14 | L'étudiant remplace son lien tant que personne n'a relu | Could | EF10, RG9 | PUT → 200 ; 409 RELECTURE_COMMENCEE |
| 15 | Blocage 2 min après 5 codes erronés | Should | EF12, RG12 | 429 TROP_DE_TENTATIVES pendant 2 min ; compteur réinitialisé à la réussite |
| 16 | [EVO] Authentification par comptes (issue #27) | Évolution (post-soumission) | CDC 7.14 | BCrypt + JWT ; rôles FORMATEUR/ETUDIANT ; identité portée par le token ; écran /connexion ; AUTH_REQUIS=false retablit le mode contrat ; 5 tests dédiés |
| 17 | [EVO] Vue riche GET /api/suivi pour le design (issue #28) | Évolution (post-soumission) | EF16, F3 | enveloppe { lignes, totaux } ; moyennePromotion calculée serveur ; moyenne null si aucune note (7.9) ; contrat /api/tableau intact ; 4 tests |
| 18 | [EVO] Gestion des promotions et étudiants (issues #29 + #30) | Évolution (post-soumission) | EF19, EF20, CDC 7.15 | POST /api/promotions, POST /api/etudiants (formateur) ; compte créé automatiquement (login prenom.nom) ; écran /gestion ; DELETE étudiant sans participation et promotion vide (409 sinon, closes #30) ; 403 si étudiant ; 7 tests (création + suppression) |

*Ordre d'exécution Must : 1 → 3 → 2 → 4 → 5 → 6 → 7 → 8 → 9. Tickets 16-18 : évolutions découpées
après la soumission à la demande du PO, une issue GitHub chacune (#27, #28, #29).*
