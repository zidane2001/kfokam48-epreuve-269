package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.dto.SessionDtos.OuvrirSessionRequest;
import com.kfokam48.presence55.dto.SessionDtos.SessionResponse;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.PromotionRepository;
import com.kfokam48.presence55.repository.SessionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class SessionService {

    public static final Duration DUREE_CODE = Duration.ofMinutes(15); // RG1

    private final SessionRepository sessions;
    private final PromotionRepository promotions;

    public SessionService(SessionRepository sessions, PromotionRepository promotions) {
        this.sessions = sessions;
        this.promotions = promotions;
    }

    /** EF1 : le formateur ouvre une session et obtient un code expirant a H+15.
     *  CDC v2 7.4 : une seule session active (non cloturee) par promotion. */
    @Transactional
    public SessionResponse ouvrir(OuvrirSessionRequest req) {
        if (!promotions.existsById(req.promotionId())) {
            throw new BusinessException("PROMOTION_INCONNUE", "Cette promotion n'existe pas.");
        }
        if (sessions.existsByPromotionIdAndClotureeFalse(req.promotionId())) {
            throw new BusinessException("SESSION_DEJA_ACTIVE",
                    "Une session est deja active pour cette promotion. Cloturez-la d'abord.");
        }
        String code;
        do {
            code = CodePresenceGenerator.generer();
        } while (sessions.findByCode(code).isPresent());

        OffsetDateTime maintenant = OffsetDateTime.now();
        SessionCours s = new SessionCours(
                req.titre(), req.promotionId(), code,
                maintenant, maintenant.plus(DUREE_CODE));
        sessions.save(s);
        return new SessionResponse(s.getId(), s.getCode(), s.getOuvertureAt(), s.getExpirationAt());
    }

    /** Recherche par code pour les usages etudiant (tickets 2, 4). */
    @Transactional(readOnly = true)
    public SessionCours parCode(String code) {
        return sessions.findByCode(code)
                .orElseThrow(() -> new BusinessException("CODE_INCONNU", "Ce code de presence est inconnu."));
    }
}
