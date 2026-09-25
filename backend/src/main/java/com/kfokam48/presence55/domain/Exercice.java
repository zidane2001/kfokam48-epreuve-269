package com.kfokam48.presence55.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Exercice depose par un etudiant (EF8). 3 etats : CDC v2 7.6 et 7.8. */
@Entity
@Table(name = "exercice",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
public class Exercice {

    public enum Statut { EN_ATTENTE_RELECTEUR, RELECTEUR_ATTRIBUE, RELU }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "etudiant_id", nullable = false)
    private Long etudiantId;

    @Column(nullable = false)
    private String lien;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    @Column(name = "depose_at", nullable = false)
    private OffsetDateTime deposeAt;

    protected Exercice() { }

    public Exercice(Long sessionId, Long etudiantId, String lien, OffsetDateTime deposeAt) {
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.lien = lien;
        this.deposeAt = deposeAt;
        this.statut = Statut.EN_ATTENTE_RELECTEUR;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public Long getEtudiantId() { return etudiantId; }
    public String getLien() { return lien; }
    public Statut getStatut() { return statut; }
    public OffsetDateTime getDeposeAt() { return deposeAt; }

    public void setLien(String lien) { this.lien = lien; }
    public void setStatut(Statut statut) { this.statut = statut; }
}
