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

    // --- REQUÊTES OPTIMISÉES ---

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organisateur")
    List<Event> findAllWithOrganisateur();

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organisateur WHERE " +
            "LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.ville) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(e.lieu) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchWithOrganisateur(@Param("keyword") String keyword);

    // --- REQUÊTES STANDARD ---

    List<Event> findByCategorie(EventCategory categorie);

    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.dateDebut BETWEEN :dateDebut AND :dateFin")
    List<Event> findPublishedEventsBetweenDates(
            @Param("dateDebut") LocalDateTime dateDebut,
            @Param("dateFin") LocalDateTime dateFin
    );

    List<Event> findByOrganisateurAndStatut(User organisateur, EventStatus statut);

    @Query("SELECT e FROM Event e WHERE e.statut = 'PUBLIE' AND e.statut != 'TERMINE'")
    List<Event> findAvailableEvents();

    long countByCategorie(EventCategory categorie);

    @Query("SELECT e FROM Event e WHERE LOWER(e.lieu) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(e.ville) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> findByLieuOrVille(@Param("keyword") String keyword);

    @Query("SELECT e FROM Event e WHERE LOWER(e.titre) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Event> searchByTitre(@Param("keyword") String keyword);

    @Query("SELECT e FROM Event e WHERE e.prixUnitaire BETWEEN :prixMin AND :prixMax")
    List<Event> findByPrixBetween(@Param("prixMin") Double prixMin, @Param("prixMax") Double prixMax);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.organisateur WHERE e.id = :id")
    List<Event> findByIdWithOrganisateur(@Param("id") Long id);
    // --- 👇👇 C'EST CETTE MÉTHODE QUI MANQUAIT 👇👇 ---
    // Elle permet de chercher par l'ID (3) au lieu de l'objet User entier
    List<Event> findByOrganisateurId(Long organisateurId);
    // ----------------------------------------------------

    // Ancienne méthode (on la garde au cas où, mais on préfère celle du dessus)
    List<Event> findByOrganisateur(User organisateur);

    List<Event> findByStatut(EventStatus statut);

    List<Event> findByVille(String ville);

    @Query("SELECT e FROM Event e WHERE e.dateDebut >= :date")
    List<Event> findEventsAfterDate(@Param("date") LocalDateTime date);

    @Query("SELECT e FROM Event e LEFT JOIN e.reservations r " +
            "WHERE e.statut = 'PUBLIE' " +
            "GROUP BY e.id " +
            "ORDER BY COUNT(r.id) DESC")
    List<Event> findPopularEvents();

    long countByStatut(EventStatus statut);

    @Query("SELECT e FROM Event e WHERE e.organisateur.id = :organisateurId AND e.statut = :statut")
    List<Event> findByOrganisateurIdAndStatut(
            @Param("organisateurId") Long organisateurId,
            @Param("statut") EventStatus statut
    );
}