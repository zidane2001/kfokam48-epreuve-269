package com.kfokam48.presence55.domain;

import jakarta.persistence.*;

/** Exercice depose par un etudiant pour une session (EF4). Statut = D2/D4. */
@Entity
@Table(name = "exercice",
       uniqueConstraints = @UniqueConstraint(columnNames = {"session_id", "etudiant_id"}))
public class Exercice {

    public enum Statut { EN_ATTENTE, RELU }

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

    protected Exercice() { }

    public Exercice(Long sessionId, Long etudiantId, String lien) {
        this.sessionId = sessionId;
        this.etudiantId = etudiantId;
        this.lien = lien;
        this.statut = Statut.EN_ATTENTE;
    }

    public Long getId() { return id; }
    public Long getSessionId() { return sessionId; }
    public Long getEtudiantId() { return etudiantId; }
    public String getLien() { return lien; }
    public Statut getStatut() { return statut; }
    public void setStatut(Statut statut) { this.statut = statut; }
}
