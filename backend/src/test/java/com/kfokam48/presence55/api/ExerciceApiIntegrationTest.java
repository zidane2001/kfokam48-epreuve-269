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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** B6 : integration POST /api/exercices (EF3/EF4, RG2, RG4, RG7). Autosuffisant. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class ExerciceApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private long promoId, aliceId, bobId, sessionId;

    @BeforeEach
    void contexte_de_test() {
        // ids uniques : le contexte partage une H2 avec les autres classes de test
        // (DemoDataLoader compris), on ne depend d'aucune donnee existante.
        long base = System.nanoTime() % 1_000_000L;
        promoId = 10_000_000L + base;
        aliceId = promoId + 1;
        bobId = promoId + 2;
        sessionId = promoId + 3;

        jdbc.update("insert into promotion (id, nom) values (?, 'Promo exercice')", promoId);
        jdbc.update("insert into etudiant (id, nom, promotion_id) values (?, 'Alice', ?)", aliceId, promoId);
        jdbc.update("insert into etudiant (id, nom, promotion_id) values (?, 'Bob', ?)", bobId, promoId);
        jdbc.update("insert into session_cours (id, titre, promotion_id, code, ouverture_at, expiration_at, cloturee) " +
                "values (?, 'Session exercice', ?, 'TX2345', ?, ?, false)",
                sessionId, promoId, OffsetDateTime.now(), OffsetDateTime.now().plusMinutes(15));
        // Alice et Bob sont presents (RG4 : le relecteur est choisi parmi les presents)
        jdbc.update("insert into presence (session_id, etudiant_id, source) values (?, ?, 'ETUDIANT')", sessionId, aliceId);
        jdbc.update("insert into presence (session_id, etudiant_id, source) values (?, ?, 'ETUDIANT')", sessionId, bobId);
    }

    @Test
    void depot_accepte_avec_relecteur_parmi_les_presents_mais_pas_le_deposant() throws Exception {
        String body = mvc.perform(post("/api/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + aliceId +
                                ",\"lien\":\"https://github.com/alice/exo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE"))
                .andReturn().getResponse().getContentAsString();

        // RG2 : le relecteur assigne n'est jamais Alice (la deposante) — ici il ne peut etre que Bob
        long relecteurId = Long.parseLong(body.replaceAll(".*\"relecteurId\":([0-9]+).*", "$1"));
        assertThat(relecteurId).isEqualTo(bobId);
    }

    @Test
    void second_depot_refuse() throws Exception {
        mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + aliceId +
                        ",\"lien\":\"https://a.b/c\"}")).andExpect(status().isCreated());
        mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + aliceId +
                                ",\"lien\":\"https://a.b/d\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EXERCICE_DEJA_DEPOSE"));
    }

    @Test
    void lien_invalide_refuse() throws Exception {
        mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":" + sessionId + ",\"etudiantId\":" + aliceId +
                                ",\"lien\":\"pas-un-lien\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("LIEN_INVALIDE"));
    }

    @Test
    void session_inconnue_refusee() throws Exception {
        mvc.perform(post("/api/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"sessionId\":999999999,\"etudiantId\":" + aliceId +
                                ",\"lien\":\"https://a.b/c\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("SESSION_INCONNUE"));
    }
}
