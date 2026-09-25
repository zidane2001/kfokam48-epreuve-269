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

import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Issue #25 : chaque exercice est relu par DEUX pairs ; note = moyenne des deux ;
 *  une seule rendue -> note PROVISOIRE. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DeuxRelecteursIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private long promoId, sId, e1, e2, e3, e4;
    private String code;

    @BeforeEach
    void session_avec_4_presents() throws Exception {
        long base = ThreadLocalRandom.current().nextLong(0, 900_000L);
        promoId = 70_000_000L + base;
        e1 = promoId + 1; e2 = promoId + 2; e3 = promoId + 3; e4 = promoId + 4;

        jdbc.update("insert into promotion (id, nom) values (?, 'Promo 2 relecteurs')", promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Alice', 'A', ?)", e1, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Bruno', 'B', ?)", e2, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Chloe', 'C', ?)", e3, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Dave', 'D', ?)", e4, promoId);

        String body = mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Cours 2 relecteurs\",\"promotionId\":" + promoId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        sId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));
        code = body.replaceAll(".*\"code\":\"([^\"]+)\".*", "$1");
        for (long e : new long[]{e1, e2, e3, e4}) {
            mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"" + code + "\",\"etudiantId\":" + e + "}"))
                    .andExpect(status().isCreated());
        }
    }

    @Test
    void depot_assigne_deux_relecteurs_distincts_jamais_le_deposant() throws Exception {
        String body = mvc.perform(post("/api/sessions/" + sId + "/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/exo\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("RELECTEUR_ATTRIBUE"))
                .andReturn().getResponse().getContentAsString();
        long exerciceId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));

        var relectures = jdbc.queryForList(
                "select relecteur_id from relecture where exercice_id = ?", Long.class, exerciceId);
        org.assertj.core.api.Assertions.assertThat(relectures).hasSize(2);
        org.assertj.core.api.Assertions.assertThat(relectures).doesNotContain(e1);   // RG10
        org.assertj.core.api.Assertions.assertThat(relectures.get(0))
                .isNotEqualTo(relectures.get(1));                                    // deux pairs differents
    }

    @Test
    void une_seule_relecture_rendue_note_provisoire_exercice_pas_RELU() throws Exception {
        String body = mvc.perform(post("/api/sessions/" + sId + "/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/exo\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long exerciceId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));

        var relecteurs = jdbc.queryForList(
                "select relecteur_id from relecture where exercice_id = ?", Long.class, exerciceId);
        long premier = relecteurs.get(0);
        long relectureId = jdbc.queryForObject(
                "select id from relecture where exercice_id = ? and relecteur_id = ?", Long.class, exerciceId, premier);

        // le premier relecteur rend sa note
        mvc.perform(put("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relecteurId\":" + premier + ",\"note\":14,\"commentaire\":\"bien\"}"))
                .andExpect(status().isOk());

        // vue auteur : note 14 affichee mais PROVISOIRE, exercice pas encore RELU
        mvc.perform(get("/api/etudiants/" + e1 + "/exercices"))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].note").value(14))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].noteProvisoire").value(true))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].statut").value("RELECTEUR_ATTRIBUE"));
    }

    @Test
    void deux_relectures_rendues_moyenne_et_plus_provisoire() throws Exception {
        String body = mvc.perform(post("/api/sessions/" + sId + "/exercices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/exo\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long exerciceId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));

        var lignes = jdbc.queryForList(
                "select id from relecture where exercice_id = ? order by id", Long.class, exerciceId);
        // chaque relecteur rend sa note : 12 et 18 -> moyenne 15
        int i = 0;
        for (Long rl : lignes) {
            Long relecteur = jdbc.queryForObject(
                    "select relecteur_id from relecture where id = ?", Long.class, rl);
            int note = (i++ == 0) ? 12 : 18;
            mvc.perform(put("/api/relectures/" + rl).contentType(MediaType.APPLICATION_JSON)
                            .content("{\"relecteurId\":" + relecteur + ",\"note\":" + note + ",\"commentaire\":\"ok\"}"))
                    .andExpect(status().isOk());
        }

        // vue auteur : moyenne 15, plus provisoire, exercice RELU
        mvc.perform(get("/api/etudiants/" + e1 + "/exercices"))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].note").value(15))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].noteProvisoire").value(false))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].statut").value("RELU"))
                .andExpect(jsonPath("$[?(@.id==" + exerciceId + ")].resultatDefinitif").value(false));
    }
}
