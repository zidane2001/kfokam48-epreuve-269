package com.kfokam48.presence55.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Evolution PO (CDC 7.15, closes #29) : gestion promotions/etudiants par le formateur. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "auth.requis=true",
        "auth.secret=test-secret-minimum-32-caracteres-long!",
        "auth.duree-secondes=3600"
})
class GestionApiIntegrationTest {

    @Autowired private MockMvc mvc;

    private String token(String login, String mdp) throws Exception {
        String corps = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"" + login + "\",\"motDePasse\":\"" + mdp + "\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(corps, "$.token");
    }

    @Test
    void formateur_cree_promotion_puis_inscrit_un_etudiant_avec_compte() throws Exception {
        String tf = token("formateur", "formateur");

        String promo = mvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + tf)
                        .contentType("application/json")
                        .content("{\"nom\":\"Promo gestion " + System.nanoTime() + "\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andReturn().getResponse().getContentAsString();
        Long promoId = ((Number) com.jayway.jsonpath.JsonPath.read(promo, "$.id")).longValue();

        mvc.perform(post("/api/etudiants")
                        .header("Authorization", "Bearer " + tf)
                        .param("promotionId", String.valueOf(promoId))
                        .contentType("application/json")
                        .content("{\"prenom\":\"Diane\",\"nom\":\"Testgestion\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.login").value("diane.testgestion"));

        // Le compte cree fonctionne des l'ouverture de session
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"diane.testgestion\",\"motDePasse\":\"diane.testgestion\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ETUDIANT"));

        // Liste des etudiants de la promotion
        mvc.perform(get("/api/promotions/" + promoId + "/etudiants")
                        .header("Authorization", "Bearer " + tf))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].prenom").value("Diane"));
    }

    @Test
    void etudiant_ne_peut_pas_creer_de_promotion() throws Exception {
        String te = token("yannick.tchoupo", "yannick.tchoupo");
        mvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + te)
                        .contentType("application/json")
                        .content("{\"nom\":\"Promo pirate\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void nom_manquant_400_et_promotion_inconnue_404() throws Exception {
        String tf = token("formateur", "formateur");

        mvc.perform(post("/api/promotions")
                        .header("Authorization", "Bearer " + tf)
                        .contentType("application/json")
                        .content("{\"nom\":\"\"}"))
                .andExpect(status().isBadRequest());

        mvc.perform(post("/api/etudiants")
                        .header("Authorization", "Bearer " + tf)
                        .param("promotionId", "999999999")
                        .contentType("application/json")
                        .content("{\"prenom\":\"X\",\"nom\":\"Y\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PROMOTION_INCONNUE"));
    }
}
