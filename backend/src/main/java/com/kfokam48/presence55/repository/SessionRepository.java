package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.SessionCours;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface SessionRepository extends JpaRepository<SessionCours, Long> {
    Optional<SessionCours> findByCode(String code);
    boolean existsByPromotionIdAndClotureeFalse(Long promotionId);   // CDC v2 7.4
    Optional<SessionCours> findByPromotionIdAndClotureeFalse(Long promotionId); // session active
}
