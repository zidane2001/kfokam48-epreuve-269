package com.kfokam48.presence55.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class PresenceDtos {

    /** Requete de l'etudiant. */
    public record PresenceRequest(
            @NotBlank(message = "code manquant") String code,
            @NotNull(message = "etudiantId manquant") Long etudiantId) { }

    /** Reponse 201 du contrat : { id, sessionId, etudiantId, source }. */
    public record PresenceResponse(
            Long id, Long sessionId, Long etudiantId, String source) { }
}
