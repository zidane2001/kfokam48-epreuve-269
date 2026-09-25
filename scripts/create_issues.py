"""Crée les 15 issues du backlog sur GitHub (API REST, token via variable d'environnement).

Usage: GITHUB_TOKEN=ghp_xxx python scripts/create_issues.py
Les issues recopient docs/BACKLOG.md (miroir local) avec labels de priorité.
"""
import json
import os
import sys
import urllib.request

REPO = "zidane2001/kfokam48-epreuve-269"
TOKEN = os.environ.get("GITHUB_TOKEN", "").strip()
if not TOKEN:
    sys.exit("Erreur: exporte GITHUB_TOKEN avant de lancer.")

ISSUES = [
    ("Le projet démarre chez un tiers : squelette backend + frontend + docker compose", "Must",
     "EF1, RNF4",
     "- ./mvnw spring-boot:run et npm run dev tournent depuis un clone vierge\n- Flyway V1__init.sql commitee\n- donnees de demo chargees au demarrage\n- docker compose up fonctionnel"),
    ("L'étudiant marque sa présence avec le code", "Must",
     "EF2, RG1, RG12, RG13",
     "- 201 nominal { id, sessionId, etudiantId, source }\n- 400 CODE_INCONNU\n- 409 DEJA_PRESENT\n- 410 CODE_EXPIRE (RG1)\n- 429 TROP_DE_TENTATIVES apres 5 echecs (RG12)\n- test d'integration vert"),
    ("Le formateur ouvre une session et obtient un code expirant à H+15", "Must",
     "EF1, RG1",
     "- 201 { id, code, ouvertureAt, expirationAt }\n- expirationAt = ouvertureAt + 15 min\n- 400 si titre ou promotionId manquant"),
    ("L'étudiant dépose le lien de son exercice et un relecteur est assigné", "Must",
     "EF3, EF4, RG2, RG3, RG4",
     "- 201 { id, statut }\n- 400 LIEN_INVALIDE\n- 409 EXERCICE_DEJA_DEPOSE\n- relecteur = present != depositant (RG2, RG4)\n- sans autre present : reste EN_ATTENTE (RG7)"),
    ("Le relecteur rend sa note et son commentaire", "Must",
     "EF5, RG5, RG2",
     "- 200 nominal\n- 400 NOTE_INVALIDE si hors 0-20 ou non entiere (RG5)\n- 403 AUTO_RELECTURE (RG2)\n- 409 RELECTURE_DEJA_RENDUE\n- test unitaire sur RG5"),
    ("Le formateur consulte le tableau par promotion", "Must",
     "EF7, RG7, Q16",
     "- 200 [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]\n- 404 promotion inconnue\n- moyenne calculee cote API uniquement (F3)"),
    ("L'écran étudiant permet présence + dépôt d'exercice", "Must",
     "EF2, EF4, F2, F3",
     "- saisie du code, erreurs { code, message } affichees\n- depot du lien\n- couche API dediee, etats chargement/erreur"),
    ("L'écran relecteur liste les relectures à faire et permet de noter", "Must",
     "EF5, F2",
     "- liste depuis GET /api/relectures-a-faire\n- note 0-20 + commentaire\n- erreurs affichees lisiblement"),
    ("L'écran formateur ouvre une session et affiche le tableau", "Must",
     "EF1, EF7, F2, F3",
     "- ouverture -> code affiche\n- tableau rafraichi par promotion\n- aucune moyenne recalculee cote front"),
    ("Le formateur clôture une session", "Should",
     "EF11, RG11",
     "- POST /api/sessions/{id}/cloture -> 200\n- 409 SESSION_DEJA_CLOTUREE\n- apres cloture : correction de note refusee, depot refuse"),
    ("Le relecteur corrige sa note avant clôture", "Should",
     "EF6, RG6",
     "- PUT /api/relectures/{id} -> 200\n- 409 SESSION_CLOTUREE apres cloture (RG11)\n- l'affichage etudiant suit la derniere version (decision Q8/Q15)"),
    ("Le formateur ajoute une présence à la main (source FORMATEUR)", "Should",
     "EF8, RG10",
     "- 201 source=FORMATEUR (RG10)\n- visible comme ajoute par le formateur dans le tableau (Q14)"),
    ("L'étudiant voit ses notes reçues sans le nom du relecteur", "Should",
     "EF9, Q8",
     "- GET /api/etudiants/{id}/relectures-recues : note + commentaire\n- relecteurId jamais expose (Q8)"),
    ("L'étudiant remplace son lien tant que personne n'a relu", "Could",
     "EF10, RG9",
     "- PUT /api/exercices/{id} -> 200\n- 409 RELECTURE_COMMENCEE si relecture commencee (RG9)"),
    ("Blocage 2 min après 5 codes erronés", "Should",
     "EF12, RG12",
     "- 429 TROP_DE_TENTATIVES pendant 2 min (RG12)\n- compteur reinitialise a la reussite\n- trace des tentatives en base (decision Q4, section 7 du cahier des charges)"),
]


def api(url, data, method="POST"):
    req = urllib.request.Request(
        url,
        data=json.dumps(data).encode(),
        headers={
            "Authorization": f"Bearer {TOKEN}",
            "Accept": "application/vnd.github+json",
            "User-Agent": "kfokam48-epreuve-269",
        },
        method=method,
    )
    with urllib.request.urlopen(req) as r:
        return json.loads(r.read())


for i, (titre, prio, refs, crit) in enumerate(ISSUES, 1):
    body = (
        f"Priorite : **{prio}**\nRenvois : {refs}\n\n"
        f"## Criteres d'acceptation\n{crit}\n\n"
        f"_Miroir : docs/BACKLOG.md#{i} — une branche par ticket, une PR par branche, "
        f"fermee par commit referencant le numero de cette issue (`closes #<numero>`)._"
    )
    res = api(f"https://api.github.com/repos/{REPO}/issues",
              {"title": titre, "body": body, "labels": [prio]})
    print(f"issue #{res['number']} : {titre}")

print("OK —", len(ISSUES), "issues creees.")
