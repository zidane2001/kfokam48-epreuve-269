package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Exercice;
import com.kfokam48.presence55.domain.Presence;
import com.kfokam48.presence55.domain.Relecture;
import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.dto.SuiviDtos.Suivi;
import com.kfokam48.presence55.dto.SuiviDtos.SuiviLigne;
import com.kfokam48.presence55.dto.SuiviDtos.Totaux;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/** Vue riche du tableau de suivi (design frontendModel) — F3 : moyennes calculees serveur. */
@Service
public class SuiviService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final SessionRepository sessions;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public SuiviService(PromotionRepository promotions,
                        EtudiantRepository etudiants,
                        SessionRepository sessions,
                        PresenceRepository presences,
                        ExerciceRepository exercices,
                        RelectureRepository relectures) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.sessions = sessions;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    /**
     * Tableau de suivi : une ligne par etudiant (Q16). Filtre optionnel par session
     * (RG15, SESSION_HORS_PROMOTION 400). Moyennes et totaux calcules ICI (F3).
     */
    @Transactional(readOnly = true)
    public Suivi parPromotion(Long promotionId, Long sessionId) {
        promotions.findById(promotionId).orElseThrow(
                () -> new BusinessException("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));

        List<SessionCours> scope;
        if (sessionId != null) {
            SessionCours session = sessions.findById(sessionId).orElseThrow(
                    () -> new BusinessException("SESSION_INCONNUE", "Cette session n'existe pas."));
            if (!session.getPromotionId().equals(promotionId)) {
                throw new BusinessException("SESSION_HORS_PROMOTION",
                        "Cette session n'appartient pas à la promotion.");
            }
            scope = List.of(session);
        } else {
            scope = sessions.findByPromotionId(promotionId);
        }

        List<Long> sessionIds = scope.stream().map(SessionCours::getId).toList();
        List<Long> etudiantIds = etudiants.findByPromotionId(promotionId).stream()
                .map(com.kfokam48.presence55.domain.Etudiant::getId).toList();

        List<Presence> presencesScope = sessionIds.stream()
                .flatMap(sid -> presences.findBySessionId(sid).stream()).toList();
        List<Exercice> exercicesScope = sessionIds.stream()
                .flatMap(sid -> exercices.findBySessionId(sid).stream()).toList();
        List<Relecture> relecturesScope = exercicesScope.stream()
                .flatMap(ex -> relectures.findAllByExerciceId(ex.getId()).stream()).toList();

        List<SuiviLigne> lignes = etudiantIds.stream()
                .map(eid -> ligneDe(eid, sessionId != null, presencesScope, exercicesScope, relecturesScope))
                .toList();

        return new Suivi(promotionId, sessionId, lignes,
                totauxDe(sessionId != null, lignes, exercicesScope, relecturesScope, scope.size()));
    }

    private SuiviLigne ligneDe(Long etudiantId, boolean parSession,
                               List<Presence> presencesScope,
                               List<Exercice> exercicesScope,
                               List<Relecture> relecturesScope) {
        List<Presence> mesPresences = presencesScope.stream()
                .filter(p -> p.getEtudiantId().equals(etudiantId)).toList();
        List<Exercice> mesExercices = exercicesScope.stream()
                .filter(x -> x.getEtudiantId().equals(etudiantId)).toList();
        List<Long> mesExerciceIds = mesExercices.stream().map(Exercice::getId).toList();

        List<Integer> notesRecues = relecturesScope.stream()
                .filter(r -> r.getStatut() == Relecture.Statut.RENDUE && r.getNote() != null)
                .filter(r -> mesExerciceIds.contains(r.getExerciceId()))
                .map(Relecture::getNote)
                .toList();

        OptionalDouble moyenne = notesRecues.stream().mapToInt(Integer::intValue).average();
        Presence premiere = mesPresences.stream()
                .min(java.util.Comparator.comparing(Presence::getEnregistreeAt)).orElse(null);

        return new SuiviLigne(
                etudiantId,
                nomEtudiant(etudiantId),
                parSession ? !mesPresences.isEmpty() : null,
                parSession ? premiere == null ? null : premiere.getSource().name() : null,
                parSession ? mesExercices.isEmpty() ? null : mesExercices.get(0).getStatut().name() : null,
                mesPresences.size(),
                presencesScope.stream().map(Presence::getSessionId).distinct().count(),
                mesExercices.size(),
                notesRecues.size(),
                moyenne.isPresent() ? moyenne.getAsDouble() : null,
                relecturesScope.stream()
                        .filter(r -> r.getRelecteurId().equals(etudiantId))
                        .filter(r -> r.getStatut() == Relecture.Statut.EN_ATTENTE)
                        .count());
    }

    private Totaux totauxDe(boolean parSession, List<SuiviLigne> lignes, List<Exercice> exercicesScope,
                            List<Relecture> relecturesScope, int nbSessions) {
        List<Double> toutesNotes = relecturesScope.stream()
                .filter(r -> r.getStatut() == Relecture.Statut.RENDUE && r.getNote() != null)
                .map(r -> r.getNote().doubleValue())
                .toList();
        OptionalDouble moyennePromo = toutesNotes.stream().mapToDouble(Double::doubleValue).average();
        return new Totaux(
                lignes.size(),
                parSession ? lignes.stream().filter(l -> Boolean.TRUE.equals(l.presentSession())).count() : null,
                nbSessions,
                exercicesScope.size(),
                relecturesScope.stream().filter(r -> r.getStatut() == Relecture.Statut.EN_ATTENTE).count(),
                moyennePromo.isPresent() ? moyennePromo.getAsDouble() : null);
    }

    private String nomEtudiant(Long etudiantId) {
        Optional<com.kfokam48.presence55.domain.Etudiant> e = etudiants.findById(etudiantId);
        return e.map(et -> et.getPrenom() + " " + et.getNom()).orElse("?");
    }
}
