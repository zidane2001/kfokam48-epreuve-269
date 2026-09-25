package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Etudiant;
import com.kfokam48.presence55.domain.Presence;
import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.dto.PresenceDtos.PresenceRequest;
import com.kfokam48.presence55.dto.PresenceDtos.PresenceResponse;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.EtudiantRepository;
import com.kfokam48.presence55.repository.PresenceRepository;
import com.kfokam48.presence55.repository.SessionRepository;
import com.kfokam48.presence55.repository.TentativeCodeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

@Service
public class PresenceService {

    private final SessionRepository sessions;
    private final PresenceRepository presences;
    private final TentativeCodeRepository tentatives;
    private final EtudiantRepository etudiants;
    private final TentativeCodeRecorder traceurEchecs;

    public PresenceService(SessionRepository sessions,
                           PresenceRepository presences,
                           TentativeCodeRepository tentatives,
                           EtudiantRepository etudiants,
                           TentativeCodeRecorder traceurEchecs) {
        this.sessions = sessions;
        this.presences = presences;
        this.tentatives = tentatives;
        this.etudiants = etudiants;
        this.traceurEchecs = traceurEchecs;
    }

    /** EF2 : l'etudiant marque sa presence.
     *  RG3 (CDC v2) : toute erreur de code (inconnu ou expire) est tracee sur la session
     *  active de la promotion de l'etudiant (decision 7.4 : une seule session active) ;
     *  5 echecs en moins de 2 minutes -> blocage 429. */
    @Transactional
    public PresenceResponse marquer(PresenceRequest req) {
        OffsetDateTime maintenant = OffsetDateTime.now();

        Etudiant etudiant = etudiants.findById(req.etudiantId())
                .orElseThrow(() -> new BusinessException("ETUDIANT_INCONNU",
                        "Cet etudiant n'existe pas."));                  // -> 404

        SessionCours active = sessions
                .findByPromotionIdAndClotureeFalse(etudiant.getPromotionId()).orElse(null);

        // RG3 : blocage actif ?
        if (active != null && tentatives.countBySessionIdAndEtudiantIdAndEchoueAtAfter(
                active.getId(), etudiant.getId(), maintenant.minusMinutes(2)) >= 5) {
            throw new BusinessException("TROP_DE_TENTATIVES",
                    "Trop de tentatives. Reessayez dans deux minutes."); // -> 429
        }

        SessionCours session = sessions.findByCode(req.code()).orElse(null);
        if (session == null) {
            // code inconnu : echec trace en transaction independante (survit au rollback)
            if (active != null) {
                traceurEchecs.enregistrer(active.getId(), etudiant.getId(), maintenant);
            }
            throw new BusinessException("CODE_INCONNU",
                    "Ce code de presence est inconnu.");             // -> 400
        }
        if (session.codeExpire(maintenant)) {                           // RG1 -> 410
            traceurEchecs.enregistrer(session.getId(), etudiant.getId(), maintenant);
            throw new BusinessException("CODE_EXPIRE", "Le code de presence a expire.");
        }
        if (session.isCloturee()) {                                     // RG2 (Q3) -> 409
            throw new BusinessException("SESSION_CLOTUREE",
                    "Cette session est cloturee : presence impossible.");
        }
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), etudiant.getId())) {
            throw new BusinessException("DEJA_PRESENT",
                    "Vous etes deja marque present pour cette session."); // RG4 -> 409
        }
        Presence p = presences.save(
                new Presence(session.getId(), etudiant.getId(), Presence.Source.ETUDIANT));
        return new PresenceResponse(p.getId(), session.getId(), etudiant.getId(), p.getSource().name());
    }
}
