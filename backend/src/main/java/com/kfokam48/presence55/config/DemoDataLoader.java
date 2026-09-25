package com.kfokam48.presence55.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * RNF4 : le correcteur doit ouvrir une application avec du contenu.
 * Charge une promotion et 8 etudiants de demonstration si la base est vide.
 * Idempotent : ne fait rien si une promotion existe deja.
 */
@Component
public class DemoDataLoader implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    public DemoDataLoader(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void run(String... args) {
        Long nb = jdbc.queryForObject("select count(*) from promotion", Long.class);
        if (nb != null && nb > 0) {
            return;
        }
        jdbc.update("insert into promotion (nom) values (?)", "KFOKAM48 - Promotion 2026");
        Long promotionId = jdbc.queryForObject("select id from promotion limit 1", Long.class);

        String[] noms = {
                "Yannick Tchoupo", "Marie Ngo Bell", "Junior Onana", "Serge Etoundi",
                "Aline Mballa", "Christian Ngassa", "Josiane Fofack", "Patrick Ekema"
        };
        for (String nom : noms) {
            jdbc.update("insert into etudiant (nom, promotion_id) values (?, ?)", nom, promotionId);
        }
    }
}
