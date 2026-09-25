package com.kfokam48.presence55.domain;

import jakarta.persistence.*;

/** Compte de connexion (evolution PO). Role ETUDIANT (rattache a un etudiant) ou FORMATEUR. */
@Entity
@Table(name = "compte")
public class Compte {

    public enum Role { ETUDIANT, FORMATEUR }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String login;

    /** Format {bcrypt}... — jamais de mot de passe en clair. */
    @Column(name = "mot_de_passe", nullable = false)
    private String motDePasse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "etudiant_id")
    private Long etudiantId;

    protected Compte() { }

    public Compte(String login, String motDePasse, Role role, Long etudiantId) {
        this.login = login;
        this.motDePasse = motDePasse;
        this.role = role;
        this.etudiantId = etudiantId;
    }

    public Long getId() { return id; }
    public String getLogin() { return login; }
    public String getMotDePasse() { return motDePasse; }
    public Role getRole() { return role; }
    public Long getEtudiantId() { return etudiantId; }

    public void setMotDePasse(String motDePasse) { this.motDePasse = motDePasse; }
}
