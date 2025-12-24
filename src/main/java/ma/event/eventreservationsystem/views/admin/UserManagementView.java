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
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;

@Route("admin/users")
@PageTitle("Gestion Utilisateurs | Event Reservation System")
public class UserManagementView extends VerticalLayout {

    private final UserService userService;

    private final Grid<User> grid = new Grid<>(User.class, false);
    private final ComboBox<UserRole> roleFilter = new ComboBox<>("Filtrer par rôle");
    private final ComboBox<Boolean> statusFilter = new ComboBox<>("Filtrer par statut");
    private final TextField searchField = new TextField("Rechercher");

    public UserManagementView(@Autowired UserService userService) {
        this.userService = userService;

        setSizeFull();
        setPadding(true);

        // Titre
        H1 title = new H1("Gestion des Utilisateurs");
        title.getStyle().set("color", "#1976D2");

        // Filtres
        configureFilters();
        HorizontalLayout filtersLayout = new HorizontalLayout(roleFilter, statusFilter, searchField);
        filtersLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Grille
        configureGrid();

        // Assemblage
        add(title, filtersLayout, grid);

        // Charger les données
        updateList();
    }

    private void configureFilters() {
        roleFilter.setItems(UserRole.values());
        roleFilter.setItemLabelGenerator(UserRole::getLabel);
        roleFilter.setClearButtonVisible(true);
        roleFilter.addValueChangeListener(e -> updateList());

        statusFilter.setItems(true, false);
        statusFilter.setItemLabelGenerator(actif -> actif ? "Actif" : "Inactif");
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());

        searchField.setPlaceholder("Nom, prénom ou email...");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> updateList());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonne Nom
        grid.addColumn(user -> user.getNom() + " " + user.getPrenom())
                .setHeader("Nom complet")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Email
        grid.addColumn(User::getEmail)
                .setHeader("Email")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Rôle avec badge
        grid.addComponentColumn(user -> {
            Span badge = new Span(user.getRole().getLabel());
            String color = switch (user.getRole()) {
                case ADMIN -> "#DC3545";
                case ORGANIZER -> "#9C27B0";
                case CLIENT -> "#28A745";
            };
            badge.getStyle()
                    .set("background", color)
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        }).setHeader("Rôle");

        // Colonne Date inscription
        grid.addColumn(user -> user.getDateInscription().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy")
                ))
                .setHeader("Inscription")
                .setSortable(true);

        // Colonne Statut
        grid.addComponentColumn(user -> {
            Span badge = new Span(user.getActif() ? "Actif" : "Inactif");
            badge.getStyle()
                    .set("background", user.getActif() ? "#28A745" : "#DC3545")
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        }).setHeader("Statut");

        // Colonne Actions
        grid.addComponentColumn(user -> createActionsLayout(user))
                .setHeader("Actions")
                .setAutoWidth(true);
    }

    private HorizontalLayout createActionsLayout(User user) {
        HorizontalLayout actions = new HorizontalLayout();
        actions.setSpacing(true);

        // Bouton Activer/Désactiver
        if (user.getActif()) {
            Button deactivateButton = new Button("Désactiver");
            deactivateButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_ERROR);
            deactivateButton.addClickListener(e -> confirmToggleStatus(user));
            actions.add(deactivateButton);
        } else {
            Button activateButton = new Button("Activer");
            activateButton.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
            activateButton.addClickListener(e -> confirmToggleStatus(user));
            actions.add(activateButton);
        }

        // Bouton Voir détails (statistiques)
        Button detailsButton = new Button("📊");
        detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);
        detailsButton.addClickListener(e -> showUserStats(user));

        actions.add(detailsButton);

        return actions;
    }

    private void updateList() {
        try {
            List<User> users = userService.findAll();

            // Filtrage par rôle
            if (roleFilter.getValue() != null) {
                users = users.stream()
                        .filter(u -> u.getRole() == roleFilter.getValue())
                        .toList();
            }

            // Filtrage par statut
            if (statusFilter.getValue() != null) {
                users = users.stream()
                        .filter(u -> u.getActif() == statusFilter.getValue())
                        .toList();
            }

            // Recherche
            if (!searchField.isEmpty()) {
                String search = searchField.getValue().toLowerCase();
                users = users.stream()
                        .filter(u ->
                                u.getNom().toLowerCase().contains(search) ||
                                        u.getPrenom().toLowerCase().contains(search) ||
                                        u.getEmail().toLowerCase().contains(search)
                        )
                        .toList();
            }

            grid.setItems(users);

        } catch (Exception e) {
            showError("Erreur lors du chargement des utilisateurs");
            grid.setItems();
        }
    }

    private void confirmToggleStatus(User user) {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader(user.getActif() ? "Désactiver le compte" : "Activer le compte");
        dialog.setText(
                "Êtes-vous sûr de vouloir " +
                        (user.getActif() ? "désactiver" : "activer") +
                        " le compte de " + user.getPrenom() + " " + user.getNom() + " ?"
        );

        dialog.setCancelable(true);
        dialog.setConfirmText(user.getActif() ? "Désactiver" : "Activer");
        dialog.setConfirmButtonTheme(user.getActif() ? "error primary" : "success primary");

        dialog.addConfirmListener(e -> {
            try {
                userService.toggleActif(user.getId());
                showSuccess("Statut modifié avec succès");
                updateList();
            } catch (Exception ex) {
                showError(ex.getMessage());
            }
        });

        dialog.open();
    }

    private void showUserStats(User user) {
        try {
            var stats = userService.getStatistiquesUtilisateur(user.getId());

            String message = "Statistiques de " + user.getPrenom() + " " + user.getNom() + "\n\n" +
                    "📝 Réservations : " + stats.get("nombreReservations") + "\n" +
                    "💰 Montant dépensé : " + stats.get("montantTotalDepense") + " DH";

            if (stats.containsKey("nombreEvenementsCreés")) {
                message += "\n📅 Événements créés : " + stats.get("nombreEvenementsCreés");
            }

            Notification.show(message, 5000, Notification.Position.MIDDLE);

        } catch (Exception e) {
            showError("Erreur lors du chargement des statistiques");
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
