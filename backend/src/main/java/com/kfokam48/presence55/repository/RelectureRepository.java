package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.Relecture;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface RelectureRepository extends JpaRepository<Relecture, Long> {
    // issue #25 : un exercice peut avoir plusieurs relectures (2 relecteurs).
    // Ne jamais re-ajouter findByExerciceId -> Optional : IncorrectResultSizeDataAccessException des que 2 lignes.
    java.util.List<Relecture> findAllByExerciceId(Long exerciceId);
    boolean existsByExerciceIdAndRelecteurId(Long exerciceId, Long relecteurId);
    List<Relecture> findByRelecteurId(Long relecteurId);
    List<Relecture> findByRelecteurIdAndRendueAtIsNull(Long relecteurId);
}
