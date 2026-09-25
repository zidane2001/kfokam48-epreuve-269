package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Exercice;
import com.kfokam48.presence55.domain.Relecture;
import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.dto.RelectureDtos.RelectureRequest;
import com.kfokam48.presence55.dto.RelectureDtos.RelectureResponse;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.ExerciceRepository;
import com.kfokam48.presence55.repository.RelectureRepository;
import com.kfokam48.presence55.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class RelectureService {

    private final RelectureRepository relectures;
    private final ExerciceRepository exercices;
    private final SessionRepository sessions;

    public RelectureService(RelectureRepository relectures,
                            ExerciceRepository exercices,
                            SessionRepository sessions) {
        this.relectures = relectures;
        this.exercices = exercices;
        this.sessions = sessions;
    }

    /**
     * EF5 : le relecteur rend sa note et son commentaire.
     * 404 inconnue, 403 AUTO_RELECTURE (RG2), 409 RELECTURE_DEJA_RENDUE (RG3),
     * 400 NOTE_INVALIDE (RG5 : entier 0-20, verifie ici et en CHECK en base).
     */
    @Transactional
    public RelectureResponse rendre(Long relectureId, RelectureRequest req) {
        Relecture r = relectures.findById(relectureId)
                .orElseThrow(() -> new BusinessException("RELECTURE_INCONNUE",
                        "Cette relecture n'existe pas."));

        Exercice ex = exercices.findById(r.getExerciceId())
                .orElseThrow(() -> new BusinessException("EXERCICE_INCONNU",
                        "L'exercice relu n'existe plus."));

        if (r.getRelecteurId().equals(ex.getEtudiantId())) {             // RG2 -> 403
            throw new BusinessException("AUTO_RELECTURE",
                    "Personne ne relit son propre exercice.");
        }
        if (r.getRendueAt() != null) {                                   // RG3 -> 409
            throw new BusinessException("RELECTURE_DEJA_RENDUE",
                    "Cette relecture a deja ete rendue.");
        }
        if (req.note() == null || req.note() < 0 || req.note() > 20) {   // RG5 -> 400
            throw new BusinessException("NOTE_INVALIDE",
                    "La note doit etre un entier entre 0 et 20.");
        }

        r.rendre(req.note(), req.commentaire(), OffsetDateTime.now());
        ex.setStatut(Exercice.Statut.RELU);                              // D4 : EN_ATTENTE -> RELU
        relectures.save(r);
        exercices.save(ex);

        return new RelectureResponse(r.getId(), ex.getId(), r.getNote(),
                r.getCommentaire(), ex.getStatut().name());
    }

    /** EF16 : relectures assignees non rendues, pour l'ecran relecteur. */
    @Transactional(readOnly = true)
    public List<RelectureAFaire> aFaire(Long relecteurId) {
        return relectures.findByRelecteurIdAndRendueAtIsNull(relecteurId).stream()
                .map(r -> {
                    Exercice ex = exercices.findById(r.getExerciceId())
                            .orElseThrow(() -> new BusinessException("EXERCICE_INCONNU",
                                    "L'exercice relu n'existe plus."));
                    return new RelectureAFaire(r.getId(), ex.getSessionId(),
                            ex.getEtudiantId(), ex.getLien());
                })
                .toList();
    }

    public record RelectureAFaire(Long relectureId, Long sessionId, Long depositaireId, String lien) { }
}
