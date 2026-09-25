package com.kfokam48.presence55.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;

/** Session de cours : ouverture avec code de presence, expiration H+15 (RG1), cloture. */
@Entity
@Table(name = "session_cours")
public class SessionCours {

    public enum Statut { OUVERTE, CLOTUREE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titre;

    @Column(name = "promotion_id", nullable = false)
    private Long promotionId;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(name = "ouverture_at", nullable = false)
    private OffsetDateTime ouvertureAt;

    @Column(name = "expiration_at", nullable = false)
    private OffsetDateTime expirationAt;

    @Column(nullable = false)
    private boolean cloturee;

    protected SessionCours() { }

    public SessionCours(String titre, Long promotionId, String code,
                        OffsetDateTime ouvertureAt, OffsetDateTime expirationAt) {
        this.titre = titre;
        this.promotionId = promotionId;
        this.code = code;
        this.ouvertureAt = ouvertureAt;
        this.expirationAt = expirationAt;
        this.cloturee = false;
    }

    /** RG1 : le code expire 15 minutes apres l'ouverture. */
    public boolean codeExpire(OffsetDateTime maintenant) {
        return maintenant.isAfter(expirationAt);
    }

    public void cloturer() { this.cloturee = true; }

    public Long getId() { return id; }
    public String getTitre() { return titre; }
    public Long getPromotionId() { return promotionId; }
    public String getCode() { return code; }
    public OffsetDateTime getOuvertureAt() { return ouvertureAt; }
    public OffsetDateTime getExpirationAt() { return expirationAt; }
    public boolean isCloturee() { return cloturee; }
}
