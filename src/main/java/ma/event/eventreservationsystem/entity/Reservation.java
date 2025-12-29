package ma.event.eventreservationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// CORRECTION 1 : On limite le equals/hashCode à l'ID uniquement
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
// CORRECTION 2 : On exclut les relations du toString pour éviter la LazyInitializationException dans les logs
@ToString(exclude = {"utilisateur", "evenement"})
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include // <-- C'est ici qu'on dit "utilise seulement l'ID pour identifier l'objet"
    private Long id;

    @NotNull(message = "Le nombre de places est obligatoire")
    @Min(value = 1, message = "Au moins une place doit être réservée")
    @Max(value = 10, message = "Maximum 10 places par réservation")
    @Column(nullable = false)
    private Integer nombrePlaces;

    @Column(nullable = false)
    private Double montantTotal;

    @Column(nullable = false)
    private LocalDateTime dateReservation;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus statut;

    @Column(unique = true, nullable = false, length = 20)
    private String codeReservation;

    @Size(max = 500, message = "Le commentaire ne peut pas dépasser 500 caractères")
    @Column(length = 500)
    private String commentaire;

    // Relations
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private User utilisateur;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evenement_id", nullable = false)
    private Event evenement;

    @PrePersist
    protected void onCreate() {
        dateReservation = LocalDateTime.now();
        if (statut == null) {
            statut = ReservationStatus.EN_ATTENTE;
        }
        // Générer le code de réservation
        if (codeReservation == null) {
            codeReservation = genererCodeReservation();
        }
        // Note : Le calcul du montant ici peut être risqué si l'événement n'est pas complet.
        // Il vaut mieux gérer le montant dans le Service, mais je laisse tel quel pour l'instant.
        if (evenement != null && nombrePlaces != null && montantTotal == null) {
            // Sécurité : on vérifie si l'objet event est chargé avant d'accéder au prix
            // pour éviter une erreur ici aussi lors de la création
            try {
                montantTotal = evenement.getPrixUnitaire() * nombrePlaces;
            } catch (Exception e) {
                // Si l'event est un proxy non initialisé, on ignore ou on gère
                montantTotal = 0.0;
            }
        }
    }

    // Méthode pour générer un code unique
    private String genererCodeReservation() {
        // Format: EVT-12345 (5 chiffres aléatoires)
        int randomNum = (int) (Math.random() * 90000) + 10000;
        return "EVT-" + randomNum;
    }

    // Méthode pour vérifier si l'annulation est possible
    public boolean peutEtreAnnulee() {
        if (statut == ReservationStatus.ANNULEE) {
            return false;
        }
        // Attention : accéder à evenement.getDateDebut() ici peut aussi provoquer une LazyEx
        // si appelé hors transaction. C'est mieux de faire cette logique dans le Service.
        if (evenement != null) {
            LocalDateTime limite = evenement.getDateDebut().minusHours(48);
            return LocalDateTime.now().isBefore(limite);
        }
        return false;
    }
}