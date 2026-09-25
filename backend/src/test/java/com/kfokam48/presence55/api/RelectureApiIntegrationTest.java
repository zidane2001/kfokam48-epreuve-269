package com.kfokam48.presence55.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** B6 : integration POST /api/relectures/{id} (EF5, RG2, RG3, RG5) + ecran relecteur. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RelectureApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private long promoId, aliceId, bobId, sessionId, exerciceId, relectureId;

    @BeforeEach
    void contexte_de_test() {
        long base = System.nanoTime() % 1_000_000L;
        promoId = 20_000_000L + base;
        aliceId = promoId + 1;
        bobId = promoId + 2;
        sessionId = promoId + 3;

        jdbc.update("insert into promotion (id, nom) values (?, 'Promo relecture')", promoId);
        jdbc.update("insert into etudiant (id, nom, promotion_id) values (?, 'Alice', ?)", aliceId, promoId);
        jdbc.update("insert into etudiant (id, nom, promotion_id) values (?, 'Bob', ?)", bobId, promoId);
        jdbc.update("insert into session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee) " +
                "values (?, 'Session relecture', ?, 'RX2345', ?, ?, false)",
                sessionId, promoId, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));
        jdbc.update("insert into presence (session_id, etudiant_id, source) values (?, ?, 'ETUDIANT')", sessionId, aliceId);
        jdbc.update("insert into presence (session_id, etudiant_id, source) values (?, ?, 'ETUDIANT')", sessionId, bobId);
        // Alice depose, Bob est assigne relecteur
        jdbc.update("insert into exercice (id, session_id, etudiant_id, lien, statut) " +
                "values (?, ?, ?, 'https://a.b/exo', 'EN_ATTENTE')", sessionId + 10, sessionId, aliceId);
        exerciceId = sessionId + 10;
        jdbc.update("insert into relecture (id, exercice_id, relecteur_id) values (?, ?, ?)",
                sessionId + 11, exerciceId, bobId);
        relectureId = sessionId + 11;
    }

    @Test
    void relecture_nominee_passee_en_RELU() throws Exception {
        mvc.perform(post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":15,\"commentaire\":\"Bon travail, clarify la partie 2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(15))
                .andExpect(jsonPath("$.statutExercice").value("RELU"));
    }

    @Test
    void auto_relecture_interdite() throws Exception {
        // Bob ne peut pas relire son propre exercice : on reassigne la relecture a Alice
        // qui est la deposante -> RG2 refuse (403)
        jdbc.update("update relecture set relecteur_id = ? where id = ?", aliceId, relectureId);
        mvc.perform(post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":15,\"commentaire\":\"...\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AUTO_RELECTURE"));
    }

    @Test
    void double_relecture_refusee() throws Exception {
        jdbc.update("update relecture set note = 12, commentaire = 'ok', rendue_at = now() where id = ?", relectureId);
        mvc.perform(post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":15,\"commentaire\":\"...\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_DEJA_RENDUE"));
    }

    @Test
    void note_hors_bornes_refusee() throws Exception {
        mvc.perform(post("/api/relectures/" + relectureId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"note\":25,\"commentaire\":\"...\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("NOTE_INVALIDE"));
    }

    @Test
    void relectures_a_faire_liste_pour_bob_uniquement() throws Exception {
        mvc.perform(get("/api/relectures-a-faire").param("relecteurId", String.valueOf(bobId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].relectureId").value(relectureId))
                .andExpect(jsonPath("$[0].lien").value("https://a.b/exo"));
        mvc.perform(get("/api/relectures-a-faire").param("relecteurId", String.valueOf(aliceId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }
}
