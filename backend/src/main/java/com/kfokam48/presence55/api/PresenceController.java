package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.PresenceDtos.PresenceRequest;
import com.kfokam48.presence55.dto.PresenceDtos.PresenceResponse;
import com.kfokam48.presence55.service.PresenceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/presences — operation imposee du contrat (EF2). */
@RestController
@RequestMapping("/api/presences")
public class PresenceController {

    private final PresenceService service;

    public PresenceController(PresenceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PresenceResponse marquer(@Valid @RequestBody PresenceRequest request) {
        return service.marquer(request);
    }
}
