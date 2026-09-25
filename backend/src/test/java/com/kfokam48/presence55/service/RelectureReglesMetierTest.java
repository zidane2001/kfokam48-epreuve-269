package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Exercice;
import com.kfokam48.presence55.domain.Relecture;
import com.kfokam48.presence55.dto.RelectureDtos.RelectureRequest;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.ExerciceRepository;
import com.kfokam48.presence55.repository.RelectureRepository;
import com.kfokam48.presence55.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/** B6/ENF11 : test unitaire sur des regles metier reelles — RG12 et CDC v2 7.11. */
class RelectureReglesMetierTest {

    private RelectureRepository relectures;
    private ExerciceRepository exercices;
    private SessionRepository sessions;
    private RelectureService service;

    private Relecture relecture;

    @BeforeEach
    void mise_en_place() {
        relectures = Mockito.mock(RelectureRepository.class);
        exercices = Mockito.mock(ExerciceRepository.class);
        sessions = Mockito.mock(SessionRepository.class);
        service = new RelectureService(relectures, exercices, sessions);

        // Bob (id 2) relit l'exercice d'Alice (id 1) : RG10 n'est pas en jeu ici
        relecture = new Relecture(100L, 2L, java.time.OffsetDateTime.now());
        when(relectures.findById(1L)).thenReturn(Optional.of(relecture));
        when(exercices.findById(100L)).thenReturn(Optional.of(
                new Exercice(10L, 1L, "https://a.b/x", java.time.OffsetDateTime.now())));
    }

    @Test
    void note_non_entiere_ou_hors_bornes_refusee_RG12() {
        assertThatThrownBy(() -> service.rendre(1L, new RelectureRequest(-1, "commentaire")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "NOTE_INVALIDE");
        assertThatThrownBy(() -> service.rendre(1L, new RelectureRequest(21, "commentaire")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "NOTE_INVALIDE");
    }

    @Test
    void commentaire_vide_refuse_CDC_7_11() {
        assertThatThrownBy(() -> service.rendre(1L, new RelectureRequest(15, "  ")))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "COMMENTAIRE_MANQUANT");
        assertThatThrownBy(() -> service.rendre(1L, new RelectureRequest(15, null)))
                .isInstanceOf(BusinessException.class)
                .hasFieldOrPropertyWithValue("code", "COMMENTAIRE_MANQUANT");
    }

    @Test
    void note_et_commentaire_valides_passent() {
        var reponse = service.rendre(1L, new RelectureRequest(15, "Bon travail"));
        org.assertj.core.api.Assertions.assertThat(reponse.note()).isEqualTo(15);
        org.assertj.core.api.Assertions.assertThat(reponse.statutExercice()).isEqualTo("RELU");
    }
}
