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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** Issue #24 : la course concurrente sur la presence doit donner 409, jamais 500.
 *  PAS de @Transactional sur la classe : le test doit reproduire des commits reels
 *  separes (comme deux clients differents), sinon tout partage la meme transaction. */
@SpringBootTest
@AutoConfigureMockMvc
class PresenceConcurrenteTest {

    @Autowired private MockMvc mvc;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private org.springframework.transaction.PlatformTransactionManager txManager;

    private long promoId, e1, sId;
    private String code;

    @BeforeEach
    void session_fraiche() throws Exception {
        long base = ThreadLocalRandom.current().nextLong(0, 900_000L);
        promoId = 60_000_000L + base;
        e1 = promoId + 1;
        jdbc.update("insert into promotion (id, nom) values (?, 'Promo bug')", promoId);
        jdbc.update("insert into etudiant (id, prenom, nom, promotion_id) values (?, 'Dave', 'D', ?)", e1, promoId);
        String body = mvc.perform(post("/api/sessions").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"titre\":\"Cours bug\",\"promotionId\":" + promoId + "}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        sId = Long.parseLong(body.replaceAll(".*\"id\":([0-9]+).*", "$1"));
        code = body.replaceAll(".*\"code\":\"([^\"]+)\".*", "$1");
    }

    @Test
    void collision_de_presence_donne_409_et_pas_500() throws Exception {
        // Reproduction de la course REELLE rapportee par le client :
        // deux requetes HTTP partent quasi simultanement. Chacune passe le check
        // existsBy (aucune ne voit l'autre), puis les deux INSERT partent.
        // La perdante viole la contrainte unique (session_id, etudiant_id) :
        //   - avant correctif : 500 ERREUR_INATTENDUE (violation non mappee)
        //   - apres correctif : 409 DEJA_PRESENT au format impose
        java.util.concurrent.CountDownLatch pret = new java.util.concurrent.CountDownLatch(2);
        java.util.concurrent.CountDownLatch go = new java.util.concurrent.CountDownLatch(1);
        java.util.concurrent.atomic.AtomicInteger statut1 = new java.util.concurrent.atomic.AtomicInteger();
        java.util.concurrent.atomic.AtomicInteger statut2 = new java.util.concurrent.atomic.AtomicInteger();

        Thread t1 = new Thread(() -> {
            try {
                pret.countDown();
                go.await();
                var reponse = mvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + e1 + "}")).andReturn();
                statut1.set(reponse.getResponse().getStatus());
            } catch (Exception e) {
                statut1.set(-1);
            }
        });
        Thread t2 = new Thread(() -> {
            try {
                pret.countDown();
                go.await();
                var reponse = mvc.perform(post("/api/presences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"code\":\"" + code + "\",\"etudiantId\":" + e1 + "}")).andReturn();
                statut2.set(reponse.getResponse().getStatus());
            } catch (Exception e) {
                statut2.set(-1);
            }
        });
        t1.start(); t2.start();
        pret.await();
        go.countDown();
        t1.join(); t2.join();

        int s1 = statut1.get(), s2 = statut2.get();
        // exactement un 201 (le gagnant) ; l'autre doit etre 409, JAMAIS 500
        org.assertj.core.api.Assertions.assertThat(java.util.List.of(s1, s2))
                .containsExactlyInAnyOrder(201, 409);

        // une seule presence en base (integrite respectee)
        Long nb = jdbc.queryForObject(
                "select count(*) from presence where session_id = ? and etudiant_id = ?",
                Long.class, sId, e1);
        org.assertj.core.api.Assertions.assertThat(nb).isEqualTo(1L);
    }
}
