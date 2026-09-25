package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.ExerciceDtos.DepotRequest;
import com.kfokam48.presence55.dto.ExerciceDtos.DepotResponse;
import com.kfokam48.presence55.service.ExerciceService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/** POST /api/exercices — operation imposee du contrat (EF4/EF3). */
@RestController
@RequestMapping("/api/exercices")
public class ExerciceController {

    private final ExerciceService service;

    public ExerciceController(ExerciceService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepotResponse deposer(@Valid @RequestBody DepotRequest request) {
        return service.deposer(request);
    }
}
