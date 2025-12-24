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
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("organizer/events")
@PageTitle("Mes Événements | Event Reservation System")
public class MyEventsView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationRepository reservationRepository;

    // TODO: Récupérer de la session
    private final Long currentUserId = 1L;

    private final Grid<Event> grid = new Grid<>(Event.class, false);
    private final ComboBox<EventStatus> statusFilter = new ComboBox<>("Filtrer par statut");

    public MyEventsView(
            @Autowired EventService eventService,
            @Autowired ReservationRepository reservationRepository
    ) {
        this.eventService = eventService;
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(true);

        // Titre et bouton créer
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

        // Filtre
        configureFilter();

        // Grille
        configureGrid();

        // Assemblage
        add(header, statusFilter, grid);

        // Charger les données
        updateList();
    }

    private void configureFilter() {
        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonne Titre
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Catégorie
        grid.addColumn(event -> event.getCategorie().getLabel())
                .setHeader("Catégorie")
                .setSortable(true);

        // Colonne Date
        grid.addColumn(event -> event.getDateDebut().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ))
                .setHeader("Date")
                .setSortable(true);

        // Colonne Statut avec badge
        grid.addComponentColumn(event -> {
            Span badge = new Span(event.getStatut().getLabel());
            String color = switch (event.getStatut()) {
                case PUBLIE -> "#28A745";
                case BROUILLON -> "#FFA500";
                case ANNULE -> "#DC3545";
                case TERMINE -> "#6C757D";
            };
            badge.getStyle()
                    .set("background", color)
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        }).setHeader("Statut");

        // Colonne Taux de remplissage
        grid.addComponentColumn(event -> createFillRateComponent(event))
                .setHeader("Taux remplissage")
                .setAutoWidth(true);

        // Colonne Actions
        grid.addComponentColumn(event -> createActionsLayout(event))
                .setHeader("Actions")
                .setAutoWidth(true);
    }

    private VerticalLayout createFillRateComponent(Event event) {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);
        layout.setSpacing(false);

        Integer placesReservees = reservationRepository.countTotalPlacesReserveesForEvent(event.getId());
        int capacite = event.getCapaciteMax();
        double fillRate = (double) placesReservees / capacite;

        ProgressBar progressBar = new ProgressBar();
        progressBar.setValue(fillRate);
        progressBar.setWidth("100px");

        // Couleur selon le taux
        String color = fillRate >= 0.8 ? "#28A745" : fillRate >= 0.5 ? "#FFA500" : "#DC3545";
        progressBar.getStyle().set("--lumo-primary-color", color);

        Span text = new Span(placesReservees + " / " + capacite);
        text.getStyle().set("font-size", "0.85em");

        layout.add(progressBar, text);

        return layout;
    }

    private HorizontalLayout createActionsLayout(Event event) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Bouton Voir
        Button viewButton = new Button("👁️");
        viewButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        viewButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("event/" + event.getId()))
        );

        // Bouton Modifier (seulement si pas terminé)
        if (event.getStatut() != EventStatus.TERMINE) {
            Button editButton = new Button("✏️");
            editButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            editButton.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate("organizer/event/edit/" + event.getId()))
            );
            actions.add(editButton);
        }

        // Bouton Publier (seulement si brouillon)
        if (event.getStatut() == EventStatus.BROUILLON) {
            Button publishButton = new Button("✅");
            publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            publishButton.addClickListener(e -> publierEvent(event));
            actions.add(publishButton);
        }

        // Bouton Réservations
        Button reservationsButton = new Button("🎫");
        reservationsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        reservationsButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/event/" + event.getId() + "/reservations"))
        );

        // Bouton Annuler (seulement si publié)
        if (event.getStatut() == EventStatus.PUBLIE) {
            Button cancelButton = new Button("❌");
            cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            cancelButton.addClickListener(e -> confirmCancelEvent(event));
            actions.add(cancelButton);
        }

        // Bouton Supprimer (seulement si pas de réservations)
        long nbReservations = reservationRepository.countByEvenement(event);
        if (nbReservations == 0) {
            Button deleteButton = new Button("🗑️");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteEvent(event));
            actions.add(deleteButton);
        }

        actions.add(viewButton, reservationsButton);

        return actions;
    }

    private void updateList() {
        try {
            List<Event> events;

            if (statusFilter.getValue() != null) {
                events = eventService.findByOrganisateur(currentUserId).stream()
                        .filter(e -> e.getStatut() == statusFilter.getValue())
                        .toList();
            } else {
                events = eventService.findByOrganisateur(currentUserId);
            }

            grid.setItems(events);

        } catch (Exception e) {
            showError("Erreur lors du chargement des événements");
            grid.setItems();
        }
    }

    private void publierEvent(Event event) {
        try {
            eventService.publierEvent(event.getId(), currentUserId);
            showSuccess("Événement publié avec succès");
            updateList();
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void confirmCancelEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer l'annulation");
        dialog.setText(
                "Êtes-vous sûr de vouloir annuler cet événement ?\n\n" +
                        event.getTitre() + "\n\n" +
                        "Les utilisateurs ayant réservé seront notifiés."
        );

        dialog.setCancelable(true);
        dialog.setConfirmText("Annuler l'événement");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                eventService.annulerEvent(event.getId(), currentUserId);
                showSuccess("Événement annulé");
                updateList();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        dialog.open();
    }

    private void confirmDeleteEvent(Event event) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer la suppression");
        dialog.setText(
                "Êtes-vous sûr de vouloir supprimer définitivement cet événement ?\n\n" +
                        event.getTitre() + "\n\n" +
                        "Cette action est irréversible."
        );

        dialog.setCancelable(true);
        dialog.setConfirmText("Supprimer");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> {
            try {
                eventService.deleteEvent(event.getId(), currentUserId);
                showSuccess("Événement supprimé");
                updateList();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        dialog.open();
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
