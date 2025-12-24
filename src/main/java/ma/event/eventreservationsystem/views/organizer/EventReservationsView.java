package ma.event.eventreservationsystem.views.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.*;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.EventService;
import ma.event.eventreservationsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("organizer/event/:id/reservations")
@PageTitle("Réservations de l'événement | Event Reservation System")
public class EventReservationsView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventService eventService;
    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    private Event event;
    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);

    private final ComboBox<ReservationStatus> statusFilter = new ComboBox<>("Filtrer par statut");
    private final TextField searchField = new TextField("Rechercher");

    public EventReservationsView(
            @Autowired EventService eventService,
            @Autowired ReservationService reservationService,
            @Autowired ReservationRepository reservationRepository
    ) {
        this.eventService = eventService;
        this.reservationService = reservationService;
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(true);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        try {
            event = eventService.findById(eventId);
            displayReservations();
        } catch (Exception e) {
            getUI().ifPresent(ui -> ui.navigate("organizer/events"));
        }
    }

    private void displayReservations() {
        removeAll();

        // Bouton retour
        Button backButton = new Button("← Retour à mes événements", e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/events"))
        );

        // Titre
        H1 title = new H1("Réservations - " + event.getTitre());
        title.getStyle().set("color", "#1976D2");

        // Statistiques
        VerticalLayout statsSection = createStatsSection();

        // Filtres
        configureFilters();
        HorizontalLayout filtersLayout = new HorizontalLayout(statusFilter, searchField);
        filtersLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Grille
        configureGrid();
        loadReservations();

        // Assemblage
        add(backButton, title, statsSection, filtersLayout, grid);
    }

    private VerticalLayout createStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H2 statsTitle = new H2("Statistiques");
        statsTitle.getStyle().set("margin", "0 0 15px 0");

        // Récupérer les statistiques
        List<Reservation> allReservations = reservationService.findByEvenement(event.getId());

        long totalReservations = allReservations.size();
        long confirmees = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .count();
        long enAttente = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE)
                .count();
        long annulees = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.ANNULEE)
                .count();

        Integer placesReservees = reservationRepository.countTotalPlacesReserveesForEvent(event.getId());
        Double revenu = reservationRepository.calculateTotalRevenueByEvent(event.getId());

        // Taux de remplissage
        double tauxRemplissage = (double) placesReservees / event.getCapaciteMax() * 100;

        // Affichage
        HorizontalLayout statsRow1 = new HorizontalLayout();
        statsRow1.setWidthFull();
        statsRow1.setSpacing(true);

        statsRow1.add(
                createStatBadge("📝 Total réservations", String.valueOf(totalReservations), "#1976D2"),
                createStatBadge("✅ Confirmées", String.valueOf(confirmees), "#28A745"),
                createStatBadge("⏳ En attente", String.valueOf(enAttente), "#FFA500"),
                createStatBadge("❌ Annulées", String.valueOf(annulees), "#DC3545")
        );

        HorizontalLayout statsRow2 = new HorizontalLayout();
        statsRow2.setWidthFull();
        statsRow2.setSpacing(true);

        statsRow2.add(
                createStatBadge("🎫 Places réservées",
                        placesReservees + " / " + event.getCapaciteMax(), "#9C27B0"),
                createStatBadge("📊 Taux remplissage",
                        String.format("%.1f%%", tauxRemplissage), "#FF5722"),
                createStatBadge("💰 Revenus",
                        String.format("%.2f DH", revenu), "#28A745")
        );

        section.add(statsTitle, statsRow1, statsRow2);

        return section;
    }

    private VerticalLayout createStatBadge(String label, String value, String color) {
        VerticalLayout badge = new VerticalLayout();
        badge.setPadding(true);
        badge.setSpacing(false);
        badge.getStyle()
                .set("background", "#F5F5F5")
                .set("border-radius", "8px")
                .set("border-left", "4px solid " + color);

        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-size", "0.85em").set("color", "#666");

        Span valueSpan = new Span(value);
        valueSpan.getStyle()
                .set("font-size", "1.3em")
                .set("font-weight", "bold")
                .set("color", color);

        badge.add(labelSpan, valueSpan);

        return badge;
    }

    private void configureFilters() {
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> loadReservations());

        searchField.setPlaceholder("Nom, prénom ou code...");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> loadReservations());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonne Code
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Client
        grid.addColumn(res ->
                        res.getUtilisateur().getPrenom() + " " + res.getUtilisateur().getNom()
                )
                .setHeader("Client")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Email
        grid.addColumn(res -> res.getUtilisateur().getEmail())
                .setHeader("Email")
                .setSortable(true);

        // Colonne Date réservation
        grid.addColumn(res -> res.getDateReservation().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ))
                .setHeader("Date réservation")
                .setSortable(true);

        // Colonne Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setSortable(true);

        // Colonne Montant
        grid.addColumn(res -> res.getMontantTotal() + " DH")
                .setHeader("Montant")
                .setSortable(true);

        // Colonne Statut avec badge
        grid.addComponentColumn(reservation -> {
            Span badge = new Span(reservation.getStatut().getLabel());
            String color = switch (reservation.getStatut()) {
                case CONFIRMEE -> "#28A745";
                case EN_ATTENTE -> "#FFA500";
                case ANNULEE -> "#DC3545";
            };
            badge.getStyle()
                    .set("background", color)
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        }).setHeader("Statut");
    }

    private void loadReservations() {
        try {
            List<Reservation> reservations = reservationService.findByEvenement(event.getId());

            // Filtrage par statut
            if (statusFilter.getValue() != null) {
                reservations = reservations.stream()
                        .filter(r -> r.getStatut() == statusFilter.getValue())
                        .toList();
            }

            // Recherche par nom/code
            if (!searchField.isEmpty()) {
                String search = searchField.getValue().toLowerCase();
                reservations = reservations.stream()
                        .filter(r ->
                                r.getCodeReservation().toLowerCase().contains(search) ||
                                        r.getUtilisateur().getNom().toLowerCase().contains(search) ||
                                        r.getUtilisateur().getPrenom().toLowerCase().contains(search)
                        )
                        .toList();
            }

            grid.setItems(reservations);

        } catch (Exception e) {
            grid.setItems();
        }
    }
}