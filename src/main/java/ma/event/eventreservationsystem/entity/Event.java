package ma.event.eventreservationsystem.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import org.hibernate.proxy.HibernateProxy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "events")
@Getter // Remplace @Data (Safe)
@Setter // Remplace @Data (Safe)
@ToString // Remplace @Data (Safe)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Le titre est obligatoire")
    @Size(min = 5, max = 100, message = "Le titre doit contenir entre 5 et 100 caractères")
    @Column(nullable = false, length = 100)
    private String titre;

    @Size(max = 1000, message = "La description ne peut pas dépasser 1000 caractères")
    @Column(length = 1000)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventCategory categorie;

    @NotNull(message = "La date de début est obligatoire")
    @Future(message = "La date de début doit être dans le futur")
    @Column(nullable = false)
    private LocalDateTime dateDebut;

    @NotNull(message = "La date de fin est obligatoire")
    @Column(nullable = false)
    private LocalDateTime dateFin;

    @NotBlank(message = "Le lieu est obligatoire")
    @Column(nullable = false)
    private String lieu;

    @NotBlank(message = "La ville est obligatoire")
    @Column(nullable = false)
    private String ville;

    @NotNull(message = "La capacité maximale est obligatoire")
    @Min(value = 1, message = "La capacité doit être supérieure à 0")
    @Column(nullable = false)
    private Integer capaciteMax;

    @NotNull(message = "Le prix est obligatoire")
    @Min(value = 0, message = "Le prix doit être supérieur ou égal à 0")
    @Column(nullable = false)
    private Double prixUnitaire;

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus statut;

    @Column(nullable = false)
    private LocalDateTime dateCreation;

    private LocalDateTime dateModification;

    // --- RELATIONS ---

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organisateur_id", nullable = false)
    @ToString.Exclude // IMPORTANT : Empêche le crash lors des logs
    private User organisateur;

    @OneToMany(mappedBy = "evenement", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude // IMPORTANT : Empêche le crash et les boucles infinies
    private List<Reservation> reservations = new ArrayList<>();

    // --- LIFECYCLE ---

    @PrePersist
    protected void onCreate() {
        dateCreation = LocalDateTime.now();
        if (statut == null) {
            statut = EventStatus.BROUILLON;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        dateModification = LocalDateTime.now();
    }

    // --- MÉTHODES MÉTIER ---

    public int getPlacesDisponibles() {
        int placesReservees = reservations.stream()
                .filter(r -> r.getStatut() == ma.event.eventreservationsystem.entity.enums.ReservationStatus.CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        return capaciteMax - placesReservees;
    }

    // --- EQUALS & HASHCODE (Optimisés pour JPA) ---

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        Event event = (Event) o;
        return getId() != null && Objects.equals(getId(), event.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}