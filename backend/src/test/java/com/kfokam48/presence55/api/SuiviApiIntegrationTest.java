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

/** Vue riche GET /api/suivi (design, F3) — verrouille le cas 2 relectures rendues sur un exercice. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SuiviApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private long promoId, aliceId, bobId, carlaId, sessionId, exAlice, exBob;

    @BeforeEach
    void contexte_de_test() {
        long base = System.nanoTime() % 1_000_000L;
        promoId = 40_000_000L + base;
        aliceId = promoId + 1;
        bobId = promoId + 2;
        carlaId = promoId + 3;
        sessionId = promoId + 4;
        exAlice = promoId + 5;
        exBob = promoId + 6;

        jdbc.update("insert into promotion (id, nom) values (?, 'Promo suivi')", promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Alice', 'Dubois', ?)", aliceId, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Bob', 'Martin', ?)", bobId, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Carla', 'Ngoma', ?)", carlaId, promoId);
        jdbc.update("insert into session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee) " +
                "values (?, 'S1', ?, 'TS1234', ?, ?, false)",
                sessionId, promoId, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));
        // Alice presente, Bob et Carla absents
        jdbc.update("insert into presence (session_id, etudiant_id, source, enregistree_at) values (?, ?, 'ETUDIANT', now())", sessionId, aliceId);
        // Alice depose (RELU), Bob depose (EN_ATTENTE_RELECTEUR)
        jdbc.update("insert into exercice (id, session_id, etudiant_id, lien, statut, depose_at) " +
                "values (?, ?, ?, 'https://a.b/alice', 'RELU', now())", exAlice, sessionId, aliceId);
        jdbc.update("insert into exercice (id, session_id, etudiant_id, lien, statut, depose_at) " +
                "values (?, ?, ?, 'https://a.b/bob', 'EN_ATTENTE_RELECTEUR', now())", exBob, sessionId, bobId);
        // Issue #25 : DEUX relecteurs rendus sur l'exercice d'Alice (15 et 13) — moyenne 14
        jdbc.update("insert into relecture (id, exercice_id, relecteur_id, note, commentaire, rendue_at, attribuee_at, statut) " +
                "values (?, ?, ?, 15, 'bien', now(), now(), 'RENDUE')", exBob + 1, exAlice, bobId);
        jdbc.update("insert into relecture (id, exercice_id, relecteur_id, note, commentaire, rendue_at, attribuee_at, statut) " +
                "values (?, ?, ?, 13, 'correct', now(), now(), 'RENDUE')", exBob + 2, exAlice, carlaId);
        // Alice doit encore relire l'exercice de Bob
        jdbc.update("insert into relecture (id, exercice_id, relecteur_id, attribuee_at, statut) " +
                "values (?, ?, ?, now(), 'EN_ATTENTE')", exBob + 3, exBob, aliceId);
    }

    @Test
    void deux_relectures_rendues_moyenne_14_et_totaux_cohérents() throws Exception {
        mvc.perform(get("/api/suivi").param("promotionId", String.valueOf(promoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.promotionId").value(promoId))
                .andExpect(jsonPath("$.sessionId").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.lignes.length()").value(3))
                // Alice : 2 notes recues (15+13) -> moyenne 14 calculee serveur (F3)
                .andExpect(jsonPath("$.lignes[0].nomComplet").value("Alice Dubois"))
                .andExpect(jsonPath("$.lignes[0].notesRecues").value(2))
                .andExpect(jsonPath("$.lignes[0].moyenne").value(14.0))
                .andExpect(jsonPath("$.lignes[0].relecturesEnAttente").value(1))
                // Bob : aucune note recue -> null (7.9) ; presentSession null hors perimetre mono-session (mock)
                .andExpect(jsonPath("$.lignes[1].moyenne").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.lignes[1].presentSession").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.lignes[1].statutExercice").value(org.hamcrest.Matchers.nullValue()))
                // Totaux : moyenne promotion (15+13)/2 = 14, 1 relecture en attente (celle d'Alice sur Bob)
                // presents null hors perimetre mono-session (mock)
                .andExpect(jsonPath("$.totaux.etudiants").value(3))
                .andExpect(jsonPath("$.totaux.presents").value(org.hamcrest.Matchers.nullValue()))
                .andExpect(jsonPath("$.totaux.sessionsComptees").value(1))
                .andExpect(jsonPath("$.totaux.exercicesDeposes").value(2))
                .andExpect(jsonPath("$.totaux.relecturesEnAttente").value(1))
                .andExpect(jsonPath("$.totaux.moyennePromotion").value(14.0));
    }

    @Test
    void perimetre_mono_session_renvoie_present_et_source() throws Exception {
        mvc.perform(get("/api/suivi")
                        .param("promotionId", String.valueOf(promoId))
                        .param("sessionId", String.valueOf(sessionId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value(sessionId))
                .andExpect(jsonPath("$.lignes[0].presentSession").value(true))
                .andExpect(jsonPath("$.lignes[0].sourcePresence").value("ETUDIANT"))
                .andExpect(jsonPath("$.lignes[0].statutExercice").value("RELU"))
                .andExpect(jsonPath("$.totaux.presents").value(1));
    }

    @Test
    void session_hors_promotion_400_et_session_inconnue_404() throws Exception {
        long autrePromo = promoId + 90;
        jdbc.update("insert into promotion (id, nom) values (?, 'Autre promo')", autrePromo);
        long autreSession = promoId + 91;
        jdbc.update("insert into session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee) " +
                "values (?, 'Hors promo', ?, 'TX9999', ?, ?, false)",
                autreSession, autrePromo, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));

        mvc.perform(get("/api/suivi")
                        .param("promotionId", String.valueOf(promoId))
                        .param("sessionId", String.valueOf(autreSession)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("SESSION_HORS_PROMOTION"));

        mvc.perform(get("/api/suivi")
                        .param("promotionId", String.valueOf(promoId))
                        .param("sessionId", "999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }

    @Test
    void promotion_inconnue_404() throws Exception {
        mvc.perform(get("/api/suivi").param("promotionId", "999999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
