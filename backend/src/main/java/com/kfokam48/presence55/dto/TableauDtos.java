package com.kfokam48.presence55.dto;

/** Reponse 200 du contrat : [ { etudiantId, nom, presences, exercicesDeposes, moyenne, relecturesEnAttente } ]. */
public final class TableauDtos {

    public record LigneTableau(
            Long etudiantId,
            String nom,
            long presences,
            long exercicesDeposes,
            Double moyenne,
            long relecturesEnAttente) { }
}
