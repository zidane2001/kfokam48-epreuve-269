package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Exercice;
import com.kfokam48.presence55.domain.Relecture;
import com.kfokam48.presence55.dto.TableauDtos.LigneTableau;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class TableauService {

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final PresenceRepository presences;
    private final ExerciceRepository exercices;
    private final RelectureRepository relectures;

    public TableauService(PromotionRepository promotions,
                          EtudiantRepository etudiants,
                          PresenceRepository presences,
                          ExerciceRepository exercices,
                          RelectureRepository relectures) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.presences = presences;
        this.exercices = exercices;
        this.relectures = relectures;
    }

    /**
     * EF7 : tableau du formateur, une ligne par etudiant de la promotion (Q16).
     * F3 : la moyenne est calculee ICI, le front ne recalcule jamais.
     * RG7 : les relectures non rendues apparaissent en attente.
     */
    @Transactional(readOnly = true)
    public List<LigneTableau> parPromotion(Long promotionId) {
        promotions.findById(promotionId).orElseThrow(
                () -> new BusinessException("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));

        return etudiants.findByPromotionId(promotionId).stream()
                .map(e -> new LigneTableau(
                        e.getId(),
                        e.getNom(),
                        presences.countByEtudiantId(e.getId()),
                        exercices.countByEtudiantId(e.getId()),
                        moyenne(e.getId()),
                        relecturesEnAttente(e.getId())))
                .toList();
    }

    /** CDC v2 7.9 : null si aucune note recue — 0 est une note valide, on ne l'affiche pas a tort. */
    private Double moyenne(Long etudiantId) {
        java.util.OptionalDouble moyenne = exercices.findByEtudiantId(etudiantId).stream()
                .map(Exercice::getId)
                .map(relectures::findByExerciceId)
                .flatMap(java.util.Optional::stream)
                .map(Relecture::getNote)
                .filter(java.util.Objects::nonNull)
                .mapToInt(Integer::intValue)
                .average();
        return moyenne.isPresent() ? moyenne.getAsDouble() : null;
    }

    private long relecturesEnAttente(Long etudiantId) {
        // Relectures que l'etudiant DOIT encore faire (assignees, non rendues) — Q16
        return relectures.findByRelecteurIdAndRendueAtIsNull(etudiantId).size();
    }
}
