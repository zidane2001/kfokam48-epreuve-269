package com.kfokam48.presence55.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** B6 : test unitaire sur le generateur de code de presence (EF1). */
class CodePresenceGeneratorTest {

    @Test
    void genere_un_code_de_6_caracteres_sans_ambiguites() {
        String attendus = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
        for (int i = 0; i < 50; i++) {
            String code = CodePresenceGenerator.generer();
            assertThat(code).hasSize(6);
            assertThat(code.chars()).allMatch(c -> attendus.indexOf(c) >= 0,
                    "caractere hors alphabet sans ambiguite");
        }
    }

    @Test
    void genere_des_codes_differents_sur_100_tirages() {
        long distincts = java.util.stream.IntStream.range(0, 100)
                .mapToObj(i -> CodePresenceGenerator.generer())
                .distinct().count();
        assertThat(distincts).isGreaterThan(90);
    }
}
