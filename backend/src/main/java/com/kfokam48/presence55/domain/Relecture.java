package com.kfokam48.presence55.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Relecture d'un exercice par un pair. Issue #25 : DEUX relecteurs par exercice
 *  (unicite par couple exercice+relecteur), note entiere 0-20 (RG12). */
@Entity
@Table(name = "relecture",
       uniqueConstraints = @UniqueConstraint(columnNames = {"exercice_id", "relecteur_id"}))
public class Relecture {

    public enum Statut { EN_ATTENTE, RENDUE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercice_id", nullable = false)
    private Long exerciceId;

    @Column(name = "relecteur_id", nullable = false)
    private Long relecteurId;

    @Column(nullable = true)
    private Integer note;

    @Column(length = 2000)
    private String commentaire;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Statut statut;

    @Column(name = "attribuee_at", nullable = false)
    private OffsetDateTime attribueeAt;

    @Column(name = "rendue_at")
    private OffsetDateTime rendueAt;

    @Column(name = "maj_at")
    private OffsetDateTime majAt;

    protected Relecture() { }

    public Relecture(Long exerciceId, Long relecteurId, OffsetDateTime attribueeAt) {
        this.exerciceId = exerciceId;
        this.relecteurId = relecteurId;
        this.attribueeAt = attribueeAt;
        this.statut = Statut.EN_ATTENTE;
    }

    public void rendre(int note, String commentaire, OffsetDateTime quand) {
        this.note = note;
        this.commentaire = commentaire;
        this.statut = Statut.RENDUE;
        this.rendueAt = quand;
    }

    /** RG16 : correction possible tant que la session n'est pas cloturee (RG17 ensuite). */
    public void corriger(int note, OffsetDateTime quand) {
        this.note = note;
        this.majAt = quand;
    }

    public Long getId() { return id; }
    public Long getExerciceId() { return exerciceId; }
    public Long getRelecteurId() { return relecteurId; }
    public Integer getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public Statut getStatut() { return statut; }
    public OffsetDateTime getAttribueeAt() { return attribueeAt; }
    public OffsetDateTime getRendueAt() { return rendueAt; }
    public OffsetDateTime getMajAt() { return majAt; }
}
