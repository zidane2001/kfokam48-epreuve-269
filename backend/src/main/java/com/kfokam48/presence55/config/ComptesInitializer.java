package com.kfokam48.presence55.config;

import com.kfokam48.presence55.domain.Compte;
import com.kfokam48.presence55.repository.CompteRepository;
import com.kfokam48.presence55.repository.EtudiantRepository;
import com.kfokam48.presence55.service.AuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Evolution PO : cree les comptes de connexion si absents.
 * 1 compte par etudiant (login prenom.nom, mot de passe initial prenom.nom)
 * + un formateur de demo. Idempotent, ne reinitialise jamais un mot de passe existant.
 */
@Component
@Order(2) // apres DemoDataLoader (etudiants crees en @Order(1) implicite)
public class ComptesInitializer implements CommandLineRunner {

    private final CompteRepository comptes;
    private final EtudiantRepository etudiants;
    private final AuthService auth;

    public ComptesInitializer(CompteRepository comptes, EtudiantRepository etudiants, AuthService auth) {
        this.comptes = comptes;
        this.etudiants = etudiants;
        this.auth = auth;
    }

    @Override
    public void run(String... args) {
        if (comptes.findByLogin("formateur").isEmpty()) {
            comptes.save(new Compte("formateur",
                    auth.encoder().encode("formateur"), Compte.Role.FORMATEUR, null));
        }
        etudiants.findAll().forEach(e -> {
            String login = (e.getPrenom() + "." + e.getNom()).toLowerCase();
            String normalise = login.replace(" ", "-").replace("'", "");
            if (comptes.findByLogin(normalise).isEmpty()) {
                comptes.save(new Compte(normalise,
                        auth.encoder().encode(normalise), Compte.Role.ETUDIANT, e.getId()));
            }
        });
    }
}
