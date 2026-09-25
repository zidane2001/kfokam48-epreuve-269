package com.kfokam48.presence55.dto;

import java.util.List;

/** DTOs des espaces etudiant/formateur — miroir des types du design (frontendModel). */
public final class EspaceDtos {

    public record StatutPresenceDto(
            Long sessionId, Long etudiantId, boolean present,
            String source, String enregistreeAt,
            int tentativesRestantes, String bloqueJusqua) { }

    public record PresenceSessionDto(
            Long id, Long sessionId, Long etudiantId, String etudiantNom,
            String source, String enregistreeAt) { }

    public record AjoutManuelRequest(Long etudiantId) { }

    public record DepotSurSessionRequest(Long etudiantId, String lien) { }

    public record ModifierLienRequest(Long etudiantId, String lien) { }

    /** Vue auteur d'un exercice — ne contient JAMAIS l'identite du relecteur (RG11/RG20).
     *  Issue #25 : noteProvisoire = true si une seule relecture rendue sur deux. */
    public record ExerciceAuteurDto(
            Long id, Long sessionId, String sessionTitre, String sessionStatut,
            String lien, String deposeAt, String statut, boolean lienModifiable,
            Integer note, String commentaire, boolean resultatDefinitif,
            boolean noteProvisoire) { }

    public record RelectureAssigneeDto(
            Long id, Long exerciceId, Long sessionId, String sessionTitre, String lien,
            Integer note, String commentaire, String statut, boolean modifiable,
            String attribueeAt, String rendueAt) { }

    public record SoumissionRelectureRequest(Long relecteurId, Integer note, String commentaire) { }
}
