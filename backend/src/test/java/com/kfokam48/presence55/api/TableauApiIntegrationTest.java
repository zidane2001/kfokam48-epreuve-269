package com.kfokam48.presence55.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** B6 : integration GET /api/tableau (EF7, F3, Q16, RG7). */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TableauApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private long promoId, aliceId, bobId, sessionId, exerciceAlice, exerciceBob;

    @BeforeEach
    void contexte_de_test() {
        long base = System.nanoTime() % 1_000_000L;
        promoId = 30_000_000L + base;
        aliceId = promoId + 1;
        bobId = promoId + 2;
        sessionId = promoId + 3;
        exerciceAlice = promoId + 4;
        exerciceBob = promoId + 5;

        jdbc.update("insert into promotion (id, nom) values (?, 'Promo tableau')", promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Alice', 'Dubois', ?)", aliceId, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Bob', 'Martin', ?)", bobId, promoId);
        jdbc.update("insert into session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee) " +
                "values (?, 'S1', ?, 'TB2345', ?, ?, false)",
                sessionId, promoId, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));
        jdbc.update("insert into presence (session_id, etudiant_id, source, enregistree_at) values (?, ?, 'ETUDIANT', now())", sessionId, aliceId);
        // Bob absent
        jdbc.update("insert into exercice (id, session_id, etudiant_id, lien, statut, depose_at) " +
                "values (?, ?, ?, 'https://a.b/alice', 'RELU', now())", exerciceAlice, sessionId, aliceId);
        jdbc.update("insert into exercice (id, session_id, etudiant_id, lien, statut, depose_at) " +
                "values (?, ?, ?, 'https://a.b/bob', 'EN_ATTENTE_RELECTEUR', now())", exerciceBob, sessionId, bobId);
        // Alice a recu un 15 et un 13 -> moyenne 14 ; Bob doit encore relire l'exercice d'Alice ? non :
        // Bob relect l'exercice d'Alice (rendu) ; Alice doit relire celui de Bob (en attente)
        jdbc.update("insert into relecture (id, exercice_id, relecteur_id, note, commentaire, rendue_at, attribuee_at, statut) " +
                "values (?, ?, ?, 15, 'bien', now(), now(), 'RENDUE')", exerciceBob + 1, exerciceAlice, bobId);
        jdbc.update("insert into relecture (id, exercice_id, relecteur_id, attribuee_at, statut) " +
                "values (?, ?, ?, now(), 'EN_ATTENTE')", exerciceBob + 2, exerciceBob, aliceId);
    }

    @Test
    void tableau_avec_moyenne_calculee_par_api_et_attentes_visibles() throws Exception {
        mvc.perform(get("/api/tableau").param("promotionId", String.valueOf(promoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].nom").value("Alice Dubois"))
                .andExpect(jsonPath("$[0].presences").value(1))
                .andExpect(jsonPath("$[0].exercicesDeposes").value(1))
                .andExpect(jsonPath("$[0].moyenne").value(15.0))
                .andExpect(jsonPath("$[0].relecturesEnAttente").value(1))  // Alice doit relire Bob
                .andExpect(jsonPath("$[1].nom").value("Bob Martin"))
                .andExpect(jsonPath("$[1].presences").value(0))
                .andExpect(jsonPath("$[1].relecturesEnAttente").value(0));
    }

    @Test
    void moyenne_null_quand_aucune_note_recue() throws Exception {
        // CDC v2 7.9 : Bob n'a recu aucune note -> moyenne absente (null), pas 0
        mvc.perform(get("/api/tableau").param("promotionId", String.valueOf(promoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].nom").value("Bob Martin"))
                .andExpect(jsonPath("$[1].moyenne").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void promotion_inconnue_404() throws Exception {
        mvc.perform(get("/api/tableau").param("promotionId", "999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
