# Cahier des charges — Plateforme de gestion des sessions, présences et relectures

Auteur : KF48-...-269 (zidane2001) · Version : 2.0 (remplace la v1, décision du PO) · Frontend choisi : React (via Next.js) — pour permettre une interface web simple, responsive et adaptée aux trois espaces fonctionnels demandés.

## 1. Contexte et objectif

### 1.1 Contexte
La direction de la formation KFOKAM48 souhaite disposer d'une application web permettant de gérer le suivi des étudiants pendant les sessions de cours.
Le besoin couvre cinq fonctions principales :
- le formateur ouvre une session et obtient un code de présence ;
- l'étudiant utilise ce code pour enregistrer sa présence ;
- l'étudiant dépose le lien de son exercice ;
- le système attribue un exercice à relire à un autre étudiant présent ;
- le formateur dispose d'un tableau de suivi des étudiants.

L'application doit également gérer les règles métier liées à l'expiration du code, aux tentatives incorrectes, à l'attribution des relectures, aux notes et à la clôture des sessions.

### 1.2 Objectif
L'objectif est de fournir une application web permettant :
- au formateur de gérer ses sessions ;
- aux étudiants d'enregistrer leur présence ;
- aux étudiants de déposer leurs exercices ;
- au système d'organiser automatiquement les relectures ;
- aux étudiants de noter et commenter les exercices qui leur sont attribués ;
- aux auteurs d'exercices de consulter leur note et leur commentaire ;
- au formateur de suivre l'activité de chaque étudiant.

L'application doit rester simple d'utilisation et exploitable depuis un ordinateur comme depuis un téléphone.

## 2. Acteurs et rôles

| Acteur | Ce qu'il peut faire |
|---|---|
| Formateur | Ouvrir une session |
| Formateur | Consulter le code et les informations de la session |
| Formateur | Clôturer une session |
| Formateur | Ajouter manuellement une présence |
| Formateur | Consulter le tableau de suivi |
| Étudiant | Choisir son identité dans la liste proposée |
| Étudiant | Saisir un code de présence |
| Étudiant | Déposer le lien de son exercice |
| Étudiant | Modifier le lien sous les conditions prévues |
| Étudiant | Consulter la note et le commentaire reçus |
| Étudiant | Réaliser une relecture attribuée |
| Système | Générer le code de présence |
| Système | Vérifier l'expiration du code |
| Système | Bloquer temporairement un étudiant après trop d'erreurs |
| Système | Attribuer aléatoirement les relectures |
| Système | Empêcher l'auto-relecture |

Aucun système de mot de passe n'est prévu pour l'étudiant. L'identification de l'étudiant se fait par sélection de son nom dans une liste, conformément à Q1.

## 3. Périmètre

### 3.1 Inclus
Le projet comprend :
- gestion des promotions nécessaires au fonctionnement ;
- gestion des étudiants nécessaires au fonctionnement ;
- ouverture d'une session ;
- génération d'un code de présence ;
- expiration du code après 15 minutes ;
- enregistrement d'une présence ;
- contrôle des doubles présences ;
- blocage temporaire après cinq erreurs ;
- ajout manuel d'une présence par le formateur ;
- distinction entre présence étudiant et présence formateur ;
- dépôt d'un exercice ;
- validation du lien ;
- modification du lien sous conditions ;
- attribution automatique d'un relecteur ;
- interdiction de l'auto-relecture ;
- notation de 0 à 20 ;
- commentaire ;
- consultation de la note et du commentaire ;
- suivi des relectures en attente ;
- clôture d'une session ;
- tableau de suivi du formateur ;
- API REST ;
- interface web responsive ;
- données de démonstration ;
- tests unitaires et d'intégration ;
- documentation d'installation.

