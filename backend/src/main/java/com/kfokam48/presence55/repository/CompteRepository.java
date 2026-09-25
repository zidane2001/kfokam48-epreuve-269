package com.kfokam48.presence55.repository;

import com.kfokam48.presence55.domain.Compte;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CompteRepository extends JpaRepository<Compte, Long> {
    Optional<Compte> findByLogin(String login);
    List<Compte> findByEtudiantId(Long etudiantId);
}
