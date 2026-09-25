package com.kfokam48.presence55.api;

import com.kfokam48.presence55.domain.*;
import com.kfokam48.presence55.dto.EspaceDtos.*;
import com.kfokam48.presence55.dto.ExerciceDtos.DepotResponse;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.*;
import com.kfokam48.presence55.service.ExerciceService;
import com.kfokam48.presence55.service.RelectureService;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Endpoints de lecture/interaction des espaces formateur et etudiant,
 * au format attendu par le design (frontendModel) — conformes au CDC v2.
 */
@RestController
public class EspaceController {

    private final SessionRepository sessions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;
    private final TentativeCodeRepository tentatives;
    private final ExerciceService exerciceService;
    private final RelectureService relectureService;

    public EspaceController(SessionRepository sessions, EtudiantRepository etudiants,
                            PresenceRepository presences, ExerciceRepository exercices,
                            RelectureRepository relectures, TentativeCodeRepository tentatives,
                            ExerciceService exerciceService, RelectureService relectureService) {
        this.sessions = sessions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
        this.tentatives = tentatives;
        this.exerciceService = exerciceService;
        this.relectureService = relectureService;
    }

    // ---------- Espace etudiant ----------

    /** GET statut de presence : present ? source ? combien d'essais restants ? blocage jusqu'a quand ? (RG3) */
    @GetMapping("/api/sessions/{sessionId}/etudiants/{etudiantId}/statut-presence")
    public StatutPresenceDto statutPresence(@PathVariable Long sessionId, @PathVariable Long etudiantId) {
        SessionCours s = session(sessionId);
        etudiant(etudiantId);
        OffsetDateTime maintenant = OffsetDateTime.now();
        boolean present = presences.existsBySessionIdAndEtudiantId(sessionId, etudiantId);
        long echecs = tentatives.countBySessionIdAndEtudiantIdAndEchoueAtAfter(
                sessionId, etudiantId, maintenant.minusMinutes(2));
        return new StatutPresenceDto(sessionId, etudiantId, present, null, null,
                (int) Math.max(0, 5 - echecs), null);
    }

    /** GET presences de la session (vue formateur, avec noms). */
    @GetMapping("/api/sessions/{sessionId}/presences")
    @Transactional(readOnly = true)
    public List<PresenceSessionDto> presences(@PathVariable Long sessionId) {
        session(sessionId);
        return presences.findBySessionId(sessionId).stream()
                .map(p -> etudiants.findById(p.getEtudiantId())
                        .map(e -> new PresenceSessionDto(p.getId(), sessionId, p.getEtudiantId(),
                                e.getPrenom() + " " + e.getNom(), p.getSource().name(),
                                p.getEnregistreeAt().toString()))
                        .orElseThrow())
                .toList();
    }

    /** POST presence manuelle du formateur -> source=FORMATEUR (EF7/RG13). */
    @PostMapping("/api/sessions/{sessionId}/presences/manuelle")
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceSessionDto presenceManuelle(@PathVariable Long sessionId,
                                               @RequestBody AjoutManuelRequest req) {
        SessionCours s = session(sessionId);
        if (s.isCloturee()) {
            throw new BusinessException("SESSION_CLOTUREE", "Session cloturee : ajout impossible.");
        }
        Etudiant e = etudiant(req.etudiantId());
        if (presences.existsBySessionIdAndEtudiantId(sessionId, e.getId())) {
            throw new BusinessException("DEJA_PRESENT", "Cet etudiant est deja present.");
        }
        Presence p = presences.save(new Presence(sessionId, e.getId(),
                Presence.Source.FORMATEUR, OffsetDateTime.now()));
        return new PresenceSessionDto(p.getId(), sessionId, e.getId(),
                e.getPrenom() + " " + e.getNom(), p.getSource().name(), p.getEnregistreeAt().toString());
    }

    /** POST depot d'exercice pour une session (EF8) — meme contrat que POST /api/exercices. */
    @PostMapping("/api/sessions/{sessionId}/exercices")
    @ResponseStatus(HttpStatus.CREATED)
    public DepotResponse deposer(@PathVariable Long sessionId,
                                 @RequestBody DepotSurSessionRequest req) {
        return exerciceService.deposer(new com.kfokam48.presence55.dto.ExerciceDtos.DepotRequest(
                sessionId, req.etudiantId(), req.lien()));
    }

    /** PUT lien d'un exercice (EF14/RG7) : tant que la relecture n'a pas commence. */
    @PutMapping("/api/exercices/{id}/lien")
    public ExerciceAuteurDto modifierLien(@PathVariable Long id, @RequestBody ModifierLienRequest req) {
        Exercice ex = exercices.findById(id)
                .orElseThrow(() -> new BusinessException("EXERCICE_INCONNU", "Exercice inconnu."));
        if (!ex.getEtudiantId().equals(req.etudiantId())) {
            throw new BusinessException("EXERCICE_AUTRE_ETUDIANT",
                    "Cet exercice n'appartient pas a cet etudiant.");
        }
        if (ex.getStatut() != Exercice.Statut.EN_ATTENTE_RELECTEUR) {
            // 7.8 : l'attribution du relecteur = debut de la relecture
            throw new BusinessException("RELECTURE_COMMENCEE",
                    "La relecture a commence : le lien ne peut plus etre modifie.");
        }
        ex.setLien(ExerciceService.validerLien(req.lien()));
        exercices.save(ex);
        return versAuteur(ex, null);
    }

