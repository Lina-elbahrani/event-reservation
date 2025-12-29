package ma.event.eventreservationsystem.repository;

import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph; // <--- IMPERATIF
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    // =================================================================================
    //  LA CORRECTION EST ICI :
    //  On force le chargement de 'utilisateur' et 'evenement' pour la liste principale
    // =================================================================================
    @Override
    @EntityGraph(attributePaths = {"utilisateur", "evenement"})
    List<Reservation> findAll();


    // --- Le reste de votre code original ---

    // Trouver les réservations d'un utilisateur
    List<Reservation> findByUtilisateur(User utilisateur);

    // Trouver les réservations d'un événement avec un statut donné
    List<Reservation> findByEvenementAndStatut(Event evenement, ReservationStatus statut);

    // Calculer le nombre total de places réservées pour un événement
    @Query("SELECT COALESCE(SUM(r.nombrePlaces), 0) FROM Reservation r " +
            "WHERE r.evenement.id = :evenementId AND r.statut = 'CONFIRMEE'")
    Integer countTotalPlacesReserveesForEvent(@Param("evenementId") Long evenementId);

    // Trouver les réservations par code
    Optional<Reservation> findByCodeReservation(String codeReservation);

    // Trouver les réservations entre deux dates
    @Query("SELECT r FROM Reservation r WHERE r.dateReservation BETWEEN :dateDebut AND :dateFin")
    List<Reservation> findReservationsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    // Trouver les réservations confirmées d'un utilisateur
    List<Reservation> findByUtilisateurAndStatut(User utilisateur, ReservationStatus statut);

    // Calculer le montant total des réservations par utilisateur
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r " +
            "WHERE r.utilisateur.id = :utilisateurId AND r.statut = 'CONFIRMEE'")
    Double calculateTotalAmountByUser(@Param("utilisateurId") Long utilisateurId);

    // Trouver toutes les réservations d'un événement
    List<Reservation> findByEvenement(Event evenement);

    // Trouver les réservations d'un utilisateur par statut
    @Query("SELECT r FROM Reservation r WHERE r.utilisateur.id = :utilisateurId AND r.statut = :statut")
    List<Reservation> findByUtilisateurIdAndStatut(
            @Param("utilisateurId") Long utilisateurId,
            @Param("statut") ReservationStatus statut
    );

    // Compter les réservations d'un utilisateur
    long countByUtilisateur(User utilisateur);

    // Compter les réservations d'un événement
    long countByEvenement(Event evenement);

    // Compter les réservations confirmées d'un événement
    @Query("SELECT COUNT(r) FROM Reservation r WHERE r.evenement.id = :evenementId AND r.statut = 'CONFIRMEE'")
    long countConfirmedReservationsByEvent(@Param("evenementId") Long evenementId);

    // Trouver les réservations à venir (événements dans le futur)
    @Query("SELECT r FROM Reservation r WHERE r.evenement.dateDebut > :now AND r.statut = 'CONFIRMEE'")
    List<Reservation> findUpcomingReservations(@Param("now") LocalDateTime now);

    // Trouver les réservations d'un utilisateur pour les événements à venir
    @Query("SELECT r FROM Reservation r " +
            "WHERE r.utilisateur.id = :utilisateurId " +
            "AND r.evenement.dateDebut > :now " +
            "AND r.statut = 'CONFIRMEE' " +
            "ORDER BY r.evenement.dateDebut ASC")
    List<Reservation> findUpcomingReservationsByUser(
            @Param("utilisateurId") Long utilisateurId,
            @Param("now") LocalDateTime now
    );

    // Calculer le revenu total d'un événement
    @Query("SELECT COALESCE(SUM(r.montantTotal), 0.0) FROM Reservation r " +
            "WHERE r.evenement.id = :evenementId AND r.statut = 'CONFIRMEE'")
    Double calculateTotalRevenueByEvent(@Param("evenementId") Long evenementId);

    // Vérifier si un code de réservation existe
    boolean existsByCodeReservation(String codeReservation);
}