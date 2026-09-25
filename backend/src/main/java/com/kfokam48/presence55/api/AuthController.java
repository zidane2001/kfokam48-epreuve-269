package com.kfokam48.presence55.api;

import com.kfokam48.presence55.domain.Compte;
import com.kfokam48.presence55.repository.EtudiantRepository;
import com.kfokam48.presence55.service.AuthService;
import com.kfokam48.presence55.service.TokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

/** POST /api/auth/login — echange login/mot de passe contre un JWT (evolution PO). */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    public record LoginRequest(String login, String motDePasse) { }
    public record LoginResponse(String token, String role, Long etudiantId, Long promotionId, String login) { }
    public record MeResponse(String login, String role, Long etudiantId, Long promotionId) { }

    private final AuthService auth;
    private final TokenService tokens;
    private final EtudiantRepository etudiants;

    public AuthController(AuthService auth, TokenService tokens, EtudiantRepository etudiants) {
        this.auth = auth;
        this.tokens = tokens;
        this.etudiants = etudiants;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest req) {
        Compte compte = auth.authentifier(req.login(), req.motDePasse());
        return new LoginResponse(tokens.emettre(compte),
                compte.getRole().name(), compte.getEtudiantId(),
                promotionDe(compte), compte.getLogin());
    }

    /** GET /api/auth/me — verifie le token et renvoie l'identite courante. */
    @GetMapping("/me")
    public MeResponse me(@RequestAttribute(name = AuthFilter.ATTRIBUT_COMPTE, required = false) Compte compte) {
        if (compte == null) {
            throw new com.kfokam48.presence55.exception.BusinessException("NON_AUTHENTIFIE",
                    "Connexion requise.");
        }
        return new MeResponse(compte.getLogin(), compte.getRole().name(),
                compte.getEtudiantId(), promotionDe(compte));
    }

    /** GET /api/auth/mode — indique si l'authentification est active (evolution PO). */
    @GetMapping("/mode")
    public ModeResponse mode(@Value("${auth.requis:true}") boolean requis) {
        return new ModeResponse(requis);
    }

    public record ModeResponse(boolean authRequise) { }

    private Long promotionDe(Compte compte) {
        return compte.getEtudiantId() == null ? null
                : etudiants.findById(compte.getEtudiantId())
                        .map(e -> e.getPromotionId()).orElse(null);
    }
}
