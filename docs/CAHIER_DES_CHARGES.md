# Cahier des charges — Presence55

Auteur : KF48-...-269 (zidane2001) · Version 1 · Frontend choisi : **Next.js**, parce que le sujet l'autorise explicitement et qu'il fournit en un seul projet le serveur de développement, le routage par dossiers et le build `next build` documenté dans le README ; aucun rendu serveur de données n'est requis, Next.js est utilisé ici comme framework React.

## 1. Contexte et objectif
La direction de la formation KFOKAM48 gère les sessions de cours avec un tableur : appel manuel, collecte des liens d'exercices par messagerie, relectures par les pairs non traçables, tableau de suivi reconstruit à la main. L'application remplace ce fonctionnement par un flux en ligne : le formateur ouvre une session et reçoit un code de présence, les étudiants marquent leur présence avec ce code, déposent le lien de leur exercice, un pair assigné note l'exercice, et le formateur suit tout dans un tableau récapitulatif.

## 2. Acteurs et rôles
| Acteur | Ce qu'il peut faire |
|---|---|
| Formateur | Ouvrir une session (→ code de présence), clôturer une session, ajouter une présence à la main (marquée « ajouté par le formateur »), consulter le tableau : présences, exercices déposés, moyennes, relectures en attente |
| Étudiant | Choisir son nom dans une liste (pas de mot de passe, Q1), marquer sa présence avec le code, déposer/remplacer le lien de son exercice, voir sa note et son commentaire (sans le nom du relecteur, Q8), faire les relectures qui lui sont assignées |
| Relecteur (étudiant assigné) | Noter (0–20 entier) et commenter l'exercice d'un pair, corriger sa note tant que le formateur n'a pas clôturé (Q10) |
| Système | Assigner le relecteur au hasard parmi les présents (Q7), faire expirer le code à H+15 (Q2), bloquer 2 min après 5 codes erronés (Q4) |

## 3. Périmètre
**Inclus :** annuaire des promotions et étudiants ; sessions avec code de présence expirant ; présences étudiant et formateur (source ETUDIANT/FORMATEUR) ; dépôt et remplacement du lien d'exercice ; assignation aléatoire d'un relecteur parmi les présents ; relecture notée /20 entier avec commentaire ; correction de note avant clôture ; clôture de session par le formateur ; tableau récapitulatif par promotion ; blocage 2 min après 5 échecs de code ; données de démonstration au démarrage.
**Exclu :** authentification et mot de passe (Q1) ; notifications email/push ; import de listes d'étudiants par fichier ; application mobile native ; archivage multi-années ; tout calcul de moyenne côté frontend (F3).

