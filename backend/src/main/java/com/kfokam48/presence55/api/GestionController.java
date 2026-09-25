package com.kfokam48.presence55.api;

import com.kfokam48.presence55.domain.Compte;
import com.kfokam48.presence55.domain.Etudiant;
import com.kfokam48.presence55.domain.Promotion;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.CompteRepository;
import com.kfokam48.presence55.repository.EtudiantRepository;
import com.kfokam48.presence55.repository.PromotionRepository;
import com.kfokam48.presence55.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestion des promotions et des etudiants par le formateur (evolution PO, CDC 7.15,
 * closes #29). Chaque etudiant inscrit recoit automatiquement son compte de
 * connexion (login prenom.nom, mot de passe initial identique).
 */
@RestController
public class GestionController {

    public record NouvellePromotionRequest(@NotBlank(message = "nom manquant") String nom) { }
    public record NouvelEtudiantRequest(@NotBlank(message = "prenom manquant") String prenom,
                                        @NotBlank(message = "nom manquant") String nom) { }
    public record PromotionCreeeDto(Long id, String nom) { }
    public record EtudiantCreeDto(Long id, String prenom, String nom, Long promotionId, String login) { }

    private final PromotionRepository promotions;
    private final EtudiantRepository etudiants;
    private final CompteRepository comptes;
    private final AuthService auth;
    private final AccessGuard guard;

    public GestionController(PromotionRepository promotions, EtudiantRepository etudiants,
                             CompteRepository comptes, AuthService auth, AccessGuard guard) {
        this.promotions = promotions;
        this.etudiants = etudiants;
        this.comptes = comptes;
        this.auth = auth;
        this.guard = guard;
    }

    /** POST /api/promotions — le formateur cree une promotion. */
    @PostMapping("/api/promotions")
    @ResponseStatus(HttpStatus.CREATED)
    public PromotionCreeeDto creerPromotion(@Valid @RequestBody NouvellePromotionRequest req,
                                            HttpServletRequest request) {
        guard.exigerFormateur(request);
        String nom = req.nom().trim();
        if (nom.isEmpty() || nom.length() > 120) {
            throw new BusinessException("VALIDATION", "Le nom doit contenir entre 1 et 120 caracteres.");
        }
        Promotion p = promotions.save(new Promotion(nom));
        return new PromotionCreeeDto(p.getId(), p.getNom());
    }

    /** POST /api/etudiants?promotionId= — inscrit un etudiant et cree son compte. */
    @PostMapping("/api/etudiants")
    @ResponseStatus(HttpStatus.CREATED)
    public EtudiantCreeDto inscrireEtudiant(@Valid @RequestBody NouvelEtudiantRequest req,
                                            @RequestParam Long promotionId,
                                            HttpServletRequest request) {
        guard.exigerFormateur(request);
        Promotion p = promotions.findById(promotionId)
                .orElseThrow(() -> new BusinessException("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));
        String prenom = req.prenom().trim();
        String nom = req.nom().trim();
        if (prenom.isEmpty() || prenom.length() > 80 || nom.isEmpty() || nom.length() > 120) {
            throw new BusinessException("VALIDATION", "Prenom ou nom manquant ou trop long.");
        }

        Etudiant e = etudiants.save(new Etudiant(prenom, nom, p.getId()));

        String login = loginDe(prenom, nom);
        if (comptes.findByLogin(login).isPresent()) {
            login = login + "." + e.getId();                       // homonymes : login suffixe
        }
        comptes.save(new Compte(login, auth.encoder().encode(login), Compte.Role.ETUDIANT, e.getId()));

        return new EtudiantCreeDto(e.getId(), e.getPrenom(), e.getNom(), e.getPromotionId(), login);
    }

    /** GET /api/promotions/{id}/etudiants — liste pour l'ecran de gestion. */
    @GetMapping("/api/promotions/{id}/etudiants")
    public List<EtudiantCreeDto> etudiantsDe(@PathVariable Long id, HttpServletRequest request) {
        guard.exigerFormateur(request);
        promotions.findById(id)
                .orElseThrow(() -> new BusinessException("PROMOTION_INCONNUE", "Cette promotion n'existe pas."));
        return etudiants.findByPromotionId(id).stream()
                .map(e -> new EtudiantCreeDto(e.getId(), e.getPrenom(), e.getNom(), e.getPromotionId(),
                        loginDe(e.getPrenom(), e.getNom())))
                .toList();
    }

    static String loginDe(String prenom, String nom) {
        return (prenom + "." + nom).toLowerCase().replace(" ", "-").replace("'", "");
    }
}
