package com.kfokam48.presence55.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Trace d'un code errone (RG12 : 5 echecs en 2 minutes -> blocage). Decision section 7 du CDC. */
@Entity
@Table(name = "tentative_code")
public class TentativeCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Column(name = "echoue_at", nullable = false)
    private OffsetDateTime echoueAt;

    protected TentativeCode() { }

    public TentativeCode(Long sessionId, Long etudiantId, OffsetDateTime echoueAt) {
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.echoueAt = echoueAt;
    }

    public Long getId() { return id; }
}
