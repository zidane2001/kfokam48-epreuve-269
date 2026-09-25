package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Compte;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.CompteRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

/** Login + verification BCrypt (evolution PO). */
@Service
public class AuthService {

    private final CompteRepository comptes;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(CompteRepository comptes) {
        this.comptes = comptes;
    }

    public BCryptPasswordEncoder encoder() {
        return encoder;
    }

    /** Verifie login/mot de passe et renvoie le compte ; sinon 401 IDENTIFIANTS_INVALIDES. */
    public Compte authentifier(String login, String motDePasse) {
        if (login == null || login.isBlank() || motDePasse == null || motDePasse.isBlank()) {
            throw new BusinessException("CHAMP_MANQUANT", "Login et mot de passe obligatoires.");
        }
        Compte compte = comptes.findByLogin(login.trim().toLowerCase())
                .orElseThrow(() -> new BusinessException("IDENTIFIANTS_INVALIDES",
                        "Login ou mot de passe incorrect."));
        if (!encoder.matches(motDePasse, compte.getMotDePasse())) {
            throw new BusinessException("IDENTIFIANTS_INVALIDES", "Login ou mot de passe incorrect.");
        }
        return compte;
    }
}
