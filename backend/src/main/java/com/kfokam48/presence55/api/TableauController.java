package com.kfokam48.presence55.api;

import com.kfokam48.presence55.dto.TableauDtos.LigneTableau;
import com.kfokam48.presence55.service.TableauService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** GET /api/tableau?promotionId= — operation imposee du contrat (EF7). Reserve au formateur. */
@RestController
public class TableauController {

    private final TableauService service;
    private final AccessGuard guard;

    public TableauController(TableauService service, AccessGuard guard) {
        this.service = service;
        this.guard = guard;
    }

    @GetMapping("/api/tableau")
    public List<LigneTableau> tableau(@RequestParam Long promotionId, HttpServletRequest request) {
        guard.exigerFormateur(request);
        return service.parPromotion(promotionId);
    }
}