### 3.2 Exclus
Les éléments suivants ne font pas partie du périmètre initial :
- authentification par mot de passe ;
- inscription publique des étudiants ;
- paiement ;
- messagerie ;
- notifications SMS ;
- notifications email ;
- application mobile native Android/iOS ;
- dépôt physique de fichiers ;
- système complet d'administration des utilisateurs ;
- gestion avancée des droits et rôles ;
- correction automatique des exercices.

## 4. Exigences fonctionnelles

| Réf | Exigence | Critère d'acceptation | Priorité |
|---|---|---|---|
| EF1 | Le formateur peut ouvrir une session | Quand le titre et la promotion sont valides, une session est créée et un code est retourné | Must |
| EF2 | Le système génère une expiration | Lors de l'ouverture, expirationAt correspond à 15 minutes après ouvertureAt | Must |
| EF3 | L'étudiant peut marquer sa présence | Quand le code est valide et non expiré, une présence est créée | Must |
| EF4 | Le système refuse un code expiré | Quand le code est expiré, l'API retourne 410 CODE_EXPIRE | Must |
| EF5 | Le système empêche une double présence | Quand l'étudiant est déjà présent, l'API retourne 409 DEJA_PRESENT | Must |
| EF6 | Le système limite les tentatives incorrectes | Après cinq erreurs, l'étudiant ne peut plus tenter de code pendant deux minutes | Must |
| EF7 | Le formateur peut ajouter une présence | Une présence ajoutée manuellement possède source = FORMATEUR | Must |
| EF8 | L'étudiant peut déposer un exercice | Quand le lien est valide et aucun exercice n'est déjà déposé pour cette session, l'exercice est créé | Must |
| EF9 | Le système empêche un double dépôt | Un second dépôt pour le même étudiant et la même session retourne 409 | Must |
| EF10 | Le système attribue une relecture | Lorsqu'un exercice peut être relu, un étudiant présent autre que son auteur est sélectionné aléatoirement | Must |
| EF11 | L'étudiant peut réaliser une relecture | Le relecteur peut envoyer une note entière comprise entre 0 et 20 et un commentaire | Must |
| EF12 | L'auto-relecture est interdite | Si le relecteur est l'auteur de l'exercice, l'API retourne 403 | Must |
| EF13 | L'auteur peut consulter son résultat | L'auteur voit la note et le commentaire mais jamais l'identité du relecteur | Must |
| EF14 | L'étudiant peut modifier son lien sous condition | Le lien peut être remplacé tant que la relecture n'a pas commencé | Must |
| EF15 | Le formateur peut clôturer une session | Une session clôturée n'accepte plus les opérations qui dépendent de son ouverture | Must |
| EF16 | Le formateur peut consulter le tableau | Le tableau présente pour chaque étudiant sa présence, ses exercices déposés, sa moyenne et ses relectures en attente | Must |
| EF17 | Une relecture peut être modifiée avant clôture | Une relecture déjà envoyée reste modifiable jusqu'à la clôture de la session | Must |
| EF18 | Une relecture devient définitive après clôture | Après clôture de la session, aucune modification de la relecture n'est possible | Must |

## 5. Exigences non fonctionnelles