## 4. Exigences fonctionnelles
| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur ouvre une session et obtient un code de présence | `POST /api/sessions` → `201 { id, code, ouvertureAt, expirationAt }` avec `expirationAt = ouvertureAt + 15 min` ; titre ou promotionId manquant → 400 | Must |
| EF2 | L'étudiant marque sa présence avec le code | Code valide → présence dans le tableau ; code inconnu → 400 `CODE_INCONNU` ; déjà présent → 409 `DEJA_PRESENT` ; code expiré → 410 `CODE_EXPIRE` | Must |
| EF3 | Le système assigne un relecteur au hasard parmi les présents | Au dépôt de l'exercice, un présent ≠ déposant est assigné (RG2, RG3) ; si aucun autre présent, l'exercice reste « EN_ATTENTE » et apparaît dans le tableau (Q11) | Must |
| EF4 | L'étudiant dépose le lien de son exercice pour une session | `POST /api/exercices` → `201 { id, statut }` ; lien invalide → 400 `LIEN_INVALIDE` ; second dépôt → 409 `EXERCICE_DEJA_DEPOSE` | Must |
| EF5 | Le relecteur note et commente l'exercice d'un pair | `POST /api/relectures/{id}` → `200` ; note hors 0–20 ou non entière → 400 ; exercice de l'étudiant lui-même → 403 `AUTO_RELECTURE` ; déjà rendue → 409 `RELECTURE_DEJA_RENDUE` | Must |
| EF6 | Le relecteur peut corriger sa note tant que la session n'est pas clôturée | `PUT /api/relectures/{id}` → `200` ; après clôture → 409 `SESSION_CLOTUREE` | Should |
| EF7 | Le formateur voit le tableau par promotion | `GET /api/tableau?promotionId=` → `200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]` ; promotion inconnue → 404 | Must |
| EF8 | Le formateur ajoute une présence à la main | La présence créée porte `source: FORMATEUR` et apparaît comme « ajouté par le formateur » (Q14) | Should |
| EF9 | L'étudiant relu voit sa note et son commentaire, sans le nom du relecteur | `GET /api/etudiants/{id}/relectures-recues` expose note + commentaire, jamais le relecteurId (Q8) | Should |
| EF10 | L'étudiant remplace le lien de son exercice tant que personne n'a commencé à le relire | `PUT /api/exercices/{id}` → `200` ; relecture commencée → 409 `RELECTURE_COMMENCEE` (Q13) | Could |
| EF11 | Le formateur clôture la session | `POST /api/sessions/{id}/cloture` → `200` ; après clôture : plus de correction de note (Q10/Q15), dépôt d'exercice refusé (Q12) | Should |
| EF12 | Blocage après 5 codes erronés | Au 5e code erroné d'un étudiant sur une session, toute nouvelle tentative → 429 `TROP_DE_TENTATIVES` pendant 2 minutes (Q4) | Should |

## 5. Exigences non fonctionnelles
| Réf | Exigence | Comment on la vérifie |
|---|---|---|
| RNF1 | Expiration du code exacte à ouverture + 15 min | Test d'intégration : présence acceptée à +14:59, refusée 410 à +15:01 |
| RNF2 | Temps de réponse < 500 ms sur les 5 opérations du contrat | Vérification manuelle locale ; volumétrie cible faible (≈ 50 étudiants/promotion), aucun cache requis |
| RNF3 | Écrans étudiant et relecteur utilisables sur mobile | Test manuel viewport 375 px : saisie du code et dépôt du lien sans scroll horizontal |
| RNF4 | Démarrage chez un tiers en 3 commandes max ou `docker compose up` | Test depuis un clone vierge : backend `./mvnw spring-boot:run`, frontend `npm install && npm run dev`, données de démo chargées au démarrage |
| RNF5 | Aucune stack trace renvoyée au client | Test d'intégration : toute erreur → `{ code, message }` uniquement (B4) |

## 6. Règles de gestion
| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Un étudiant ne peut pas relire son propre exercice | Q5 |
| RG3 | Un seul relecteur par exercice | Q6 |
| RG4 | Le relecteur est choisi par le système, au hasard, parmi les étudiants présents à la session | Q7 |
| RG5 | La note est un entier de 0 à 20 | Q9 |
| RG6 | Le relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session | Q10 |
| RG7 | L'exercice sans relecteur rendu reste « EN_ATTENTE » et visible dans le tableau du formateur | Q11 |
| RG8 | Le dépôt d'exercice reste possible après la fin du code, jusqu'à la clôture de la session | Q12 |
| RG9 | Le lien de l'exercice peut être remplacé tant que la relecture n'a pas commencé | Q13 |
| RG10 | La présence ajoutée à la main par le formateur est marquée `source: FORMATEUR` | Q14 |
| RG11 | La note est définitive une fois la session clôturée ; plus aucune correction possible | Q15 (tranché, voir §7) |
| RG12 | Après 5 codes erronés, l'étudiant est bloqué 2 minutes | Q4 |
| RG13 | Une seule présence par étudiant et par session | Contrat (409 DEJA_PRESENT) |

