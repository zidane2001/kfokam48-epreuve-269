package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.SuiviDtos.Suivi;
import com.kfokam48.presence55.service.SuiviService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Vue riche du tableau de suivi consommee par le design (totaux, presence par
 * session, statut exercice, moyenne de promotion). /api/tableau reste la
 * reponse contractuelle du sujet.
 */
@RestController
public class SuiviController {

    private final SuiviService service;
    private final AccessGuard guard;

    public SuiviController(SuiviService service, AccessGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @GetMapping("/api/suivi")
    public Suivi suivi(@RequestParam Long promotionId,
                       @RequestParam(required = false) Long sessionId,
                       HttpServletRequest request) {
        guard.exigerFormateur(request);
        return service.parPromotion(promotionId, sessionId);
    }
}