| Réf | Exigence | Vérification |
|---|---|---|
| ENF1 | Le backend doit utiliser Java 17 ou plus avec Maven | java -version et build Maven |
| ENF2 | Le wrapper Maven doit être versionné | Présence de mvnw et mvnw.cmd |
| ENF3 | L'API doit respecter le contrat imposé | Tests HTTP des cinq opérations obligatoires |
| ENF4 | L'application doit séparer contrôleur, service et repository | Inspection du code |
| ENF5 | Les entités JPA ne doivent pas être exposées directement | Présence et utilisation de DTO |
| ENF6 | Les entrées doivent être validées | Tests des données invalides |
| ENF7 | Les erreurs doivent être gérées centralement | @RestControllerAdvice et tests d'erreurs |
| ENF8 | Les erreurs API doivent respecter le format imposé | Vérification des réponses JSON |
| ENF9 | Le schéma de base doit être versionné | Migrations Flyway ou Liquibase |
| ENF10 | ddl-auto=update est interdit hors tests | Vérification de configuration |
| ENF11 | Un test unitaire doit vérifier une règle métier réelle | Exécution des tests |
| ENF12 | Un test d'intégration doit vérifier un endpoint | Exécution sur environnement vierge |
| ENF13 | Le frontend doit utiliser React, Angular ou Next.js | README et package |
| ENF14 | Le frontend doit proposer trois espaces fonctionnels | Vérification des écrans |
| ENF15 | Les appels API doivent être regroupés dans une couche dédiée | Inspection frontend |
| ENF16 | Les états de chargement et d'erreur doivent être gérés | Test manuel |
| ENF17 | La moyenne affichée doit provenir de l'API | Inspection du code frontend |
| ENF18 | L'interface étudiant doit rester utilisable sur mobile | Test sur viewport mobile |
| ENF19 | Le projet doit démarrer avec une procédure documentée courte | Test depuis un clone vierge |
| ENF20 | Des données de démonstration doivent être disponibles au démarrage | Test de l'application vierge |

## 6. Règles de gestion

| Réf | Règle | Source |
|---|---|---|
| RG1 | Le code de présence expire 15 minutes après l'ouverture de la session | Q2 |
| RG2 | Une présence ne peut plus être enregistrée après la fin de la session | Q3 |
| RG3 | Après cinq erreurs de code, l'étudiant est bloqué pendant deux minutes | Q4 |
| RG4 | Un étudiant ne peut enregistrer qu'une seule présence pour une session | Contrat API |
| RG5 | Un étudiant ne peut déposer qu'un seul exercice pour une session | Contrat API |
| RG6 | Le dépôt d'exercice reste possible après expiration du code, jusqu'à la clôture de la session | Q12 |
| RG7 | Le lien d'un exercice peut être remplacé tant qu'aucune relecture n'a commencé | Q13 |
| RG8 | Un exercice possède un seul relecteur | Q6 |
| RG9 | Le relecteur est choisi aléatoirement parmi les étudiants présents à la session | Q7 |
| RG10 | L'auteur d'un exercice ne peut pas relire son propre exercice | Q5 |
| RG11 | L'auteur voit sa note et son commentaire mais pas l'identité du relecteur | Q8 |
| RG12 | Une note est un nombre entier compris entre 0 et 20 inclus | Q9 |
| RG13 | Une présence ajoutée manuellement par le formateur porte la source FORMATEUR | Q14 + contrat API |
| RG14 | Une présence enregistrée par l'étudiant porte la source ETUDIANT | Contrat API |
| RG15 | Une relecture non rendue reste en attente et doit apparaître dans le tableau du formateur | Q11 |
| RG16 | La relecture reste modifiable jusqu'à la clôture de la session | Q10 |
| RG17 | Une relecture devient définitive lorsque la session est clôturée | Décision issue de Q10 |
| RG18 | Une session peut continuer à recevoir des exercices après l'expiration du code | Q2 + Q12 |
| RG19 | La clôture de session est distincte de l'expiration du code | Q2 + Q12 |
| RG20 | L'identité du relecteur ne doit pas être communiquée à l'auteur de l'exercice | Q8 |

## 7. Zones d'ombre, hypothèses et contradictions tranchées

Cette section contient les décisions prises lorsque le client n'a pas fourni suffisamment d'informations.

### 7.1 Contradiction Q10 / Q15
**Réponses du client** — Q10 : le relecteur peut corriger sa note tant que le formateur n'a pas clôturé la session. Q15 : une fois la note envoyée, elle est définitive.
Ces deux réponses sont contradictoires.

**Décision** — La règle Q10 est retenue : une relecture envoyée reste modifiable jusqu'à la clôture de la session. Après clôture, elle devient définitive.

