package ma.event.eventreservationsystem.views.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.progressbar.ProgressBar;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.EventService;
import ma.event.eventreservationsystem.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Route("organizer/events")
@PageTitle("Mes Événements | Event Reservation System")
@RolesAllowed({"ORGANIZER", "ADMIN"})
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationRepository reservationRepository;
    private final UserService userService;

    private final Grid<Event> grid = new Grid<>(Event.class, false);
    private final ComboBox<EventStatus> statusFilter = new ComboBox<>("Filtrer par statut");

    // On ne stocke plus l'ID en dur ici !

    public MyEventsView(EventService eventService,
                        ReservationRepository reservationRepository,
                        UserService userService) { // On injecte UserService
        this.eventService = eventService;
        this.reservationRepository = reservationRepository;
        this.userService = userService;

        setSizeFull();
        setPadding(true);

        // --- Header ---
        HorizontalLayout header = new HorizontalLayout();
        header.setWidthFull();
        header.setJustifyContentMode(JustifyContentMode.BETWEEN);
        header.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        H1 title = new H1("Mes Événements");
        title.getStyle().set("color", "#1976D2").set("margin", "0");

        Button createButton = new Button("➕ Créer un événement", e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/event/new"))
        );
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        header.add(title, createButton);

        // --- Filtres & Grille ---
        configureFilter();
        configureGrid();

        add(header, statusFilter, grid);

        // --- Chargement des données ---
        updateList();
    }

    // --- LE CŒUR DE LA CORRECTION EST ICI ---
    private void updateList() {
        // 1. On récupère l'utilisateur connecté dynamiquement
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            System.out.println("Erreur : Utilisateur non connecté");
            return;
        }

        // 2. On utilise SON ID (donc 3 pour Fatima)
        try {
            List<Event> events;
            if (statusFilter.getValue() != null) {
                events = eventService.findByOrganisateur(currentUser.getId()).stream()
                        .filter(e -> e.getStatut() == statusFilter.getValue())
                        .collect(Collectors.toList());
            } else {
                events = eventService.findByOrganisateur(currentUser.getId());
            }
            grid.setItems(events);
        } catch (Exception e) {
            showError("Erreur : " + e.getMessage());
        }
    }

    // Méthode utilitaire pour récupérer Fatima (ID 3)
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            return userService.findByEmail(auth.getName());
        }
        return null;
    }
    // ----------------------------------------

    private void configureFilter() {
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());
    }

    private void configureGrid() {
        grid.setSizeFull();

        grid.addColumn(Event::getTitre).setHeader("Titre").setSortable(true).setAutoWidth(true);
        grid.addColumn(event -> event.getCategorie().getLabel()).setHeader("Catégorie").setSortable(true);
        grid.addColumn(event -> event.getDateDebut().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))).setHeader("Date").setSortable(true);

        grid.addComponentColumn(event -> {
            Span badge = new Span(event.getStatut().getLabel());
            String color = switch (event.getStatut()) {
                case PUBLIE -> "#28A745";
                case BROUILLON -> "#FFA500";
                case ANNULE -> "#DC3545";
                case TERMINE -> "#6C757D";
            };
            badge.getStyle().set("background", color).set("color", "white").set("padding", "5px 10px").set("border-radius", "12px").set("font-size", "0.85em");
            return badge;
        }).setHeader("Statut");

        grid.addComponentColumn(this::createFillRateComponent).setHeader("Taux remplissage").setAutoWidth(true);
        grid.addComponentColumn(this::createActionsLayout).setHeader("Actions").setAutoWidth(true);
    }

    private VerticalLayout createFillRateComponent(Event event) {
        Integer placesReservees = reservationRepository.countTotalPlacesReserveesForEvent(event.getId());
        if (placesReservees == null) placesReservees = 0;
        int capacite = event.getCapaciteMax();
        double fillRate = capacite > 0 ? (double) placesReservees / capacite : 0;

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(fillRate);
        progressBar.setWidth("100px");
        String color = fillRate >= 0.8 ? "#28A745" : fillRate >= 0.5 ? "#FFA500" : "#DC3545";
        progressBar.getStyle().set("--lumo-primary-color", color);

        Span text = new Span(placesReservees + " / " + capacite);
        text.getStyle().set("font-size", "0.85em");
        return new VerticalLayout(progressBar, text);
    }

    private HorizontalLayout createActionsLayout(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);
        User currentUser = getCurrentUser(); // Nécessaire pour les actions

        Button viewButton = new Button("👁️", e -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        actions.add(viewButton);

        // On vérifie que currentUser n'est pas null avant de passer l'ID
        if (currentUser != null) {
            if (event.getStatut() != EventStatus.TERMINE) {
                Button editButton = new Button("✏️", e -> getUI().ifPresent(ui -> ui.navigate("organizer/event/edit/" + event.getId())));
                editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
                actions.add(editButton);
            }
            if (event.getStatut() == EventStatus.BROUILLON) {
                Button publishButton = new Button("✅", e -> publierEvent(event, currentUser.getId()));
                publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                actions.add(publishButton);
            }
            if (event.getStatut() == EventStatus.PUBLIE) {
                Button cancelButton = new Button("❌", e -> confirmCancelEvent(event, currentUser.getId()));
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                actions.add(cancelButton);
            }
            long nbReservations = reservationRepository.countByEvenement(event);
            if (nbReservations == 0) {
                Button deleteButton = new Button("🗑️", e -> confirmDeleteEvent(event, currentUser.getId()));
                deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                actions.add(deleteButton);
            }
        }
        return actions;
    }

    private void publierEvent(Event event, Long userId) {
        try {
            eventService.publierEvent(event.getId(), userId);
            showSuccess("Événement publié avec succès");
            updateList();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void confirmCancelEvent(Event event, Long userId) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Annuler ?");
        dialog.setText("Êtes-vous sûr ?");
        dialog.setConfirmText("Oui");
        dialog.addConfirmListener(e -> {
            try {
                eventService.annulerEvent(event.getId(), userId);
                showSuccess("Annulé");
                updateList();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        dialog.open();
    }

    private void confirmDeleteEvent(Event event, Long userId) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Supprimer ?");
        dialog.setText("Irréversible.");
        dialog.setConfirmText("Oui");
        dialog.addConfirmListener(e -> {
            try {
                eventService.deleteEvent(event.getId(), userId);
                showSuccess("Supprimé");
                updateList();
            } catch (Exception ex) { showError(ex.getMessage()); }
        });
        dialog.open();
    }

    private void showError(String message) {
        Notification.show(message, 4000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification.show(message, 3000, Notification.Position.TOP_CENTER).addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}