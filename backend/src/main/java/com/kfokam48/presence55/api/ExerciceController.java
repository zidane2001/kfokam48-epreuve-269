package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.ExerciceDtos.DepotRequest;
import com.kfokam48.presence55.dto.ExerciceDtos.DepotResponse;
import com.kfokam48.presence55.service.ExerciceService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/exercices — operation imposee du contrat (EF4/EF3). Evolution PO : l'etudiant
 *  connecte depose SON exercice (l'etudiantId du corps est ignore au profit du token). */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService service;
    private final AccessGuard guard;

    public ExerciceController(ExerciceService service, AccessGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepotResponse deposer(@Valid @RequestBody DepotRequest request,
                                 HttpServletRequest httpRequest) {
        Long etudiantId = guard.exigerEtudiantLuimeme(httpRequest, request.etudiantId());
        return service.deposer(new DepotRequest(request.sessionId(), etudiantId, request.lien()));
    }
}
