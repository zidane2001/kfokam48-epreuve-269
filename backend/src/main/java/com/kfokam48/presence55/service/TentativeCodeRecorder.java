package com.kfokam48.presence55.service;

import com.kfokam48.presence55.domain.TentativeCode;
import com.kfokam48.presence55.repository.TentativeCodeRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.time.OffsetDateTime;

/**
 * RG3 : chaque echec de code doit etre PERSISTE, meme quand la requete principale
 * echoue en exception (400/410) — sinon le compteur de blocage ne compte jamais.
 * Une transaction independante (REQUIRES_NEW) survit au rollback de la requete.
 */
@Component
public class TentativeCodeRecorder {

    private final TentativeCodeRepository tentatives;

    public TentativeCodeRecorder(TentativeCodeRepository tentatives) {
        this.tentatives = tentatives;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void enregistrer(Long sessionId, Long etudiantId, OffsetDateTime quand) {
        tentatives.save(new TentativeCode(sessionId, etudiantId, quand));
    }
}
