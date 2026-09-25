package com.kfokam48.presence55.dto;

/** DTOs de lecture des sessions pour l'espace formateur. */
public final class SessionQueryDtos {

    public record SessionListeDto(
            Long id, String titre, Long promotionId, String promotionNom,
            String code, String ouvertureAt, String expirationAt,
            String statut, String clotureAt, boolean codeExpire) { }

    public record SessionDetailDto(
            Long id, String titre, Long promotionId, String promotionNom,
            String code, String ouvertureAt, String expirationAt,
            String statut, String clotureAt, boolean codeExpire,
            int nbEtudiants, int nbPresents, int nbExercices) { }
}
