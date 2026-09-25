package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
    long countByEtudiantId(Long etudiantId);
    long countBySessionId(Long sessionId);
    java.util.List<Exercice> findByEtudiantId(Long etudiantId);
    java.util.Optional<Exercice> findBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
}