**Justification** — Q10 fournit une condition temporelle précise et directement exploitable : la clôture de la session. Cette décision permet de définir clairement le cycle de vie d'une relecture et évite d'avoir deux règles contradictoires. Q15 est donc interprétée comme l'intention de rendre la note définitive, mais cette finalisation intervient dans notre modèle lors de la clôture.

### 7.2 Clôture d'une session
Le client mentionne plusieurs fois la clôture mais aucune opération permettant au formateur de clôturer une session n'est fournie dans les cinq endpoints obligatoires.

**Décision** — Ajouter une opération complémentaire : `POST /api/sessions/{id}/cloture`. Elle retourne 200 lorsque la clôture est réussie. La clôture change l'état de la session : OUVERTE → CLOTUREE. Cette opération est ajoutée au contrat car le sujet autorise l'ajout d'opérations au-delà des cinq opérations imposées.

### 7.3 Gestion des étudiants et promotions
Le client indique que l'étudiant choisit son nom dans une liste, mais ne précise pas comment les étudiants et promotions sont créés.

**Décision** — Les étudiants et promotions nécessaires au fonctionnement sont considérés comme des données préexistantes et sont fournies avec les données de démonstration. La création complète des étudiants et promotions est hors périmètre.

### 7.4 Plusieurs sessions simultanées
Le client ne précise pas si plusieurs sessions peuvent être ouvertes simultanément pour une même promotion.

**Décision** — Une promotion peut posséder plusieurs sessions, mais une seule session peut être active pour une promotion donnée. Cette décision évite qu'un étudiant ou un formateur ne puisse confondre deux codes de présence actifs pour la même promotion.

### 7.5 Dépôt par un étudiant non présent
Q12 autorise le dépôt jusqu'à la clôture mais ne dit pas que l'étudiant doit être présent.

**Décision** — La présence n'est pas une condition obligatoire au dépôt. Un étudiant peut donc déposer son exercice jusqu'à la clôture même si son code de présence a expiré ou s'il n'a pas marqué sa présence. Cette décision suit la formulation explicite de Q12.

### 7.6 Aucun autre étudiant présent
Q5 interdit l'auto-relecture et Q7 impose de choisir un étudiant présent. Si un seul étudiant est présent et qu'il dépose son exercice, aucun relecteur valide n'existe.

**Décision** — L'exercice reste en état : EN_ATTENTE_RELECTEUR. Il n'est pas attribué à son auteur.

### 7.7 Étudiant présent sans exercice
Q7 indique que le relecteur est choisi parmi les étudiants présents.

**Décision** — Un étudiant présent peut être choisi comme relecteur même s'il n'a pas encore déposé son propre exercice. Cela respecte directement la formulation de Q7.

### 7.8 Début d'une relecture
Q13 permet de modifier le lien tant que personne n'a commencé à relire. Le sujet ne définit pas ce qui constitue précisément le début de la relecture.

**Décision** — L'attribution du relecteur est considérée comme le début de la relecture. Ainsi : EXERCICE_DEPOSE → RELECTEUR_ATTRIBUE → lien non modifiable.

### 7.9 Moyenne sans note
Q16 exige une moyenne mais ne précise pas la valeur lorsqu'aucune note n'existe.

**Décision** — L'API retourne null lorsque l'étudiant n'a encore reçu aucune note. Le frontend affiche « — » plutôt que 0, car zéro est une note valide.

### 7.10 Relecture en attente lors de la clôture
Q11 demande que les relectures non rendues restent « en attente ».

**Décision** — Une relecture non rendue au moment de la clôture reste historiquement en état EN_ATTENTE. Elle apparaît dans le tableau du formateur et n'est plus modifiable après clôture.

### 7.11 Commentaire
Le contrat prévoit un champ commentaire mais ne précise pas explicitement son caractère obligatoire.

**Décision** — Le commentaire est obligatoire lors de la soumission d'une relecture. Cette décision garantit qu'une note est accompagnée d'un retour pédagogique.

