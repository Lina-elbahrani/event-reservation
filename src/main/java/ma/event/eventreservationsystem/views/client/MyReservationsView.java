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
import com.vaadin.flow.server.auth.AnonymousAllowed;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.security.SecurityService;
import ma.event.eventreservationsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route("my-reservations")
@PageTitle("Mes Réservations | Event Reservation System")
@AnonymousAllowed  // Permet l'accès sans restriction de rôle
@Transactional
public class MyReservationsView extends VerticalLayout {

    private final ReservationService reservationService;
    private final SecurityService securityService;

    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);
    private final ComboBox<ReservationStatus> statusFilter = new ComboBox<>("Filtrer par statut");
    private final TextField searchField = new TextField("Rechercher par code");

    private User currentUser;

    public MyReservationsView(
            @Autowired ReservationService reservationService,
            @Autowired SecurityService securityService) {

        this.reservationService = reservationService;
        this.securityService = securityService;

        setSizeFull();
        setPadding(true);

        System.out.println("=== MyReservationsView - Initialisation ===");

        // Récupérer l'utilisateur connecté
        try {
            currentUser = securityService.getAuthenticatedUser();
            System.out.println("✅ Utilisateur connecté: " + currentUser.getEmail() + " (ID: " + currentUser.getId() + ")");

            // Interface normale
            initializeNormalView();

        } catch (Exception e) {
            System.err.println("❌ Erreur: Impossible de récupérer l'utilisateur connecté");
            System.err.println("Détails: " + e.getMessage());
            e.printStackTrace();

            // Interface de redirection
            initializeLoginRedirect();
        }
    }

    private void initializeNormalView() {
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

    private void initializeLoginRedirect() {
        VerticalLayout loginPrompt = new VerticalLayout();
        loginPrompt.setSizeFull();
        loginPrompt.setAlignItems(Alignment.CENTER);
        loginPrompt.setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("🔒 Accès Restreint");
        title.getStyle().set("color", "#DC3545");

        Span message = new Span("Vous devez être connecté pour accéder à vos réservations.");
        message.getStyle()
                .set("font-size", "1.2em")
                .set("margin", "20px 0");

        Button loginButton = new Button("Se connecter", e ->
                getUI().ifPresent(ui -> ui.navigate("login"))
        );
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button homeButton = new Button("Retour à l'accueil", e ->
                getUI().ifPresent(ui -> ui.navigate(""))
        );
        homeButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        HorizontalLayout buttons = new HorizontalLayout(loginButton, homeButton);
        buttons.setSpacing(true);

        loginPrompt.add(title, message, buttons);
        add(loginPrompt);

        showError("Vous devez être connecté pour accéder à cette page");
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
        grid.addColumn(res -> {
                    try {
                        return res.getEvenement().getTitre();
                    } catch (Exception e) {
                        return "N/A";
                    }
                })
                .setHeader("Événement")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Date événement
        grid.addColumn(res -> {
                    try {
                        return res.getEvenement().getDateDebut().format(
                                DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        );
                    } catch (Exception e) {
                        return "N/A";
                    }
                })
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
        if (currentUser == null) {
            System.err.println("❌ currentUser est null, impossible de charger les réservations");
            return;
        }

        try {
            System.out.println("🔄 Chargement des réservations pour l'utilisateur ID: " + currentUser.getId());

            List<Reservation> reservations;

            // Recherche par code
            if (!searchField.isEmpty()) {
                try {
                    Reservation res = reservationService.findByCode(searchField.getValue());
                    // Vérifier que la réservation appartient à l'utilisateur
                    if (res.getUtilisateur().getId().equals(currentUser.getId())) {
                        reservations = List.of(res);
                    } else {
                        reservations = List.of();
                    }
                } catch (Exception e) {
                    System.err.println("⚠️ Réservation non trouvée avec le code: " + searchField.getValue());
                    reservations = List.of();
                }
            }
            // Filtrage par statut
            else if (statusFilter.getValue() != null) {
                System.out.println("🔍 Filtrage par statut: " + statusFilter.getValue());
                reservations = reservationService.findByUtilisateurAndStatut(
                        currentUser.getId(),
                        statusFilter.getValue()
                );
            }
            // Toutes les réservations
            else {
                System.out.println("📋 Chargement de toutes les réservations");
                reservations = reservationService.findByUtilisateur(currentUser.getId());
            }

            System.out.println("📊 Nombre de réservations trouvées: " + reservations.size());

            if (reservations.isEmpty()) {
                System.out.println("ℹ️ Aucune réservation pour cet utilisateur");
                showInfo("Vous n'avez aucune réservation pour le moment");
            } else {
                for (Reservation res : reservations) {
                    System.out.println("  - " + res.getCodeReservation() + " | " +
                            res.getEvenement().getTitre() + " | " +
                            res.getStatut().getLabel());
                }
            }

            grid.setItems(reservations);

        } catch (Exception e) {
            System.err.println("❌ Erreur lors du chargement: " + e.getMessage());
            e.printStackTrace();
            showError("Erreur lors du chargement des réservations: " + e.getMessage());
            grid.setItems();
        }
    }

    private void showDetails(Reservation reservation) {
        try {
            Map<String, Object> recap = reservationService.getRecapitulatifReservation(reservation.getId());

            // TODO: Afficher dans une belle dialog
            String details = "Détails de la réservation " + reservation.getCodeReservation() + "\n\n" +
                    "Événement: " + recap.get("titre") + "\n" +
                    "Places: " + recap.get("nombrePlaces") + "\n" +
                    "Montant: " + recap.get("montantTotal") + " DH";

            showSuccess(details);

        } catch (Exception e) {
            showError("Erreur lors du chargement des détails");
        }
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
            reservationService.annulerReservation(reservation.getId(), currentUser.getId());
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

    private void showInfo(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_CONTRAST);
    }
}