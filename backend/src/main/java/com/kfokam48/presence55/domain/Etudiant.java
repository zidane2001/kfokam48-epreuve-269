package com.kfokam48.presence55.domain;

import jakarta.persistence.*;

/** Etudiant, rattache a une promotion. Pas de mot de passe : choix du nom (Q1). */
@Entity
@Table(name = "etudiant")
public class Etudiant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nom;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    protected Etudiant() { }

    public Etudiant(String nom, Long promotionId) {
        this.nom = nom;
        this.promotionId = promotionId;
    }

    public Long getId() { return id; }
    public String getNom() { return nom; }
    public Long getPromotionId() { return promotionId; }
}
