package ma.event.eventreservationsystem.service;

import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface EventService {
    // Gestion des événements
    Event createEvent(Event event, Long organisateurId);
    Event updateEvent(Long id, Event event, Long utilisateurId);
    Event publierEvent(Long id, Long utilisateurId);
    void annulerEvent(Long id, Long utilisateurId);
    void deleteEvent(Long id, Long utilisateurId);

    // Recherche et récupération
    Event findById(Long id);
    List<Event> findAll();
    List<Event> findByOrganisateur(Long organisateurId);
    List<Event> findByCategorie(EventCategory categorie);
    List<Event> findByStatut(EventStatus statut);
    List<Event> findAvailableEvents();
    List<Event> findPopularEvents();

    // Recherche avancée
    List<Event> searchEvents(EventCategory categorie, LocalDateTime dateDebut,
                             LocalDateTime dateFin, String ville,
                             Double prixMin, Double prixMax);
    List<Event> searchByTitre(String keyword);

    // Calculs
    int getPlacesDisponibles(Long eventId);
    Map<String, Object> getStatistiquesOrganisateur(Long organisateurId);

    // Vérifications
    void verifierEvenementsTermines();
}
