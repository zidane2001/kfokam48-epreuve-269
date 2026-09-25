package com.kfokam48.presence55.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ExerciceDtos {

    /** Requete de depot : { sessionId, etudiantId, lien }. */
    public record DepotRequest(
            @NotNull(message = "sessionId manquant") Long sessionId,
            @NotNull(message = "etudiantId manquant") Long etudiantId,
            @NotBlank(message = "lien manquant") String lien) { }

    /** Reponse 201 du contrat : { id, statut } + infos utiles pour l'ecran. */
    public record DepotResponse(Long id, String statut, Long relecteurId) { }
}
