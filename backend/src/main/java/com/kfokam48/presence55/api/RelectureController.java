package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.RelectureDtos.RelectureRequest;
import com.kfokam48.presence55.dto.RelectureDtos.RelectureResponse;
import com.kfokam48.presence55.service.RelectureService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** POST /api/relectures/{id} — operation imposee du contrat (EF5). */
@RestController
public class RelectureController {

    private final RelectureService service;

    public RelectureController(RelectureService service) {
        this.service = service;
    }

    @PostMapping("/api/relectures/{id}")
    public RelectureResponse rendre(@PathVariable Long id, @Valid @RequestBody RelectureRequest request) {
        return service.rendre(id, request);
    }

    /** GET /api/relectures-a-faire?relecteurId= — alimente l'ecran relecteur (F2). */
    @GetMapping("/api/relectures-a-faire")
    public java.util.List<RelectureService.RelectureAFaire> aFaire(@RequestParam Long relecteurId) {
        return service.aFaire(relecteurId);
    }
}
