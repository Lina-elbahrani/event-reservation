package ma.event.eventreservationsystem.repository;

import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    // Trouver les événements par catégorie
    List<Event> findByCategorie(EventCategory categorie);

    // Trouver les événements publiés entre deux dates
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.dateDebut BETWEEN :dateDebut AND :dateFin")
    List<Event> findPublishedEventsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    // Trouver les événements d'un organisateur avec un statut donné
    List<Event> findByOrganisateurAndStatut(User organisateur, EventStatus statut);

    // Trouver les événements disponibles (publiés et non terminés)
    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.statut != 'TERMINE'")
    List<Event> findAvailableEvents();

    // Compter le nombre d'événements par catégorie
    long countByCategorie(EventCategory categorie);

    // Trouver les événements par lieu ou ville
    @Query("SELECT e FROM Event e WHERE LOWER(e.lieu) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.ville) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> findByLieuOrVille(@Param("keyword") String keyword);

    // Rechercher les événements par titre (contenant un mot-clé)
    @Query("SELECT e FROM Event e WHERE LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByTitre(@Param("keyword") String keyword);

    // Trouver les événements par plage de prix
    @Query("SELECT e FROM Event e WHERE e.prixUnitaire BETWEEN :prixMin AND :prixMax")
    List<Event> findByPrixBetween(@Param("prixMin") Double prixMin, @Param("prixMax") Double prixMax);

    // Trouver tous les événements d'un organisateur
    List<Event> findByOrganisateur(User organisateur);

    // Trouver les événements par statut
    List<Event> findByStatut(EventStatus statut);

    // Trouver les événements par ville
    List<Event> findByVille(String ville);

    // Trouver les événements après une certaine date
    @Query("SELECT e FROM Event e WHERE e.dateDebut >= :date")
    List<Event> findEventsAfterDate(@Param("date") LocalDateTime date);

    // Trouver les événements populaires (les plus réservés)
    @Query("SELECT e FROM Event e LEFT JOIN e.reservations r " +
            "WHERE e.statut = 'PUBLIE' " +
            "GROUP BY e.id " +
            "ORDER BY COUNT(r.id) DESC")
    List<Event> findPopularEvents();

    // Compter les événements par statut
    long countByStatut(EventStatus statut);

    // Trouver les événements d'un organisateur par statut
    @Query("SELECT e FROM Event e WHERE e.organisateur.id = :organisateurId AND e.statut = :statut")
    List<Event> findByOrganisateurIdAndStatut(
            @Param("organisateurId") Long organisateurId,
            @Param("statut") EventStatus statut
    );
}
