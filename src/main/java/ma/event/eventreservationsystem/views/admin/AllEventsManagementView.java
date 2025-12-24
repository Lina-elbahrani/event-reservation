package ma.event.eventreservationsystem.views.admin;

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
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("admin/events")
@PageTitle("Gestion des Événements | Event Reservation System")
public class AllEventsManagementView extends VerticalLayout {

    private final EventService eventService;
    private final ReservationRepository reservationRepository;

    // Admin peut tout modifier
    private final Long currentUserId = 1L; // TODO: Session

    private final Grid<Event> grid = new Grid<>(Event.class, false);
    private final ComboBox<EventCategory> categoryFilter = new ComboBox<>("Catégorie");
    private final ComboBox<EventStatus> statusFilter = new ComboBox<>("Statut");
    private final TextField searchField = new TextField("Rechercher");

    public AllEventsManagementView(
            @Autowired EventService eventService,
            @Autowired ReservationRepository reservationRepository
    ) {
        this.eventService = eventService;
        this.reservationRepository = reservationRepository;

        setSizeFull();
        setPadding(true);

        // Titre
        H1 title = new H1("Gestion de Tous les Événements");
        title.getStyle().set("color", "#1976D2");

        // Filtres
        configureFilters();
        HorizontalLayout filtersLayout = new HorizontalLayout(categoryFilter, statusFilter, searchField);
        filtersLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Grille
        configureGrid();

        // Assemblage
        add(title, filtersLayout, grid);

        // Charger les données
        updateList();
    }

    private void configureFilters() {
        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(EventCategory::getLabel);
        categoryFilter.setClearButtonVisible(true);
        categoryFilter.addValueChangeListener(e -> updateList());

        statusFilter.setItems(EventStatus.values());
        statusFilter.setItemLabelGenerator(EventStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());

        searchField.setPlaceholder("Titre, ville...");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> updateList());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonne Titre
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Organisateur
        grid.addColumn(event ->
                        event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom()
                )
                .setHeader("Organisateur")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Catégorie
        grid.addColumn(event -> event.getCategorie().getLabel())
                .setHeader("Catégorie")
                .setSortable(true);

        // Colonne Date
        grid.addColumn(event -> event.getDateDebut().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ))
                .setHeader("Date")
                .setSortable(true);

        // Colonne Ville
        grid.addColumn(Event::getVille)
                .setHeader("Ville")
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

        // Colonne Réservations
        grid.addColumn(event -> {
                    long nbRes = reservationRepository.countByEvenement(event);
                    return nbRes + " réservation(s)";
                })
                .setHeader("Réservations")
                .setSortable(false);

        // Colonne Actions
        grid.addComponentColumn(event -> createActionsLayout(event))
                .setHeader("Actions")
                .setAutoWidth(true);
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

        // Bouton Publier (si brouillon)
        if (event.getStatut() == EventStatus.BROUILLON) {
            Button publishButton = new Button("✅");
            publishButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            publishButton.addClickListener(e -> publierEvent(event));
            actions.add(publishButton);
        }

        // Bouton Annuler (si publié)
        if (event.getStatut() == EventStatus.PUBLIE) {
            Button cancelButton = new Button("❌");
            cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            cancelButton.addClickListener(e -> confirmCancelEvent(event));
            actions.add(cancelButton);
        }

        // Bouton Supprimer (si pas de réservations)
        long nbReservations = reservationRepository.countByEvenement(event);
        if (nbReservations == 0) {
            Button deleteButton = new Button("🗑️");
            deleteButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deleteButton.addClickListener(e -> confirmDeleteEvent(event));
            actions.add(deleteButton);
        }

        // Bouton Réservations
        Button reservationsButton = new Button("🎫");
        reservationsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        reservationsButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/event/" + event.getId() + "/reservations"))
        );

        actions.add(viewButton, reservationsButton);

        return actions;
    }

    private void updateList() {
        try {
            List<Event> events = eventService.findAll();

            // Filtrage par catégorie
            if (categoryFilter.getValue() != null) {
                events = events.stream()
                        .filter(e -> e.getCategorie() == categoryFilter.getValue())
                        .toList();
            }

            // Filtrage par statut
            if (statusFilter.getValue() != null) {
                events = events.stream()
                        .filter(e -> e.getStatut() == statusFilter.getValue())
                        .toList();
            }

            // Recherche
            if (!searchField.isEmpty()) {
                String search = searchField.getValue().toLowerCase();
                events = events.stream()
                        .filter(e ->
                                e.getTitre().toLowerCase().contains(search) ||
                                        e.getVille().toLowerCase().contains(search)
                        )
                        .toList();
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
        dialog.setText("Êtes-vous sûr de vouloir annuler cet événement ?\n\n" + event.getTitre());

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

