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
    @Transactional(readOnly = true)
    public List<Event> findAll() {
        return eventRepository.findAllWithOrganisateur();
    }

    @Override
    public Event createEvent(Event event, Long organisateurId) {
        User organisateur = userService.findById(organisateurId);

        if (organisateur.getRole() != UserRole.ADMIN &&
                organisateur.getRole() != UserRole.ORGANIZER) {
            throw new ForbiddenException("Vous n'avez pas les droits pour créer un événement");
        }
        if (event.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date de début doit être dans le futur");
        }
        if (event.getDateFin().isBefore(event.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }
        if (event.getCapaciteMax() <= 0) {
            throw new BadRequestException("La capacité doit être supérieure à 0");
        }
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

        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour modifier cet événement");
        }

        if (event.getStatut() == EventStatus.TERMINE) {
            throw new BusinessException("Un événement terminé ne peut pas être modifié");
        }

        if (updatedEvent.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La date de début doit être dans le futur");
        }

        if (updatedEvent.getDateFin().isBefore(updatedEvent.getDateDebut())) {
            throw new BadRequestException("La date de fin doit être après la date de début");
        }

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

        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour publier cet événement");
        }

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

        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour annuler cet événement");
        }

        event.setStatut(EventStatus.ANNULE);
        eventRepository.save(event);
    }

    @Override
    public void deleteEvent(Long id, Long utilisateurId) {
        Event event = findById(id);
        User utilisateur = userService.findById(utilisateurId);

        if (!event.getOrganisateur().getId().equals(utilisateurId) &&
                utilisateur.getRole() != UserRole.ADMIN) {
            throw new ForbiddenException("Vous n'avez pas les droits pour supprimer cet événement");
        }

        long nbReservations = reservationRepository.countByEvenement(event);
        if (nbReservations > 0) {
            throw new BusinessException("Impossible de supprimer un événement avec des réservations");
        }

        eventRepository.delete(event);
    }

    @Override
    @Transactional(readOnly = true)
    public Event findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Événement non trouvé avec l'ID : " + id));

        // SOLUTION : Forcer le chargement de l'organisateur
        if (event.getOrganisateur() != null) {
            event.getOrganisateur().getPrenom(); // Force le chargement
        }

        return event;
    }

    // 🔥🔥🔥 CORRECTION MAJEURE ICI 🔥🔥🔥
    @Override
    @Transactional(readOnly = true)
    public List<Event> findByOrganisateur(Long organisateurId) {
        // ✅ CORRECTION : On utilise l'ID directement (le chiffre 3)
        // Ne récupérez PAS l'utilisateur avec userService.findById(organisateurId) ici.
        return eventRepository.findByOrganisateurId(organisateurId);
    }
    // ------------------------------------

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
        return eventRepository.findAllWithOrganisateur().stream()
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
        if (keyword == null || keyword.isEmpty()) {
            return eventRepository.findAllWithOrganisateur();
        }
        return eventRepository.searchWithOrganisateur(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public int getPlacesDisponibles(Long eventId) {
        Event event = findById(eventId);
        Integer placesReservees = reservationRepository.countTotalPlacesReserveesForEvent(eventId);
        if (placesReservees == null) placesReservees = 0;
        return event.getCapaciteMax() - placesReservees;
    }

    // 🔥 CORRECTION ICI EGALEMENT POUR LES STATS 🔥
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesOrganisateur(Long organisateurId) {
        Map<String, Object> stats = new HashMap<>();

        // On utilise aussi la méthode par ID ici
        List<Event> events = eventRepository.findByOrganisateurId(organisateurId);

        stats.put("nombreEvenements", events.size());

        long nbBrouillon = events.stream().filter(e -> e.getStatut() == EventStatus.BROUILLON).count();
        long nbPublie = events.stream().filter(e -> e.getStatut() == EventStatus.PUBLIE).count();
        long nbAnnule = events.stream().filter(e -> e.getStatut() == EventStatus.ANNULE).count();
        long nbTermine = events.stream().filter(e -> e.getStatut() == EventStatus.TERMINE).count();

        stats.put("nombreBrouillons", nbBrouillon);
        stats.put("nombrePublies", nbPublie);
        stats.put("nombreAnnules", nbAnnule);
        stats.put("nombreTermines", nbTermine);

        long totalReservations = events.stream()
                .mapToLong(e -> reservationRepository.countByEvenement(e))
                .sum();
        stats.put("nombreTotalReservations", totalReservations);

        double revenuTotal = 0.0;
        try {
            revenuTotal = events.stream()
                    .mapToDouble(e -> {
                        Double rev = reservationRepository.calculateTotalRevenueByEvent(e.getId());
                        return rev != null ? rev : 0.0;
                    })
                    .sum();
        } catch (Exception e) {
            // Ignorer si la méthode n'existe pas encore
        }
        stats.put("revenuTotal", revenuTotal);

        return stats;
    }

    @Override
    public void verifierEvenementsTermines() {
        List<Event> eventsATerminer = eventRepository.findByStatut(EventStatus.PUBLIE).stream()
                .filter(e -> e.getDateFin().isBefore(LocalDateTime.now()))
                .collect(Collectors.toList());

        eventsATerminer.forEach(event -> {
            event.setStatut(EventStatus.TERMINE);
            eventRepository.save(event);
        });
    }
}