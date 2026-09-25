package com.kfokam48.presence55.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.OffsetDateTime;

public final class SessionDtos {

    /** Requete du formateur. */
    public record OuvrirSessionRequest(
            @NotBlank(message = "titre manquant") String titre,
            @NotNull(message = "promotionId manquant") Long promotionId) { }

    /** Reponse 201 du contrat : { id, code, ouvertureAt, expirationAt }. */
    public record SessionResponse(
            Long id, String code,
            @JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime ouvertureAt,
            @JsonFormat(shape = JsonFormat.Shape.STRING) OffsetDateTime expirationAt) { }
}