## 8. Contraintes techniques

**Backend** — Java 17 ou supérieur ; Spring Boot ; Maven ; wrapper Maven versionné ; API REST ; séparation Controller / Service / Repository ; DTO ; validation ; @RestControllerAdvice ; migrations Flyway ou Liquibase ; aucun ddl-auto=update hors tests ; tests unitaires ; tests d'intégration.

**Frontend** — React ; appels API dans une couche dédiée ; gestion des états de chargement ; gestion des erreurs ; aucune duplication de règle métier ; trois espaces fonctionnels.

**API** — Les cinq opérations imposées sont obligatoires et doivent respecter exactement les chemins, verbes, corps, réponses et codes HTTP du contrat. Les erreurs utilisent systématiquement : `{ "code": "CODE_ERREUR", "message": "Message explicatif." }`

**Infrastructure** — L'application doit pouvoir être démarrée avec Docker Compose ou avec au maximum trois commandes documentées dans le README. Des données de démonstration doivent être disponibles.

## 9. Livrables

Le dépôt doit contenir :
```
/
├── docs/
│   ├── CAHIER_DES_CHARGES.md
│   ├── JOURNAL.md
│   └── diagrammes/
│       ├── D1-cas-utilisation.md
│       ├── D2-modele-donnees.md
│       ├── D3-sequence-presence.md
│       └── D4-cycle-vie-exercice.md
├── api/
│   └── contrat.yaml
├── backend/
│   ├── pom.xml
│   ├── mvnw
│   └── src/
├── frontend/
│   └── ...
├── README.md
├── CHANGELOG.md
└── .gitignore
```

Les trois jalons obligatoires sont : `[JALON] analyse`, `[JALON] v0.1`, `[JALON] v1.0`

## 10. Démarche prévue

**Étape 1 — Analyse et conception** — Avant tout code : finaliser le cahier des charges ; finaliser les exigences fonctionnelles ; finaliser les règles de gestion ; produire D1 ; produire D2 ; produire D3 ; produire D4 ; compléter le contrat API ; créer les issues GitHub ; vérifier la cohérence entre API, modèle et règles ; créer le commit `[JALON] analyse`. Aucun code Spring Boot ne doit être créé avant cette étape.

**Étape 2 — Version v0.1** — Développer uniquement les fonctionnalités Must. Pour chaque issue : Issue → branche → développement → tests → PR → commit explicite → fermeture de l'issue. Puis : `[JALON] v0.1` et push.

**Étape 3 — Changement de besoin** — Après le push de v0.1, récupérer l'enveloppe. Le bug et le changement de besoin doivent être traités comme une évolution réelle : ouvrir une issue ; reproduire le bug ; identifier son impact ; modifier la migration ; mettre à jour le contrat ; mettre à jour le cahier des charges ; mettre à jour les diagrammes ; reprioriser le backlog ; corriger ; tester.

**Étape 4 — Version finale** — terminer les fonctionnalités restantes ; intégrer le changement de l'enveloppe ; vérifier le contrat API ; vérifier les tests ; tester l'installation depuis un clone vierge ; mettre à jour le README ; produire le CHANGELOG ; trier les issues restantes ; créer `[JALON] v1.0`.

**Étape 5 — Soumission** — Créer SOUMISSION.md avec : nom ; matricule ; centre ; URL du dépôt public ; hash complet du commit final ; framework frontend ; commande de démarrage. La soumission doit être déposée avant 18h00.

**Definition of Done** — Une issue est considérée comme terminée lorsque : son critère d'acceptation est satisfait ; le comportement a été testé ; le code respecte l'architecture ; les erreurs prévues sont gérées ; aucune règle métier n'est dupliquée ; la documentation concernée est mise à jour si nécessaire ; le commit est atomique et explicite ; la branche correspondante possède une PR ; l'issue est correctement référencée et fermée ; aucune régression connue n'est introduite.