    /** GET mes exercices, vue auteur : jamais l'identite du relecteur (EF13/RG11/RG20). */
    @GetMapping("/api/etudiants/{etudiantId}/exercices")
    public List<ExerciceAuteurDto> mesExercices(@PathVariable Long etudiantId) {
        etudiant(etudiantId);
        return exercices.findByEtudiantId(etudiantId).stream()
                .map(ex -> versAuteur(ex, relectures.findByExerciceId(ex.getId()).orElse(null)))
                .toList();
    }

    /** GET relectures assignees a l'etudiant (EF11), avec lien modifiable (RG16/17). */
    @GetMapping("/api/etudiants/{etudiantId}/relectures")
    public List<RelectureAssigneeDto> mesRelectures(@PathVariable Long etudiantId) {
        etudiant(etudiantId);
        return relectures.findByRelecteurId(etudiantId).stream()
                .map(r -> {
                    Exercice ex = exercices.findById(r.getExerciceId())
                            .orElseThrow(() -> new BusinessException("EXERCICE_INCONNU", "Exercice inconnu."));
                    SessionCours s = sessions.findById(ex.getSessionId()).orElse(null);
                    boolean modifiable = r.getStatut() == Relecture.Statut.RENDUE
                            && s != null && !s.isCloturee();          // RG16 : avant cloture
                    return new RelectureAssigneeDto(r.getId(), ex.getId(), ex.getSessionId(),
                            s == null ? "?" : s.getTitre(), ex.getLien(),
                            r.getNote(), r.getCommentaire(), r.getStatut().name(),
                            modifiable, r.getAttribueeAt().toString(), 
                            r.getRendueAt() == null ? null : r.getRendueAt().toString());
                })
                .toList();
    }

    /** PUT relecture : rendre (EF11) ou corriger avant cloture (EF17/RG16). */
    @PutMapping("/api/relectures/{id}")
    public RelectureAssigneeDto soumettreRelecture(@PathVariable Long id,
                                                   @RequestBody SoumissionRelectureRequest req) {
        Relecture r = relectures.findById(id)
                .orElseThrow(() -> new BusinessException("RELECTURE_INCONNUE", "Relecture inconnue."));
        Exercice ex = exercices.findById(r.getExerciceId())
                .orElseThrow(() -> new BusinessException("EXERCICE_INCONNU", "Exercice inconnu."));
        SessionCours s = sessions.findById(ex.getSessionId())
                .orElseThrow(() -> new BusinessException("SESSION_INCONNUE", "Session inconnue."));

        if (!r.getRelecteurId().equals(req.relecteurId())) {
            throw new BusinessException("RELECTURE_AUTRE_ETUDIANT",
                    "Cette relecture n'est pas assignee a cet etudiant.");
        }
        if (s.isCloturee() && r.getStatut() == Relecture.Statut.EN_ATTENTE) {
            throw new BusinessException("SESSION_CLOTUREE",
                    "Session cloturee : cette relecture ne peut plus etre rendue.");
        }
        if (r.getStatut() == Relecture.Statut.RENDUE && s.isCloturee()) {
            throw new BusinessException("SESSION_CLOTUREE",
                    "RG17 : la relecture est definitive apres cloture.");  // EF18
        }
        if (req.note() == null || req.note() < 0 || req.note() > 20) {
            throw new BusinessException("NOTE_INVALIDE", "La note doit etre un entier entre 0 et 20.");
        }
        if (req.commentaire() == null || req.commentaire().isBlank()) {
            throw new BusinessException("COMMENTAIRE_MANQUANT", "Le commentaire est obligatoire.");
        }

        if (r.getStatut() == Relecture.Statut.EN_ATTENTE) {
            r.rendre(req.note(), req.commentaire(), OffsetDateTime.now());
            ex.setStatut(Exercice.Statut.RELU);
            exercices.save(ex);
        } else {
            r.corriger(req.note(), OffsetDateTime.now());            // EF17 : correction RG16
        }
        relectures.save(r);
        return mesRelectures(req.relecteurId()).stream()
                .filter(d -> d.id().equals(r.getId())).findFirst().orElseThrow();
    }

    // ---------- helpers ----------

    private SessionCours session(Long id) {
        return sessions.findById(id).orElseThrow(
                () -> new BusinessException("SESSION_INCONNUE", "Cette session n'existe pas."));
    }

    private Etudiant etudiant(Long id) {
        return etudiants.findById(id).orElseThrow(
                () -> new BusinessException("ETUDIANT_INCONNU", "Cet etudiant n'existe pas."));
    }

    /** Vue auteur d'un exercice : note/commentaire OK, relecteur JAMAIS expose (RG11/RG20). */
    private ExerciceAuteurDto versAuteur(Exercice ex, Relecture r) {
        SessionCours s = sessions.findById(ex.getSessionId()).orElse(null);
        boolean definitif = s != null && s.isCloturee();              // EF18 : apres cloture
        return new ExerciceAuteurDto(ex.getId(), ex.getSessionId(),
                s == null ? "?" : s.getTitre(),
                s == null ? "OUVERTE" : (s.isCloturee() ? "CLOTUREE" : "OUVERTE"),
                ex.getLien(), ex.getDeposeAt().toString(), ex.getStatut().name(),
                ex.getStatut() == Exercice.Statut.EN_ATTENTE_RELECTEUR,      // lienModifiable (7.8)
                r == null ? null : r.getNote(),
                r == null ? null : r.getCommentaire(),
                definitif);
    }
}
