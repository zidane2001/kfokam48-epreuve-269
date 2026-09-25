package com.kfokam48.presence55.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Presence a une session, source ETUDIANT ou FORMATEUR (RG13/14), unique par couple (RG4). */
@Entity
@Table(name = "presence",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
public class Presence {

    public enum Source { ETUDIANT, FORMATEUR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Source source;

    @Column(name = "enregistree_at", nullable = false)
    private OffsetDateTime enregistreeAt;

    protected Presence() { }

    public Presence(Long sessionId, Long etudiantId, Source source, OffsetDateTime enregistreeAt) {
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.source = source;
        this.enregistreeAt = enregistreeAt;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public Long getEtudiantId() { return etudiantId; }
    public Source getSource() { return source; }
    public OffsetDateTime getEnregistreeAt() { return enregistreeAt; }
}
