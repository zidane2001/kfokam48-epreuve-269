package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Presence;
import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.domain.TentativeCode;
import com.kfokam48.presence55.dto.PresenceDtos.PresenceRequest;
import com.kfokam48.presence55.dto.PresenceDtos.PresenceResponse;
import com.kfokam48.presence55.exception.BusinessException;
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

    public PresenceService(SessionRepository sessions,
                           PresenceRepository presences,
                           TentativeCodeRepository tentatives) {
        this.sessions = sessions;
        this.presences = presences;
        this.tentatives = tentatives;
    }

    /** EF2 : l'etudiant marque sa presence. Ordre des verifications = diagramme D3. */
    @Transactional
    public PresenceResponse marquer(PresenceRequest req) {
        OffsetDateTime maintenant = OffsetDateTime.now();

        SessionCours session = sessions.findByCode(req.code())
                .orElseThrow(() -> new BusinessException("CODE_INCONNU",
                        "Ce code de presence est inconnu."));            // -> 400

        // RG12 : 5 echecs en moins de 2 minutes sur cette session -> blocage
        if (tentatives.countBySessionIdAndEtudiantIdAndEchoueAtAfter(
                session.getId(), req.etudiantId(), maintenant.minusMinutes(2)) >= 5) {
            throw new BusinessException("TROP_DE_TENTATIVES",
                    "Trop de tentatives. Reessayez dans deux minutes."); // -> 429
        }

        if (session.codeExpire(maintenant)) {                           // RG1 -> 410
            enregistrerEchec(session.getId(), req.etudiantId(), maintenant);
            throw new BusinessException("CODE_EXPIRE", "Le code de presence a expire.");
        }
        if (session.isCloturee()) {                                     // RG2 (Q3) -> 409
            throw new BusinessException("SESSION_CLOTUREE",
                    "Cette session est cloturee : presence impossible.");
        }
        if (presences.existsBySessionIdAndEtudiantId(session.getId(), req.etudiantId())) {
            throw new BusinessException("DEJA_PRESENT",
                    "Vous etes deja marque present pour cette session."); // RG13 -> 409
        }
        Presence p = presences.save(
                new Presence(session.getId(), req.etudiantId(), Presence.Source.ETUDIANT));
        return new PresenceResponse(p.getId(), session.getId(), req.etudiantId(), p.getSource().name());
    }

    private void enregistrerEchec(Long sessionId, Long etudiantId, OffsetDateTime quand) {
        tentatives.save(new TentativeCode(sessionId, etudiantId, quand));
    }
}
