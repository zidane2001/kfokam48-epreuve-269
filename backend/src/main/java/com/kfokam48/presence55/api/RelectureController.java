package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.RelectureDtos.RelectureRequest;
import com.kfokam48.presence55.dto.RelectureDtos.RelectureResponse;
import com.kfokam48.presence55.repository.RelectureRepository;
import com.kfokam48.presence55.service.RelectureService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/** POST /api/relectures/{id} — operation imposee du contrat (EF5). */
@RestController
public class RelectureController {

    private final RelectureService service;
    private final RelectureRepository relectures;
    private final AccessGuard guard;

    public RelectureController(RelectureService service, RelectureRepository relectures, AccessGuard guard) {
        this.service = service;
        this.relectures = relectures;
        this.guard = guard;
    }

    /** POST /api/relectures/{id} — operation imposee (EF5). Evolution PO : seul le relecteur
     *  assigne (ou le formateur) peut agir ; l'identite vient du token, pas du corps. */
    @PostMapping("/api/relectures/{id}")
    public RelectureResponse rendre(@PathVariable Long id, @Valid @RequestBody RelectureRequest request,
                                    HttpServletRequest httpRequest) {
        relectures.findById(id).ifPresent(r -> guard.exigerSoimemeOuFormateur(httpRequest, r.getRelecteurId()));
        return service.rendre(id, request);
    }

    /** GET /api/relectures-a-faire?relecteurId= — alimente l'ecran relecteur (F2). Identite forcée. */
    @GetMapping("/api/relectures-a-faire")
    public java.util.List<RelectureService.RelectureAFaire> aFaire(@RequestParam Long relecteurId,
                                                                   HttpServletRequest httpRequest) {
        guard.exigerSoimemeOuFormateur(httpRequest, relecteurId);
        return service.aFaire(relecteurId);
    }
}
