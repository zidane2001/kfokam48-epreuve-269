# Presence55 — KFOKAM48

Application de gestion des sessions de cours, présences et relectures par les pairs pour la direction de la formation KFOKAM48.

**Frontend : Next.js (React)** — choisi parce que le sujet l'autorise explicitement et qu'il fournit en un seul projet le serveur de développement, le routage et le build ; l'interface reproduit la maquette validée par le product owner (design system + Framer Motion).

## Démarrage (3 commandes)

Prérequis : Java 17+, Node 18+, une base PostgreSQL accessible.

```bash
# 1. Backend (port 8080)
cd backend && ./mvnw spring-boot:run

# 2. Frontend (port 3000, dans un second terminal)
cd frontend && npm install && npm run dev
```

Ouvrir http://localhost:3000 — des **données de démonstration** (1 promotion, 8 étudiants) sont chargées automatiquement si la base est vide.

### Configuration de la base

Le backend lit les variables d'environnement (valeurs par défaut : PostgreSQL local) :

```bash
export DATABASE_URL="jdbc:postgresql://hote:5432/presence55"
export DATABASE_USER="postgres"
export DATABASE_PASSWORD="postgres"
```

Le schéma est versionné par **Flyway** (migrations `V1`→`V4` appliquées au démarrage).

## Tests

```bash
cd backend && ./mvnw test     # 29 tests (H2 mémoire, aucune base requise)
cd frontend && npm run build  # build de production
```

## Structure

```
docs/       cahier des charges, diagrammes Mermaid, journal, backlog
api/        contrat OpenAPI (5 opérations imposées + opérations ajoutées)
backend/    Spring Boot 3 (Java 17, Flyway, DTO, @RestControllerAdvice)
frontend/   Next.js + design system du PO (Framer Motion, Tailwind)
```

## Règles métier principales

Code de présence expirant à H+15 (RG1) · blocage 2 min après 5 erreurs (RG3) · une présence et un exercice par étudiant et session (RG4/RG5) · relecteur tiré au sort parmi les présents, jamais l'auteur (RG9/RG10) · note entière 0–20 avec commentaire obligatoire (RG12) · correction possible jusqu'à la clôture, définitive après (RG16/RG17) · l'auteur ne voit jamais l'identité du relecteur (RG20).

Voir `docs/CAHIER_DES_CHARGES.md` pour le détail complet.
