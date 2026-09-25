package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.TableauDtos.LigneTableau;
import com.kfokam48.presence55.service.TableauService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** GET /api/tableau?promotionId= — operation imposee du contrat (EF7). */
@RestController
public class TableauController {

    private final TableauService service;

    public TableauController(TableauService service) {
        this.service = service;
    }

    @GetMapping("/api/tableau")
    public List<LigneTableau> tableau(@RequestParam Long promotionId) {
        return service.parPromotion(promotionId);
    }
}
