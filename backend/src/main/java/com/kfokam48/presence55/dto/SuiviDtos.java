package com.kfokam48.presence55.dto;

import java.util.List;

/** Vue riche du tableau (design frontendModel, F3 : moyennes calculees serveur). */
public final class SuiviDtos {

    public record SuiviLigne(
            Long etudiantId,
            String nomComplet,
            Boolean presentSession,          // null quand perimetre multi-sessions
            String sourcePresence,           // ETUDIANT | FORMATEUR | null
            String statutExercice,           // EN_ATTENTE_RELECTEUR | RELECTEUR_ATTRIBUE | RELU | null
            long presences,
            long sessionsComptees,
            long exercicesDeposes,
            long notesRecues,
            Double moyenne,                  // null si aucune note recue (7.9)
            long relecturesEnAttente) { }

    public record Totaux(
            long etudiants,
            Long presents,                   // null quand perimetre multi-sessions
            long sessionsComptees,
            long exercicesDeposes,
            long relecturesEnAttente,
            Double moyennePromotion) { }

    public record Suivi(
            Long promotionId,
            Long sessionId,                  // null = toutes les sessions de la promotion
            List<SuiviLigne> lignes,
            Totaux totaux) { }
}
