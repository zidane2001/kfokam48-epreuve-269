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
import java.util.concurrent.ThreadLocalRandom;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Parcours utilisateurs du CDC v2, de bout en bout, un test par user story. */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CasUtilisationCompletsTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;

    private long promoId, sId, e1, e2, e3;
    private String code;

    @BeforeEach
    void promotion_avec_session_active() throws Exception {
        long base = ThreadLocalRandom.current().nextLong(0, 900_000L);
        promoId = 40_000_000L + base;
        e1 = promoId + 1; e2 = promoId + 2; e3 = promoId + 3;

        jdbc.update("insert into promotion (id, nom) values (?, 'Promo parcours')", promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Alice', 'A', ?)", e1, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Bruno', 'B', ?)", e2, promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Chloe', 'C', ?)", e3, promoId);

        String body = mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Cours parcours\",\"promotionId\":" + promoId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        sId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));
        code = body.replaceAll(".*\"code\":\"([^\"]+)\".*", "$1");
    }

    private void present(long etudiantId) throws Exception {
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + etudiantId + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void US_formateur_ouvre_session_puis_une_seule_active_par_promotion() throws Exception {
        // 7.4 : une 2e session active pour la meme promotion est refusee
        mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Doublon\",\"promotionId\":" + promoId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("SESSION_DEJA_ACTIVE"));
    }

    @Test
    void US_etudiant_marque_sa_presence_puis_voit_son_statut() throws Exception {
        present(e1);
        // PresenceCard : statut present + 5 tentatives restantes
        mvc.perform(get("/api/sessions/" + sId + "/etudiants/" + e1 + "/statut-presence"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.present").value(true))
                .andExpect(jsonPath("$.tentativesRestantes").value(5));
    }

    @Test
    void US_etudiant_se_trompe_5_fois_est_bloque_puis_debloque_apres_2_min() throws Exception {
        for (int i = 0; i < 5; i++) {
            mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"code\":\"XXXXXX\",\"etudiantId\":" + e2 + "}"))
                    .andExpect(status().isBadRequest());
        }
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"XXXXXX\",\"etudiantId\":" + e2 + "}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value("TROP_DE_TENTATIVES"));
        // meme avec le bon code il reste bloque
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + e2 + "}"))
                .andExpect(status().isTooManyRequests());
        // RG3 : fenetre glissante de 2 minutes -> on vieillit les 5 echecs artificiellement
        jdbc.update("update tentative_code set echoue_at = dateadd('MINUTE', -3, echoue_at) where etudiant_id = ?", e2);
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + e2 + "}"))
                .andExpect(status().isCreated());
    }

    @Test
    void US_formateur_ajoute_une_presence_manuelle_source_FORMATEUR() throws Exception {
        // Q14/RG13 : visible comme ajoute par le formateur
        mvc.perform(post("/api/sessions/" + sId + "/presences/manuelle").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e3 + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.source").value("FORMATEUR"));
        mvc.perform(get("/api/sessions/" + sId + "/presences"))
                .andExpect(jsonPath("$[0].source").value("FORMATEUR"));
    }

    @Test
    void US_depose_exercice_relecteur_attribue_puis_lien_verrouille() throws Exception {
        present(e1); present(e2);
        String body = mvc.perform(post("/api/sessions/" + sId + "/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/alice\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("RELECTEUR_ATTRIBUE"))
                .andReturn().getResponse().getContentAsString();
        long exerciceId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));

        // 7.8 : l'attribution = debut de la relecture -> lien non modifiable
        mvc.perform(put("/api/exercices/" + exerciceId + "/lien").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/alice-v2\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("RELECTURE_COMMENCEE"));
    }

    @Test
    void US_lien_modifiable_tant_que_personne_n_a_ete_assigne() throws Exception {
        // E3 est le seul present -> depot sans relecteur possible (7.6)
        present(e3);
        String body = mvc.perform(post("/api/sessions/" + sId + "/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e3 + ",\"lien\":\"https://a.b/chloe\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statut").value("EN_ATTENTE_RELECTEUR"))
                .andReturn().getResponse().getContentAsString();
        long exerciceId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));

        // RG7 : lien remplaçable tant que la relecture n'a pas commence
        mvc.perform(put("/api/exercices/" + exerciceId + "/lien").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e3 + ",\"lien\":\"https://a.b/chloe-v2\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lien").value("https://a.b/chloe-v2"))
                .andExpect(jsonPath("$.lienModifiable").value(true));
    }

    @Test
    void US_relecteur_note_puis_corrige_avant_cloture_mais_plus_apres() throws Exception {
        present(e1); present(e2);
        String body = mvc.perform(post("/api/sessions/" + sId + "/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/ex-a\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long exerciceId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));

        // qui est le relecteur ? (RG10 : jamais E1)
        Long relecteur = jdbc.queryForObject(
                "select relecteur_id from relecture where exercice_id = ?", Long.class, exerciceId);
        Long autre = relecteur.equals(e1) ? e2 : e1; // impossible, mais garde-fou
        Long relectureId = jdbc.queryForObject(
                "select id from relecture where exercice_id = ?", Long.class, exerciceId);

        // l'auteur ne peut pas soumettre la relecture de quelqu'un d'autre
        mvc.perform(put("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relecteurId\":" + e1 + ",\"note\":10,\"commentaire\":\"non\"}"))
                .andExpect(status().isForbidden());

        // le bon relecteur rend sa note (EF11)
        mvc.perform(put("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relecteurId\":" + relecteur + ",\"note\":14,\"commentaire\":\"correct\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statut").value("RENDUE"));

        // l'exercice passe a RELU, l'auteur voit sa note SANS le nom du relecteur (EF13/RG20)
        mvc.perform(get("/api/etudiants/" + e1 + "/exercices"))
                .andExpect(jsonPath("$[0].statut").value("RELU"))
                .andExpect(jsonPath("$[0].note").value(14))
                .andExpect(jsonPath("$[0].commentaire").value("correct"));

        // correction possible avant clôture (EF17/RG16)
        mvc.perform(put("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relecteurId\":" + relecteur + ",\"note\":16,\"commentaire\":\"correct\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.note").value(16));

        // clôture (EF15)
        mvc.perform(post("/api/sessions/" + sId + "/cloture")).andExpect(status().isOk());

        // apres clôture : correction refusee (EF18/RG17), depot refuse (Q12), presence refusee (RG2)
        mvc.perform(put("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relecteurId\":" + relecteur + ",\"note\":10,\"commentaire\":\"trop tard\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/sessions/" + sId + "/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e3 + ",\"lien\":\"https://a.b/late\"}"))
                .andExpect(status().isConflict());
        mvc.perform(post("/api/presences").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + e3 + "}"))
                .andExpect(status().isConflict());
        // double clôture refusee
        mvc.perform(post("/api/sessions/" + sId + "/cloture"))
                .andExpect(status().isConflict());
    }

    @Test
    void US_formateur_voit_le_tableau_avec_moyenne_null_ou_calculee() throws Exception {
        present(e1); present(e2);
        mvc.perform(post("/api/sessions/" + sId + "/exercices").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"etudiantId\":" + e1 + ",\"lien\":\"https://a.b/ta\"}"))
                .andExpect(status().isCreated());
        Long relectureId = jdbc.queryForObject(
                "select id from relecture where exercice_id = (select max(id) from exercice where session_id = ?)",
                Long.class, sId);
        Long relecteur = jdbc.queryForObject("select relecteur_id from relecture where id = ?", Long.class, relectureId);
        mvc.perform(put("/api/relectures/" + relectureId).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"relecteurId\":" + relecteur + ",\"note\":11,\"commentaire\":\"ok\"}"))
                .andExpect(status().isOk());

        // E1 : une presence, un exercice, moyenne 11 ; E2 present sans exercice (7.7 : il peut etre relecteur)
        mvc.perform(get("/api/tableau?promotionId=" + promoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[0].moyenne").value(11.0))
                .andExpect(jsonPath("$[1].moyenne").doesNotExist())
                .andExpect(jsonPath("$[?(@.etudiantId==" + e2 + ")].presences").value(1));
    }
}
