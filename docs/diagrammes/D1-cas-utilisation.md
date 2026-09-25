# D1 — Cas d'utilisation

```mermaid
graph LR
    formateur([Formateur])
    etudiant([Étudiant])
    utilisateur([Utilisateur connecté])
    systeme(["Système (auto)"])

    subgraph Presence55
      UC1([Ouvrir une session → code])
      UC2([Clôturer une session])
      UC3([Ajouter une présence à la main])
      UC4([Consulter le tableau])
      UC5([Marquer sa présence avec le code])
      UC6([Déposer le lien de son exercice])
      UC7([Remplacer son lien tant que non relu])
      UC8([Voir sa note et son commentaire])
      UC9([Rendre une relecture notée /20])
      UC10([Corriger sa note avant clôture])
      UC11([Assigner le relecteur au hasard])
      UC12([Expirer le code à H+15])
      UC13([Bloquer 2 min après 5 échecs])
      UC14([Se connecter avec ses identifiants])
      UC15([Créer une promotion])
      UC16([Inscrire un étudiant → compte auto])
    end

    utilisateur --> UC14
    formateur --> UC1
    formateur --> UC2
    formateur --> UC3
    formateur --> UC4
    formateur --> UC15
    formateur --> UC16
    etudiant --> UC5
    etudiant --> UC6
    etudiant --> UC7
    etudiant --> UC8
    etudiant --> UC9
    etudiant --> UC10
    systeme --> UC11
    systeme --> UC12
    systeme --> UC13
    UC11 -. RG2 : jamais soi-même .-> UC9
    UC16 -. compte créé automatiquement .-> UC14
```

**Évolutions post-soumission (CDC 7.14/7.15)** : UC14 connexion par comptes (BCrypt + JWT, rôle
FORMATEUR ou ETUDIANT, identité portée par le token) ; UC15/UC16 gestion des promotions et des
étudiants, réservées au formateur. En mode `AUTH_REQUIS=false` (contrat initial, Q1), UC14
disparaît : l'étudiant choisit librement son nom dans la liste.
