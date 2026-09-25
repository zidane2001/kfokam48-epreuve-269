package com.kfokam48.presence55.dto;

import jakarta.validation.constraints.NotNull;

public final class RelectureDtos {

    /** Requete du relecteur : { note, commentaire }. */
    public record RelectureRequest(
            @NotNull(message = "note manquante") Integer note,
            String commentaire) { }

    /** Reponse 200 : la relecture enregistree. */
    public record RelectureResponse(
            Long id, Long exerciceId, Integer note, String commentaire, String statutExercice) { }
}
