package ma.event.eventreservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.repository.EventRepository;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.EventService;
import ma.event.eventreservationsystem.service.UserService;
import ma.event.eventreservationsystem.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final UserService userService;

    @Override
    public Event createEvent(Event event, Long organisateurId) {
        User organisateur = userService.findById(organisateurId);

        // RÈGLE 1 : Seulement ADMIN ou ORGANIZER peuvent créer des événements
        if (organisateur.getRole() != UserRole.ADMIN &&
                organisateur.getRole() != UserRole.ORGANIZER) {
            throw new ForbiddenException("Vous n'avez pas les droits pour créer un événement");
        }

        // RÈGLE 2 : Date de début dans le futur
        if (event.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date de début doit être dans le futur");
        }

        // RÈGLE 3 : Date de fin après date de début
        if (event.getDateFin().isBefore(event.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }

        // RÈGLE 4 : Capacité > 0
        if (event.getCapaciteMax() <= 0) {
            throw new BadRequestException("La capacité doit être supérieure à 0");
        }

        // RÈGLE 5 : Prix >= 0
        if (event.getPrixUnitaire() < 0) {
            throw new BadRequestException("Le prix doit être supérieur ou égal à 0");
        }

        event.setOrganisateur(organisateur);
        event.setStatut(EventStatus.BROUILLON);

        return eventRepository.save(event);
    }

    @Override
    public Event updateEvent(Long id, Event updatedEvent, Long utilisateurId) {
        Event event = findById(id);
        User utilisateur = userService.findById(utilisateurId);

        // RÈGLE 6 : Seulement le créateur ou ADMIN peuvent modifier
        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour modifier cet événement");
        }

        // RÈGLE 7 : Un événement terminé ne peut plus être modifié
        if (event.getStatut() == EventStatus.TERMINE) {
            throw new BusinessException("Un événement terminé ne peut pas être modifié");
        }

        // Validations des dates
        if (updatedEvent.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date de début doit être dans le futur");
        }

        if (updatedEvent.getDateFin().isBefore(updatedEvent.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }

        // Mise à jour des champs
        event.setTitre(updatedEvent.getTitre());
        event.setDescription(updatedEvent.getDescription());
        event.setCategorie(updatedEvent.getCategorie());
        event.setDateDebut(updatedEvent.getDateDebut());
        event.setDateFin(updatedEvent.getDateFin());
        event.setLieu(updatedEvent.getLieu());
        event.setVille(updatedEvent.getVille());
        event.setCapaciteMax(updatedEvent.getCapaciteMax());
        event.setPrixUnitaire(updatedEvent.getPrixUnitaire());
        event.setImageUrl(updatedEvent.getImageUrl());

        return eventRepository.save(event);
    }

    @Override
    public Event publierEvent(Long id, Long utilisateurId) {
        Event event = findById(id);
        User utilisateur = userService.findById(utilisateurId);

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour publier cet événement");
        }

        // RÈGLE 8 : Un événement ne peut être publié que s'il a toutes les informations requises
        if (event.getTitre() == null || event.getTitre().isBlank() ||
                event.getDateDebut() == null || event.getDateFin() == null ||
                event.getLieu() == null || event.getLieu().isBlank() ||
                event.getVille() == null || event.getVille().isBlank() ||
                event.getCapaciteMax() == null || event.getPrixUnitaire() == null) {
            throw new BusinessException("Toutes les informations obligatoires doivent être renseignées pour publier");
        }

        event.setStatut(EventStatus.PUBLIE);
        return eventRepository.save(event);
    }

    @Override
    public void annulerEvent(Long id, Long utilisateurId) {
        Event event = findById(id);
        User utilisateur = userService.findById(utilisateurId);

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour annuler cet événement");
        }

        event.setStatut(EventStatus.ANNULE);
        eventRepository.save(event);

        // TODO : Gérer l'annulation automatique des réservations existantes
    }

    @Override
    public void deleteEvent(Long id, Long utilisateurId) {
        Event event = findById(id);
        User utilisateur = userService.findById(utilisateurId);

        // Vérification des droits
        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour supprimer cet événement");
        }

        // RÈGLE 9 : Un événement ne peut être supprimé que s'il n'y a aucune réservation
        long nbReservations = reservationRepository.countByEvenement(event);
        if (nbReservations > 0) {
            throw new BusinessException("Impossible de supprimer un événement avec des réservations");
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé avec l'ID : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByOrganisateur(Long organisateurId) {
        User organisateur = userService.findById(organisateurId);
        return eventRepository.findByOrganisateur(organisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByCategorie(EventCategory categorie) {
        return eventRepository.findByCategorie(categorie);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findByStatut(EventStatus statut) {
        return eventRepository.findByStatut(statut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findAvailableEvents() {
        return eventRepository.findAvailableEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> findPopularEvents() {
        return eventRepository.findPopularEvents();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> searchEvents(EventCategory categorie, LocalDateTime dateDebut,
                                    LocalDateTime dateFin, String ville,
                                    Double prixMin, Double prixMax) {
        // Utilisation des Streams Java pour filtrer
        return eventRepository.findAll().stream()
                .filter(e -> categorie == null || e.getCategorie().equals(categorie))
                .filter(e -> dateDebut == null || e.getDateDebut().isAfter(dateDebut))
                .filter(e -> dateFin == null || e.getDateDebut().isBefore(dateFin))
                .filter(e -> ville == null || e.getVille().equalsIgnoreCase(ville))
                .filter(e -> prixMin == null || e.getPrixUnitaire() >= prixMin)
                .filter(e -> prixMax == null || e.getPrixUnitaire() <= prixMax)
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Event> searchByTitre(String keyword) {
        return eventRepository.searchByTitre(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public int getPlacesDisponibles(Long eventId) {
        Event event = findById(eventId);
        Integer placesReservees = reservationRepository.countTotalPlacesReserveesForEvent(eventId);
        return event.getCapaciteMax() - placesReservees;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesOrganisateur(Long organisateurId) {
        User organisateur = userService.findById(organisateurId);
        Map<String, Object> stats = new HashMap<>();

        List<Event> events = eventRepository.findByOrganisateur(organisateur);

        // Nombre total d'événements
        stats.put("nombreEvenements", events.size());

        // Par statut (utilisation des Streams Java)
        long nbBrouillon = events.stream()
                .filter(e -> e.getStatut() == EventStatus.BROUILLON)
                .count();
        long nbPublie = events.stream()
                .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                .count();
        long nbAnnule = events.stream()
                .filter(e -> e.getStatut() == EventStatus.ANNULE)
                .count();
        long nbTermine = events.stream()
                .filter(e -> e.getStatut() == EventStatus.TERMINE)
                .count();

        stats.put("nombreBrouillons", nbBrouillon);
        stats.put("nombrePublies", nbPublie);
        stats.put("nombreAnnules", nbAnnule);
        stats.put("nombreTermines", nbTermine);

        // Nombre total de réservations
        long totalReservations = events.stream()
                .mapToLong(e -> reservationRepository.countByEvenement(e))
                .sum();
        stats.put("nombreTotalReservations", totalReservations);

        // Revenu total
        double revenuTotal = events.stream()
                .mapToDouble(e -> reservationRepository.calculateTotalRevenueByEvent(e.getId()))
                .sum();
        stats.put("revenuTotal", revenuTotal);

        return stats;
    }

    @Override
    public void verifierEvenementsTermines() {
        // RÈGLE 10 : Vérification automatique des événements terminés
        // Récupérer tous les événements publiés dont la date de fin est passée
        List<Event> eventsATerminer = eventRepository.findByStatut(EventStatus.PUBLIE).stream()
                .filter(e -> e.getDateFin().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        // Mettre à jour leur statut à TERMINE
        eventsATerminer.forEach(event -> {
            event.setStatut(EventStatus.TERMINE);
            eventRepository.save(event);
        });
    }
}