package com.kfokam48.presence55.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** B6 : test d'integration sur l'endpoint impose POST /api/sessions (EF1, RG1). */
@SpringBootTest
@AutoConfigureMockMvc
class SessionApiIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void une_promotion_existe() {
        Integer n = jdbc.queryForObject("select count(*) from promotion where id = 1", Integer.class);
        if (n != null && n == 0) {
            jdbc.update("insert into promotion (id, nom) values (1, 'Promotion de test')");
        }
    }

    @Test
    void ouvre_une_session_avec_code_expirant_a_H_plus_15() throws Exception {
        long avant = System.currentTimeMillis();
        var resultat = mvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Algo avancé\",\"promotionId\":1}"))
                .andReturn();
        if (resultat.getResponse().getStatus() != 201) {
            throw new IllegalStateException("statut=" + resultat.getResponse().getStatus()
                    + " corps=" + resultat.getResponse().getContentAsString());
        }
        String body = resultat.getResponse().getContentAsString();
        org.assertj.core.api.Assertions.assertThat(body)
                .contains("\"id\"").contains("\"code\"")
                .contains("\"ouvertureAt\"").contains("\"expirationAt\"");

        // RG1 : expirationAt - ouvertureAt = 15 minutes (tolerance reseau)
        String ouv = body.replaceAll(".*\"ouvertureAt\":\"([^\"]+)\".*", "$1");
        String exp = body.replaceAll(".*\"expirationAt\":\"([^\"]+)\".*", "$1");
        java.time.OffsetDateTime o = java.time.OffsetDateTime.parse(ouv);
        java.time.OffsetDateTime e = java.time.OffsetDateTime.parse(exp);
        org.assertj.core.api.Assertions.assertThat(Duration.between(o, e))
                .isBetween(Duration.ofMinutes(14).plusSeconds(55), Duration.ofMinutes(15).plusSeconds(5));
        org.assertj.core.api.Assertions.assertThat(System.currentTimeMillis() - avant)
                .isLessThan(60_000);
    }

    @Test
    void refuse_une_session_sans_titre() throws Exception {
        mvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"promotionId\":1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("CHAMP_MANQUANT"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void refuse_une_promotion_inconnue() throws Exception {
        mvc.perform(post("/api/sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"X\",\"promotionId\":999999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
