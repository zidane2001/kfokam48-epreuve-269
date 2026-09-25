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

/** Evolution PO : avec auth.requis=true, l'API exige une session et le bon role. */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "auth.requis=true",
        "auth.secret=test-secret-minimum-32-caracteres-long!",
        "auth.duree-secondes=3600"
})
class AuthIntegrationTest {

    @Autowired private MockMvc mvc;

    private String login(String login, String mdp) throws Exception {
        return mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"" + login + "\",\"motDePasse\":\"" + mdp + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.role").exists())
                .andReturn().getResponse().getContentAsString();
    }

    private String tokenDe(String corps) {
        return com.jayway.jsonpath.JsonPath.read(corps, "$.token");
    }

    @Test
    void login_invalide_401_format_erreur() throws Exception {
        // Comptes de demo crees au demarrage ; mauvais mot de passe -> 401 IDENTIFIANTS_INVALIDES
        mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"login\":\"formateur\",\"motDePasse\":\"mauvais\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("IDENTIFIANTS_INVALIDES"));
    }

    @Test
    void endpoint_protege_sans_token_401() throws Exception {
        // POST /api/sessions n'est pas public : sans session -> 401 par le filtre
        mvc.perform(post("/api/sessions")
                        .contentType("application/json")
                        .content("{\"titre\":\"T\",\"promotionId\":1}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("NON_AUTHENTIFIE"));
    }

    @Test
    void formateur_peut_ouvrir_et_cloturer_mais_pas_un_etudiant() throws Exception {
        String respFormateur = login("formateur", "formateur");
        String tokenFormateur = tokenDe(respFormateur);

        // Le formateur ouvre une session
        mvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenFormateur)
                        .contentType("application/json")
                        .content("{\"titre\":\"Session auth\",\"promotionId\":1}"))
                .andExpect(status().isCreated());

        // Un etudiant connecte ne peut PAS ouvrir de session (403 ACCES_REFUSE)
        String respEtudiant = login("yannick.tchoupo", "yannick.tchoupo");
        String tokenEtudiant = tokenDe(respEtudiant);
        mvc.perform(post("/api/sessions")
                        .header("Authorization", "Bearer " + tokenEtudiant)
                        .contentType("application/json")
                        .content("{\"titre\":\"Session pirate\",\"promotionId\":1}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCES_REFUSE"));
    }

    @Test
    void etudiant_ne_voit_pas_le_tableau_du_formateur() throws Exception {
        String respEtudiant = login("yannick.tchoupo", "yannick.tchoupo");
        String tokenEtudiant = tokenDe(respEtudiant);

        mvc.perform(get("/api/tableau").param("promotionId", "1")
                        .header("Authorization", "Bearer " + tokenEtudiant))
                .andExpect(status().isForbidden());

        String respFormateur = login("formateur", "formateur");
        mvc.perform(get("/api/tableau").param("promotionId", "1")
                        .header("Authorization", "Bearer " + tokenDe(respFormateur)))
                .andExpect(status().isOk());
    }

    @Test
    void me_renvoie_l_identite_du_token() throws Exception {
        String resp = login("formateur", "formateur");
        String token = tokenDe(resp);
        mvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.login").value("formateur"))
                .andExpect(jsonPath("$.role").value("FORMATEUR"));
    }
}
