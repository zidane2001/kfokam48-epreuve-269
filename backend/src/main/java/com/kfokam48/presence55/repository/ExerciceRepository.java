package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.Exercice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciceRepository extends JpaRepository<Exercice, Long> {
    boolean existsBySessionIdAndEtudiantId(Long sessionId, Long etudiantId);
    long countByEtudiantId(Long etudiantId);
    java.util.List<Exercice> findByEtudiantId(Long etudiantId);
}
