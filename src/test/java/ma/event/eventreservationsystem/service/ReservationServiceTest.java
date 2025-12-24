package ma.event.eventreservationsystem.service;

import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ReservationServiceTest {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private EventService eventService;

    @Autowired
    private UserService userService;

    private User client;
    private Event event;

    @BeforeEach
    void setUp() {
        // Créer un client
        client = User.builder()
                .nom("Client")
                .prenom("Test")
                .email("client." + UUID.randomUUID() + "@test.com")
                .password("password123")
                .role(UserRole.CLIENT)
                .build();
        client = userService.inscription(client);

        // Créer un organisateur
        User organizer = User.builder()
                .nom("Organizer")
                .prenom("Test")
                .email("org." + UUID.randomUUID() + "@test.com")
                .password("password123")
                .role(UserRole.ORGANIZER)
                .build();
        organizer = userService.inscription(organizer);

        // Créer et publier un événement
        event = Event.builder()
                .titre("Concert Test Réservation")
                .description("Test")
                .categorie(EventCategory.CONCERT)
                .dateDebut(LocalDateTime.now().plusDays(10))
                .dateFin(LocalDateTime.now().plusDays(10).plusHours(3))
                .lieu("Salle")
                .ville("Casablanca")
                .capaciteMax(100)
                .prixUnitaire(50.0)
                .build();

        event = eventService.createEvent(event, organizer.getId());
        event = eventService.publierEvent(event.getId(), organizer.getId());
    }

    // ============================================
    // TESTS DE CRÉATION DE RÉSERVATION
    // ============================================

    @Test
    void testCreateReservation_Success() {
        // ARRANGE
        Reservation reservation = Reservation.builder()
                .nombrePlaces(2)
                .commentaire("Places VIP")
                .build();

        // ACT
        Reservation saved = reservationService.createReservation(
                reservation,
                client.getId(),
                event.getId()
        );

        // ASSERT
        assertNotNull(saved.getId());
        assertEquals(2, saved.getNombrePlaces());
        assertEquals(100.0, saved.getMontantTotal()); // 2 × 50
        assertEquals(ReservationStatus.EN_ATTENTE, saved.getStatut());
        assertNotNull(saved.getCodeReservation());
        assertTrue(saved.getCodeReservation().startsWith("EVT-"));
    }

    @Test
    void testCreateReservation_Maximum10Places() {
        // ARRANGE
        Reservation reservation = Reservation.builder()
                .nombrePlaces(11)  // Plus de 10 !
                .build();

        // ACT & ASSERT
        assertThrows(BusinessException.class, () -> {
            reservationService.createReservation(reservation, client.getId(), event.getId());
        }, "Maximum 10 places par réservation");
    }

    @Test
    void testCreateReservation_CalculMontantTotal() {
        // ARRANGE
        Reservation reservation = Reservation.builder()
                .nombrePlaces(5)
                .build();

        // ACT
        Reservation saved = reservationService.createReservation(
                reservation,
                client.getId(),
                event.getId()
        );

        // ASSERT
        assertEquals(250.0, saved.getMontantTotal()); // 5 × 50
    }

    @Test
    void testCreateReservation_CodeUnique() {
        // ARRANGE
        Reservation res1 = Reservation.builder().nombrePlaces(1).build();
        Reservation res2 = Reservation.builder().nombrePlaces(1).build();

        // ACT
        Reservation saved1 = reservationService.createReservation(res1, client.getId(), event.getId());
        Reservation saved2 = reservationService.createReservation(res2, client.getId(), event.getId());

        // ASSERT
        assertNotEquals(saved1.getCodeReservation(), saved2.getCodeReservation(),
                "Les codes de réservation doivent être uniques");
    }
}