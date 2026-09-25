package com.kfokam48.presence55.api;

import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.dto.SessionQueryDtos.SessionDetailDto;
import com.kfokam48.presence55.dto.SessionQueryDtos.SessionListeDto;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.*;
import com.kfokam48.presence55.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

/** Lectures et cloture des sessions — alimente l'espace formateur du design (EF15). */
@RestController
public class SessionQueryController {

    private final SessionRepository sessions;
    private final PromotionRepository promotions;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final EtudiantRepository etudiants;
    private final SessionService sessionService;
    private final AccessGuard guard;

    public SessionQueryController(SessionRepository sessions, PromotionRepository promotions,
                                  PresenceRepository presences, ExerciceRepository exercices,
                                  EtudiantRepository etudiants, SessionService sessionService,
                                  AccessGuard guard) {
        this.sessions = sessions;
        this.promotions = promotions;
        this.presences = presences;
        this.exercices = exercices;
        this.etudiants = etudiants;
        this.sessionService = sessionService;
        this.guard = guard;
    }

    /** GET /api/sessions?promotionId= — liste avec statut OUVERTE/CLOTUREE. */
    @GetMapping("/api/sessions")
    public List<SessionListeDto> lister(@RequestParam(required = false) Long promotionId) {
        List<SessionCours> liste = promotionId == null
                ? sessions.findAll()
                : sessions.findByPromotionId(promotionId);
        return liste.stream().map(s -> new SessionListeDto(
                s.getId(), s.getTitre(), s.getPromotionId(),
                promotions.findById(s.getPromotionId()).map(p -> p.getNom()).orElse("?"),
                s.getCode(), s.getOuvertureAt().toString(), s.getExpirationAt().toString(),
                s.isCloturee() ? "CLOTUREE" : "OUVERTE",
                s.getClotureAt() == null ? null : s.getClotureAt().toString(),
                s.codeExpire(OffsetDateTime.now()))).toList();
    }

    /** GET /api/sessions/{id} — detail avec compteurs pour la vue formateur. */
    @GetMapping("/api/sessions/{id}")
    @Transactional(readOnly = true)
    public SessionDetailDto detail(@PathVariable Long id) {
        SessionCours s = sessions.findById(id)
                .orElseThrow(() -> new BusinessException("SESSION_INCONNUE", "Cette session n'existe pas."));
        int nbEtudiants = etudiants.findByPromotionId(s.getPromotionId()).size();
        return new SessionDetailDto(
                s.getId(), s.getTitre(), s.getPromotionId(),
                promotions.findById(s.getPromotionId()).map(p -> p.getNom()).orElse("?"),
                s.getCode(), s.getOuvertureAt().toString(), s.getExpirationAt().toString(),
                s.isCloturee() ? "CLOTUREE" : "OUVERTE",
                s.getClotureAt() == null ? null : s.getClotureAt().toString(),
                s.codeExpire(OffsetDateTime.now()),
                nbEtudiants, presences.findBySessionId(id).size(),
                (int) exercices.countBySessionId(id));
    }

    /** POST /api/sessions/{id}/cloture — EF15/RG17 : clôture definit les relectures. Formateur only. */
    @PostMapping("/api/sessions/{id}/cloture")
    public SessionDetailDto cloturer(@PathVariable Long id, HttpServletRequest request) {
        guard.exigerFormateur(request);
        SessionCours s = sessions.findById(id)
                .orElseThrow(() -> new BusinessException("SESSION_INCONNUE", "Cette session n'existe pas."));
        if (s.isCloturee()) {
            throw new BusinessException("SESSION_DEJA_CLOTUREE", "Cette session est deja cloturee.");
        }
        s.cloturer(OffsetDateTime.now());
        sessions.save(s);
        return detail(id);
    }
}
