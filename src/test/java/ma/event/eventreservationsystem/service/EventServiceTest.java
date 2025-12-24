package ma.event.eventreservationsystem.service;

import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.exception.BadRequestException;
import ma.event.eventreservationsystem.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class EventServiceTest {

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    private User organizer;
    private User client;

    @BeforeEach
    void setUp() {
        // Créer un organisateur pour les tests
        organizer = User.builder()
                .nom("Organizer")
                .prenom("Test")
                .email("organizer." + UUID.randomUUID() + "@test.com")
                .password("password123")
                .role(UserRole.ORGANIZER)
                .build();
        organizer = userService.inscription(organizer);

        // Créer un client
        client = User.builder()
                .nom("Client")
                .prenom("Test")
                .email("client." + UUID.randomUUID() + "@test.com")
                .password("password123")
                .role(UserRole.CLIENT)
                .build();
        client = userService.inscription(client);
    }

    // ============================================
    // TESTS DE CRÉATION D'ÉVÉNEMENT
    // ============================================

    @Test
    void testCreateEvent_Success() {
        // ARRANGE
        Event event = Event.builder()
                .titre("Concert Test")
                .description("Description du concert test")
                .categorie(EventCategory.CONCERT)
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(10).plusHours(3))
                .lieu("Salle de Concert")
                .ville("Casablanca")
                .capaciteMax(500)
                .prixUnitaire(150.0)
                .build();

        // ACT
        Event saved = eventService.createEvent(event, organizer.getId());

        // ASSERT
        assertNotNull(saved.getId());
        assertEquals(EventStatus.BROUILLON, saved.getStatut());
        assertEquals("Concert Test", saved.getTitre());
        assertNotNull(saved.getDateCreation());
        assertEquals(organizer.getId(), saved.getOrganisateur().getId());
    }

    @Test
    void testCreateEvent_ClientNePeutPas() {
        // ARRANGE
        Event event = Event.builder()
                .titre("Concert Test")
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(10).plusHours(3))
                .lieu("Salle")
                .ville("Casablanca")
                .capaciteMax(100)
                .prixUnitaire(50.0)
                .categorie(EventCategory.CONCERT)
                .build();

        // ACT & ASSERT
        assertThrows(ForbiddenException.class, () -> {
            eventService.createEvent(event, client.getId());
        }, "Un CLIENT ne peut pas créer d'événement");
    }

    @Test
    void testCreateEvent_DateDansLePassé() {
        // ARRANGE
        Event event = Event.builder()
                .titre("Concert Test")
                .dateDebut(LocalDateTime.now().minusDays(1))  // Date passée !
                .dateFin(LocalDateTime.now())
                .lieu("Salle")
                .ville("Casablanca")
                .capaciteMax(100)
                .prixUnitaire(50.0)
                .categorie(EventCategory.CONCERT)
                .build();

        // ACT & ASSERT
        assertThrows(BadRequestException.class, () -> {
            eventService.createEvent(event, organizer.getId());
        }, "La date de début doit être dans le futur");
    }

    @Test
    void testCreateEvent_DateFinAvantDateDebut() {
        // ARRANGE
        Event event = Event.builder()
                .titre("Concert Test")
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(5))  // Avant date début !
                .lieu("Salle")
                .ville("Casablanca")
                .capaciteMax(100)
                .prixUnitaire(50.0)
                .categorie(EventCategory.CONCERT)
                .build();

        // ACT & ASSERT
        assertThrows(BadRequestException.class, () -> {
            eventService.createEvent(event, organizer.getId());
        }, "La date de fin doit être après la date de début");
    }

    // ============================================
    // TESTS DE PUBLICATION
    // ============================================

    @Test
    void testPublierEvent_Success() {
        // ARRANGE - Créer un événement complet
        Event event = Event.builder()
                .titre("Concert à publier")
                .description("Description complète")
                .categorie(EventCategory.CONCERT)
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(10).plusHours(3))
                .lieu("Salle de Concert")
                .ville("Casablanca")
                .capaciteMax(500)
                .prixUnitaire(150.0)
                .build();

        Event saved = eventService.createEvent(event, organizer.getId());

        // ACT
        Event published = eventService.publierEvent(saved.getId(), organizer.getId());

        // ASSERT
        assertEquals(EventStatus.PUBLIE, published.getStatut());
    }

    // ============================================
    // TESTS DE RECHERCHE
    // ============================================

    @Test
    void testGetPlacesDisponibles() {
        // ARRANGE
        Event event = Event.builder()
                .titre("Concert Test Capacité")
                .description("Test")
                .categorie(EventCategory.CONCERT)
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(10).plusHours(3))
                .lieu("Salle")
                .ville("Casablanca")
                .capaciteMax(100)
                .prixUnitaire(50.0)
                .build();

        Event saved = eventService.createEvent(event, organizer.getId());

        // ACT
        int placesDisponibles = eventService.getPlacesDisponibles(saved.getId());

        // ASSERT
        assertEquals(100, placesDisponibles, "Toutes les places doivent être disponibles");
    }
}