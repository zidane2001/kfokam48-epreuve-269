package com.kfokam48.presence55.api;

import com.kfokam48.presence55.domain.Compte;
import com.kfokam48.presence55.repository.CompteRepository;
import com.kfokam48.presence55.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre d'authentification (evolution PO) : lit le header Authorization: Bearer,
 * attache le Compte aux requetes authentifiees en attribut "auth.compte".
 *
 * auth.requis=false (mode contrat du sujet, demo/correction) : le filtre attache
 * le compte si un token valide est present mais n'exige jamais rien.
 * auth.requis=true (defaut, mode PO) : tout /api/** non public exige une session.
 */
@Component
public class AuthFilter extends OncePerRequestFilter {

    public static final String ATTRIBUT_COMPTE = "auth.compte";
    public static final String ATTRIBUT_ROLE = "auth.role";
    public static final String ATTRIBUT_ETUDIANT = "auth.etudiantId";

    private static final List<String> PUBLICS = List.of(
            "/api/auth/login", "/api/auth/me", "/api/auth/mode",
            "/api/promotions", "/api/sessions",
            "/api/presences", "/api/exercices", "/api/relectures", "/api/relectures-a-faire",
            "/api/tableau", "/api/suivi");

    private final TokenService tokens;
    private final CompteRepository comptes;
    private final boolean requis;

    public AuthFilter(TokenService tokens, CompteRepository comptes,
                      @Value("${auth.requis:true}") boolean requis) {
        this.tokens = tokens;
        this.comptes = comptes;
        this.requis = requis;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String chemin = request.getRequestURI();

        // 1) Toujours laisse passer : preflight CORS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // 2) Attache le compte si un Bearer valide est present
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            java.util.Optional<Claims> claims = tokens.verifier(header.substring(7));
            if (claims.isPresent()) {
                Compte compte = comptes.findByLogin(claims.get().getSubject()).orElse(null);
                if (compte != null) {
                    request.setAttribute(ATTRIBUT_COMPTE, compte);
                    request.setAttribute(ATTRIBUT_ROLE, claims.get().get("role", String.class));
                    request.setAttribute(ATTRIBUT_ETUDIANT, claims.get().get("etudiantId", Long.class));
                }
            }
        }

        // 3) Mode contrat du sujet : rien n'est refuse par le filtre
        if (!requis) {
            chain.doFilter(request, response);
            return;
        }

        // 4) Mode PO : publics explicites OU tout chemin hors /api
        boolean estPublic = PUBLICS.stream().anyMatch(chemin::startsWith);
        if (estPublic || !chemin.startsWith("/api/")) {
            chain.doFilter(request, response);
            return;
        }

        if (request.getAttribute(ATTRIBUT_COMPTE) == null) {
            repondre401(response, "NON_AUTHENTIFIE", "Connexion requise.");
            return;
        }
        chain.doFilter(request, response);
    }

    private void repondre401(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"" + code + "\",\"message\":\"" + message + "\"}");
    }
}
