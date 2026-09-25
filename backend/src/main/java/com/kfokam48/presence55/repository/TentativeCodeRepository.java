package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.TentativeCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.OffsetDateTime;

public interface TentativeCodeRepository extends JpaRepository<TentativeCode, Long> {
    long countBySessionIdAndEtudiantIdAndEchoueAtAfter(Long sessionId, Long etudiantId, OffsetDateTime depuis);
}
