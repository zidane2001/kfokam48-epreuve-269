package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.Etudiant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    List<Etudiant> findByPromotionId(Long promotionId);
}