## 7. Zones d'ombre, hypothèses et contradictions
| Point | Réponse client (Qx) ou hypothèse | Décision retenue | Pourquoi |
|---|---|---|---|
| Q10 vs Q15 | Q10 : « le relecteur peut corriger sa note tant que le formateur n'a pas clôturé » — Q15 : « une fois que le relecteur a validé, c'est fini, il ne peut plus y revenir » | **Tranché en faveur de Q10** : correction possible jusqu'à la clôture de la session (RG6, RG11) | Q10 décrit un cas d'usage concret (faute de frappe), Q15 exprime une intention générale d'honnêteté ; la clôture matérialise cette intention |
| Q8 vs Q15 | Q8 dit que l'étudiant voit sa note — mais si la note reste modifiable, que voit-il ? | La note affichée est toujours la dernière validée ; une correction la met à jour | Q8 (voir la note) et Q10 (pouvoir corriger) sont compatibles si l'affichage suit la dernière version |
| Q4 — trou non vu | Q4 impose un blocage 2 min après 5 codes erronés, mais aucune question ne demande de détecter ces échecs | Comptage des échecs par (étudiant, session) en base, fenêtre glissante, réinitialisé à la réussite ou à l'expiration du blocage | Le blocage de Q4 est impossible sans traçage des tentatives ; décision écrite à la place du client |
| Q7 vs session vide | Q7 : relecteur parmi les présents — que faire s'il n'y a aucun autre présent ? | L'exercice reste « EN_ATTENTE » sans relecteur, visible dans le tableau (RG7) | Hypothèse la plus prudente, cohérente avec Q11 |
| Fin de session | Le sujet parle d'« expiration du code » (Q2) et de « fin de la session » (Q3, Q12) comme si c'était la même chose | Deux instants distincts : le code expire à H+15 ; la session reste ouverte pour les dépôts jusqu'à la clôture par le formateur | Q12 prouve que la session survit à l'expiration du code (« certains n'ont pas de connexion le soir même ») |

## 8. Contraintes techniques
B1 Java 17+, Maven, wrapper `mvnw` commité · B2 contrat `api/contrat.yaml` respecté à la lettre (chemins, verbes, codes, format d'erreur) · B3 couches controller/service/repository, DTO partout, aucune entité JPA exposée en JSON · B4 validation + `@RestControllerAdvice`, erreurs `{ code, message }`, jamais de stack trace · B5 schéma versionné par Flyway, migrations commitées, `ddl-auto=update` interdit hors tests · B6 un test unitaire sur une règle métier réelle + un test d'intégration sur un endpoint, tournant sans base locale · F1 Next.js justifié dans le README, build qui passe · F2 trois écrans : formateur, étudiant, relecteur · F3 couche API dédiée, états de chargement/erreur, moyenne jamais recalculée côté frontend.

## 9. Livrables
Dépôt public `kfokam48-epreuve-269` : `docs/CAHIER_DES_CHARGES.md`, `docs/diagrammes/` (D1–D3 + D4 bonus, Mermaid), `docs/JOURNAL.md`, `api/contrat.yaml` complété, `backend/` Spring Boot (Flyway, DTO, tests), `frontend/` Next.js (3 écrans), `CHANGELOG.md`, `README.md` testé depuis un clone vierge, backlog en issues GitHub, `SOUMISSION.md`. Second dépôt `kfokam48-gitlab-269` à l'étape 5.

## 10. Démarche prévue
Étape 1 : analyse, diagrammes, contrat, issues → commit `[JALON] analyse`. Étape 2 : stories Must uniquement, une branche par ticket, une PR par branche, issues fermées par les commits → `[JALON] v0.1`. Étape 3 : ouverture de l'enveloppe, issue avant de coder, migration versionnée, contrat et analyse mis à jour. Étape 4 : `[JALON] v1.0`, CHANGELOG, README testé. Étape 5 : épreuve git-lab sur le second dépôt. Étape 6 : SOUMISSION.md.
**Definition of Done :** un ticket est terminé quand son code est revu en PR, les tests passent, la règle RGx citée est couverte par un test ou une vérification explicite, l'issue est fermée par un commit, et tout est poussé sur origin.
