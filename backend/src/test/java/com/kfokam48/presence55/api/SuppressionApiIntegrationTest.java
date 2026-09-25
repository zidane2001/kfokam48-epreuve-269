package com.kfokam48.presence55.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Evolution PO (closes #30) : suppression d'etudiants sans participation et de promotions vides. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "auth.requis=true",
        "auth.secret=test-secret-minimum-32-caracteres-long!",
        "auth.duree-secondes=3600"
})
class SuppressionApiIntegrationTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private String token(String login, String mdp) throws Exception {
        String corps = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"" + login + "\",\"motDePasse\":\"" + mdp + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(corps, "$.token");
    }

    private long creerPromoEtEtudiant(String tokenFormateur, String prenom, String nom) throws Exception {
        String promo = mvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + tokenFormateur)
                        .contentType("application/json")
                        .content("{\"nom\":\"Promo suppression " + System.nanoTime() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long promoId = ((Number) com.jayway.jsonpath.JsonPath.read(promo, "$.id")).longValue();

        String etu = mvc.perform(post("/api/etudiants")
                        .header("Authorization", "Bearer " + tokenFormateur)
                        .param("promotionId", String.valueOf(promoId))
                        .contentType("application/json")
                        .content("{\"prenom\":\"" + prenom + "\",\"nom\":\"" + nom + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(etu, "$.id")).longValue();
    }

    @Test
    void supprimer_etudiant_sans_participation_204_compte_detruit() throws Exception {
        String tf = token("formateur", "formateur");
        long etudiantId = creerPromoEtEtudiant(tf, "Nadia", "Supprtest");

        mvc.perform(delete("/api/etudiants/" + etudiantId)
                        .header("Authorization", "Bearer " + tf))
                .andExpect(status().isNoContent());

        // L'etudiant et son compte ont disparu
        Integer nb = jdbc.queryForObject("select count(*) from etudiant where id = ?", Integer.class, etudiantId);
        org.junit.jupiter.api.Assertions.assertEquals(0, nb);
        Integer nbComptes = jdbc.queryForObject(
                "select count(*) from compte where etudiant_id = ?", Integer.class, etudiantId);
        org.junit.jupiter.api.Assertions.assertEquals(0, nbComptes);
        // Son login ne peut plus ouvrir de session
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"nadia.supprtest\",\"motDePasse\":\"nadia.supprtest\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void supprimer_etudiant_avec_participation_409() throws Exception {
        String tf = token("formateur", "formateur");
        long etudiantId = creerPromoEtEtudiant(tf, "Omar", "Participe");

        // Il participe : une presence
        jdbc.update("insert into presence (session_id, etudiant_id, source, enregistree_at) " +
                "values (1, ?, 'ETUDIANT', now())", etudiantId);

        mvc.perform(delete("/api/etudiants/" + etudiantId)
                        .header("Authorization", "Bearer " + tf))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ETUDIANT_A_DEJA_PARTICIPE"));
    }

    @Test
    void supprimer_promotion_vide_204_puis_non_vide_409() throws Exception {
        String tf = token("formateur", "formateur");

        // Vide -> 204
        String p1 = mvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + tf)
                        .contentType("application/json")
                        .content("{\"nom\":\"Promo vide " + System.nanoTime() + "\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        long videId = ((Number) com.jayway.jsonpath.JsonPath.read(p1, "$.id")).longValue();
        mvc.perform(delete("/api/promotions/" + videId).header("Authorization", "Bearer " + tf))
                .andExpect(status().isNoContent());

        // Non vide (contient un etudiant) -> 409
        long etudiantId = creerPromoEtEtudiant(tf, "Lia", "Garde");
        // promo de Lia : retrouvons-la via son id etudiant
        Long promoId = jdbc.queryForObject("select promotion_id from etudiant where id = ?", Long.class, etudiantId);
        mvc.perform(delete("/api/promotions/" + promoId).header("Authorization", "Bearer " + tf))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("PROMOTION_NON_VIDE"));
    }

    @Test
    void suppression_reservee_au_formateur_403_et_inconnu_404() throws Exception {
        String te = token("yannick.tchoupo", "yannick.tchoupo");
        mvc.perform(delete("/api/etudiants/999999999").header("Authorization", "Bearer " + te))
                .andExpect(status().isForbidden());

        String tf = token("formateur", "formateur");
        mvc.perform(delete("/api/etudiants/999999999").header("Authorization", "Bearer " + tf))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ETUDIANT_INCONNU"));
    }
}
