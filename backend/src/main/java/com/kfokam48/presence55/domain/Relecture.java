package com.kfokam48.presence55.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Relecture d'un exercice par un pair. Un seul relecteur (RG3), note entiere 0-20 (RG5). */
@Entity
@Table(name = "relecture")
public class Relecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "exercice_id", nullable = false, unique = true)   // RG3
    private Long exerciceId;

    @Column(name = "relecteur_id", nullable = false)
    private Long relecteurId;

    @Column(nullable = true)
    private Integer note;

    @Column(length = 2000)
    private String commentaire;

    @Column(name = "rendue_at")
    private OffsetDateTime rendueAt;

    @Column(name = "maj_at")
    private OffsetDateTime majAt;

    protected Relecture() { }

    public Relecture(Long exerciceId, Long relecteurId) {
        this.exerciceId = exerciceId;
        this.relecteurId = relecteurId;
    }

    public Long getId() { return id; }
    public Long getExerciceId() { return exerciceId; }
    public Long getRelecteurId() { return relecteurId; }
    public Integer getNote() { return note; }
    public String getCommentaire() { return commentaire; }
    public OffsetDateTime getRendueAt() { return rendueAt; }
    public OffsetDateTime getMajAt() { return majAt; }

    public void rendre(int note, String commentaire, OffsetDateTime quand) {
        this.note = note;
        this.commentaire = commentaire;
        this.rendueAt = quand;
    }

    /** RG6 : correction possible tant que la session n'est pas cloturee. */
    public void corriger(int note, OffsetDateTime quand) {
        this.note = note;
        this.majAt = quand;
    }
}
