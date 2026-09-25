package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.PresenceDtos.PresenceRequest;
import com.kfokam48.presence55.dto.PresenceDtos.PresenceResponse;
import com.kfokam48.presence55.service.PresenceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/presences — operation imposee du contrat (EF2). Evolution PO : l'etudiant
 *  connecte marque SA presence (l'etudiantId du corps est ignore au profit du token). */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;
    private final AccessGuard guard;

    public PresenceController(PresenceService service, AccessGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceResponse marquer(@Valid @RequestBody PresenceRequest request,
                                    HttpServletRequest httpRequest) {
        Long etudiantId = guard.exigerEtudiantLuimeme(httpRequest, request.etudiantId());
        PresenceRequest avecIdentite = new PresenceRequest(request.code(), etudiantId);
        return service.marquer(avecIdentite);
    }
}
