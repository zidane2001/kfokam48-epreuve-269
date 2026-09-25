package com.kfokam48.presence55.api;

import com.kfokam48.presence55.repository.EtudiantRepository;
import com.kfokam48.presence55.repository.PromotionRepository;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/** GET /api/promotions — annuaire pour l'ecran de selection (Q1 : l'etudiant choisit son nom). */
@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    public record PromotionDto(Long id, String nom, List<EtudiantDto> etudiants) { }
    public record EtudiantDto(Long id, String nom) { }

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;

    public PromotionController(PromotionRepository promotions, EtudiantRepository etudiants) {
        this.promotions = promotions;
        this.etudiants = etudiants;
    }

    @GetMapping
    public List<PromotionDto> lister() {
        return promotions.findAll().stream()
                .map(p -> new PromotionDto(p.getId(), p.getNom(),
                        etudiants.findByPromotionId(p.getId()).stream()
                                .map(e -> new EtudiantDto(e.getId(), e.getNom()))
                                .toList()))
                .toList();
    }
}
