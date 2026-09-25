package com.kfokam48.presence55.api;

import com.kfokam48.presence55.domain.Compte;
import com.kfokam48.presence55.exception.BusinessException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Contrôle d'accès (evolution PO) : role obligatoire, identite forcée par le token.
 *
 * auth.requis (defaut true, env AUTH_REQUIS) :
 *  - true  : tous les gardes s'appliquent (connexion obligatoire, bon role, bonne identite) ;
 *  - false : mode contrat du sujet (demo/correction) — les gardes sont neutres, l'API
 *            reste utilisable sans compte, exactement comme dans le perimetre initial.
 */
@Component
public class AccessGuard {

    public record Identite(Compte compte, String role, Long etudiantId) { }

    // NB : requete non authentifiee en mode PO -> 401 (NON_AUTHENTIFIE) ;
    // requete authentifiee mais mauvais role -> 403 (ACCES_REFUSE).
    private final boolean requis;

    public AccessGuard(@Value("${auth.requis:true}") boolean requis) {
        this.requis = requis;
    }

    public boolean estRequis() {
        return requis;
    }

    public Identite identite(HttpServletRequest request) {
        Compte compte = (Compte) request.getAttribute(AuthFilter.ATTRIBUT_COMPTE);
        if (compte == null) {
            if (!requis) {
                return new Identite(null, "ANONYME", null);
            }
            throw new BusinessException("NON_AUTHENTIFIE", "Connexion requise.");
        }
        String role = (String) request.getAttribute(AuthFilter.ATTRIBUT_ROLE);
        Long etudiantId = (Long) request.getAttribute(AuthFilter.ATTRIBUT_ETUDIANT);
        return new Identite(compte, role, etudiantId);
    }

    public Identite exigerFormateur(HttpServletRequest request) {
        Identite id = identite(request);
        if (!requis) {
            return id;
        }
        if (id.compte() == null) {
            throw new BusinessException("NON_AUTHENTIFIE", "Connexion requise.");
        }
        if (!"FORMATEUR".equals(id.role())) {
            throw new BusinessException("ACCES_REFUSE", "Action reservee au formateur.");
        }
        return id;
    }

    public Identite exigerEtudiant(HttpServletRequest request) {
        Identite id = identite(request);
        if (!requis) {
            return id;
        }
        if (id.compte() == null) {
            throw new BusinessException("NON_AUTHENTIFIE", "Connexion requise.");
        }
        if (!"ETUDIANT".equals(id.role()) || id.etudiantId() == null) {
            throw new BusinessException("ACCES_REFUSE", "Action reservee a un etudiant connecte.");
        }
        return id;
    }

    /** L'etudiant connecte ne peut agir que sur sa propre identite (403 sinon). */
    public Long exigerEtudiantLuimeme(HttpServletRequest request, Long etudiantId) {
        if (!requis) {
            return etudiantId;
        }
        Identite id = exigerEtudiant(request);
        if (!id.etudiantId().equals(etudiantId)) {
            throw new BusinessException("ACCES_REFUSE", "Vous ne pouvez agir que sur votre propre compte.");
        }
        return etudiantId;
    }

    /** Soi-meme pour un etudiant, tout acces pour le formateur. */
    public void exigerSoimemeOuFormateur(HttpServletRequest request, Long etudiantId) {
        if (!requis) {
            return;
        }
        Identite id = identite(request);
        if (id.compte() == null) {
            throw new BusinessException("NON_AUTHENTIFIE", "Connexion requise.");
        }
        if ("FORMATEUR".equals(id.role())) {
            return;
        }
        exigerEtudiantLuimeme(request, etudiantId);
    }
}
