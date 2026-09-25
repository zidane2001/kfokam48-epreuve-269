package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.SessionDtos.OuvrirSessionRequest;
import com.kfokam48.presence55.dto.SessionDtos.SessionResponse;
import com.kfokam48.presence55.service.SessionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/sessions — operation imposee du contrat (EF1). */
@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionService service;

    public SessionController(SessionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse ouvrir(@Valid @RequestBody OuvrirSessionRequest request) {
        return service.ouvrir(request);
    }
}
