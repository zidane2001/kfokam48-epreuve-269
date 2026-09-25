package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.SessionDtos.OuvrirSessionRequest;
import com.kfokam48.presence55.dto.SessionDtos.SessionResponse;
import com.kfokam48.presence55.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/sessions — operation imposee du contrat (EF1). Reserve au formateur (evolution PO). */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;
    private final AccessGuard guard;

    public SessionController(SessionService service, AccessGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse ouvrir(@Valid @RequestBody OuvrirSessionRequest request,
                                  HttpServletRequest httpRequest) {
        guard.exigerFormateur(httpRequest);
        return service.ouvrir(request);
    }
}
