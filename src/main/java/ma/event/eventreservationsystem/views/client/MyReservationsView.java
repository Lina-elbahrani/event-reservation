package ma.event.eventreservationsystem.views.client;

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
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route("my-reservations")
@PageTitle("Mes Réservations | Event Reservation System")
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;

    // TODO: Récupérer l'utilisateur connecté
    private final Long currentUserId = 1L;

    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);
    private final ComboBox<ReservationStatus> statusFilter = new ComboBox<>("Filtrer par statut");
    private final TextField searchField = new TextField("Rechercher par code");

    public MyReservationsView(@Autowired ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);

        // Titre
        H1 title = new H1("Mes Réservations");
        title.getStyle().set("color", "#1976D2");

        // Filtres
        configureFilters();
        HorizontalLayout filtersLayout = new HorizontalLayout(statusFilter, searchField);
        filtersLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Grille
        configureGrid();

        // Assemblage
        add(title, filtersLayout, grid);

        // Charger les données
        updateList();
    }

    private void configureFilters() {
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());

        searchField.setPlaceholder("EVT-12345");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> updateList());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonne Code
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Événement
        grid.addColumn(res -> res.getEvenement().getTitre())
                .setHeader("Événement")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Date événement
        grid.addColumn(res -> res.getEvenement().getDateDebut().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ))
                .setHeader("Date événement")
                .setSortable(true);

        // Colonne Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setSortable(true);

        // Colonne Montant
        grid.addColumn(res -> res.getMontantTotal() + " DH")
                .setHeader("Montant")
                .setSortable(true);

        // Colonne Statut avec badge coloré
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

        // Colonne Actions
        grid.addComponentColumn(reservation -> {
            HorizontalLayout actions = new HorizontalLayout();

            Button detailsButton = new Button("Détails");
            detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
            detailsButton.addClickListener(e -> showDetails(reservation));

            // Bouton annuler (seulement si possible)
            if (reservation.getStatut() != ReservationStatus.ANNULEE) {
                Button cancelButton = new Button("Annuler");
                cancelButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
                cancelButton.addClickListener(e -> confirmCancellation(reservation));
                actions.add(detailsButton, cancelButton);
            } else {
                actions.add(detailsButton);
            }

            return actions;
        }).setHeader("Actions");
    }

    private void updateList() {
        try {
            List<Reservation> reservations;

            // Recherche par code
            if (!searchField.isEmpty()) {
                try {
                    Reservation res = reservationService.findByCode(searchField.getValue());
                    reservations = List.of(res);
                } catch (Exception e) {
                    reservations = List.of();
                }
            }
            // Filtrage par statut
            else if (statusFilter.getValue() != null) {
                reservations = reservationService.findByUtilisateurAndStatut(
                        currentUserId,
                        statusFilter.getValue()
                );
            }
            // Toutes les réservations
            else {
                reservations = reservationService.findByUtilisateur(currentUserId);
            }

            grid.setItems(reservations);

        } catch (Exception e) {
            showError("Erreur lors du chargement des réservations");
            grid.setItems();
        }
    }

    private void showDetails(Reservation reservation) {
        // TODO: Afficher une dialog avec les détails complets
        Map<String, Object> recap = reservationService.getRecapitulatifReservation(reservation.getId());
        showSuccess("Détails chargés (TODO: afficher dans dialog)");
    }

    private void confirmCancellation(Reservation reservation) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("Confirmer l'annulation");
        dialog.setText(
                "Êtes-vous sûr de vouloir annuler cette réservation ?\n\n" +
                        "Code : " + reservation.getCodeReservation() + "\n" +
                        "Événement : " + reservation.getEvenement().getTitre() + "\n\n" +
                        "Rappel : Les réservations doivent être annulées au moins 48h avant l'événement."
        );

        dialog.setCancelable(true);
        dialog.setConfirmText("Annuler la réservation");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> cancelReservation(reservation));

        dialog.open();
    }

    private void cancelReservation(Reservation reservation) {
        try {
            reservationService.annulerReservation(reservation.getId(), currentUserId);
            showSuccess("Réservation annulée avec succès");
            updateList();
        } catch (Exception e) {
            showError(e.getMessage());
        }
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