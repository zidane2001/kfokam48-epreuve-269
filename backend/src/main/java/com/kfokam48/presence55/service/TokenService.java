package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Compte;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

/** Tokens JWT HS256 signes (evolution PO). Cle fournie par env AUTH_SECRET, 12 h de validite. */
@Service
public class TokenService {

    private final SecretKey cle;
    private final long dureeSecondes;

    public TokenService(@Value("${auth.secret:${AUTH_SECRET:presence55-secret-dev-min-32-caracteres-xx}}") String secret,
                        @Value("${auth.duree-secondes:43200}") long dureeSecondes) {
        this.cle = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.dureeSecondes = dureeSecondes;
    }

    public String emettre(Compte compte) {
        Instant maintenant = Instant.now();
        return Jwts.builder()
                .subject(compte.getLogin())
                .claim("role", compte.getRole().name())
                .claim("etudiantId", compte.getEtudiantId())
                .issuedAt(Date.from(maintenant))
                .expiration(Date.from(maintenant.plusSeconds(dureeSecondes)))
                .signWith(cle)
                .compact();
    }

    /** Renvoie les claims si le token est valide et non expire, sinon empty. */
    public Optional<Claims> verifier(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(Jwts.parser().verifyWith(cle).build()
                    .parseSignedClaims(token).getPayload());
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
