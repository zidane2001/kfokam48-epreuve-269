package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.Exercice;
import com.kfokam48.presence55.domain.Presence;
import com.kfokam48.presence55.domain.Relecture;
import com.kfokam48.presence55.domain.SessionCours;
import com.kfokam48.presence55.dto.ExerciceDtos.DepotRequest;
import com.kfokam48.presence55.dto.ExerciceDtos.DepotResponse;
import com.kfokam48.presence55.exception.BusinessException;
import com.kfokam48.presence55.repository.ExerciceRepository;
import com.kfokam48.presence55.repository.PresenceRepository;
import com.kfokam48.presence55.repository.RelectureRepository;
import com.kfokam48.presence55.repository.EtudiantRepository;
import com.kfokam48.presence55.repository.SessionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class ExerciceService {

    private static final SecureRandom RNG = new SecureRandom();

    private final ExerciceRepository exercices;
    private final PresenceRepository presences;
    private final RelectureRepository relectures;
    private final EtudiantRepository etudiants;
    private final SessionRepository sessions;

    public ExerciceService(ExerciceRepository exercices,
                           PresenceRepository presences,
                           RelectureRepository relectures,
                           EtudiantRepository etudiants,
                           SessionRepository sessions) {
        this.exercices = exercices;
        this.presences = presences;
        this.relectures = relectures;
        this.etudiants = etudiants;
        this.sessions = sessions;
    }

    /** EF4 : depot du lien. 409 si second depot ; EF3 : assignation relecteur (RG2/RG4). */
    @Transactional
    public DepotResponse deposer(DepotRequest req) {
        SessionCours session = sessions.findById(req.sessionId())
                .orElseThrow(() -> new BusinessException("SESSION_INCONNUE", "Cette session n'existe pas."));

        if (session.isCloturee()) {
            throw new BusinessException("SESSION_CLOTUREE",
                    "Cette session est cloturee : depot impossible.");   // Q12 : avant cloture seulement
        }
        if (!etudiants.existsById(req.etudiantId())) {
            throw new BusinessException("ETUDIANT_INCONNU", "Cet etudiant n'existe pas.");
        }
        String lienValide = validerLien(req.lien());                     // 400 LIEN_INVALIDE
        if (exercices.existsBySessionIdAndEtudiantId(req.sessionId(), req.etudiantId())) {
            throw new BusinessException("EXERCICE_DEJA_DEPOSE",
                    "Vous avez deja depose un exercice pour cette session.");  // -> 409
        }

        Exercice ex = exercices.save(new Exercice(req.sessionId(), req.etudiantId(), lienValide, OffsetDateTime.now()));
        assignerRelecteurs(ex);                                          // EF10 : attribue -> RELECTEUR_ATTRIBUE

        return new DepotResponse(ex.getId(), ex.getStatut().name());
    }

    /** EF10/RG9 v2 (issue #25) : DEUX relecteurs distincts au hasard parmi les presents != depositaire (RG10).
     *  CDC v2 7.8 : l'attribution EST le debut de la relecture -> RELECTEUR_ATTRIBUE.
     *  Fallback : 1 seul autre present -> 1 relecteur ; aucun -> reste EN_ATTENTE_RELECTEUR. */
    private void assignerRelecteurs(Exercice ex) {
        List<Long> presents = presences.findBySessionId(ex.getSessionId()).stream()
                .map(Presence::getEtudiantId)
                .filter(id -> !id.equals(ex.getEtudiantId()))            // RG10 : jamais soi-meme
                .collect(java.util.stream.Collectors.toList());
        java.util.Collections.shuffle(presents, RNG);
        int nb = Math.min(2, presents.size());                           // issue #25 : 2 si possible
        for (int i = 0; i < nb; i++) {
            relectures.save(new Relecture(ex.getId(), presents.get(i), OffsetDateTime.now()));
        }
        if (nb > 0) {
            ex.setStatut(Exercice.Statut.RELECTEUR_ATTRIBUE);
            exercices.save(ex);
        }
    }

    /** Validation du lien, partagee avec PUT /api/exercices/{id}/lien (EF14). */
    public static String validerLien(String lien) {
        if (lien == null || lien.isBlank()) {
            throw new BusinessException("LIEN_INVALIDE", "Le lien de l'exercice est obligatoire.");
        }
        try {
            URI uri = new URI(lien.trim());
            if (!"http".equalsIgnoreCase(uri.getScheme()) && !"https".equalsIgnoreCase(uri.getScheme())) {
                throw new BusinessException("LIEN_INVALIDE", "Le lien doit commencer par http:// ou https://");
            }
        } catch (URISyntaxException e) {
            throw new BusinessException("LIEN_INVALIDE", "Le lien de l'exercice est invalide.");
        }
        return lien.trim();
    }
}
