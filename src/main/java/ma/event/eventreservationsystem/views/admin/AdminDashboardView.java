package ma.event.eventreservationsystem.views.admin;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.service.EventService;
import ma.event.eventreservationsystem.service.ReservationService;
import ma.event.eventreservationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Route("admin/dashboard")
@PageTitle("Dashboard Admin | Event Reservation System")
public class AdminDashboardView extends VerticalLayout {

    private final UserService userService;
    private final EventService eventService;
    private final ReservationService reservationService;

    public AdminDashboardView(
            @Autowired UserService userService,
            @Autowired EventService eventService,
            @Autowired ReservationService reservationService
    ) {
        this.userService = userService;
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Titre
        H1 title = new H1("Dashboard Administrateur");
        title.getStyle().set("color", "#1976D2");

        // Statistiques utilisateurs
        H2 usersTitle = new H2("Utilisateurs");
        HorizontalLayout usersStats = createUsersStats();

        // Statistiques événements
        H2 eventsTitle = new H2("Événements");
        HorizontalLayout eventsStats = createEventsStats();

        // Statistiques réservations
        H2 reservationsTitle = new H2("Réservations");
        HorizontalLayout reservationsStats = createReservationsStats();

        // Actions rapides
        H2 actionsTitle = new H2("Actions Rapides");
        HorizontalLayout quickActions = createQuickActions();

        // Assemblage
        add(
                title,
                usersTitle, usersStats,
                eventsTitle, eventsStats,
                reservationsTitle, reservationsStats,
                actionsTitle, quickActions
        );
    }

    private HorizontalLayout createUsersStats() {
        long totalUsers = userService.findAll().size();
        long nbAdmins = userService.countByRole(UserRole.ADMIN);
        long nbOrganizers = userService.countByRole(UserRole.ORGANIZER);
        long nbClients = userService.countByRole(UserRole.CLIENT);
        long nbActifs = userService.findActifs().size();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        layout.add(
                createStatCard("👥 Total utilisateurs", String.valueOf(totalUsers), "Tous les comptes", "#1976D2"),
                createStatCard("🔑 Admins", String.valueOf(nbAdmins), "Administrateurs", "#DC3545"),
                createStatCard("🎭 Organisateurs", String.valueOf(nbOrganizers), "Créateurs d'événements", "#9C27B0"),
                createStatCard("👤 Clients", String.valueOf(nbClients), "Utilisateurs standards", "#28A745"),
                createStatCard("✅ Actifs", String.valueOf(nbActifs), "Comptes actifs", "#1976D2")
        );

        return layout;
    }

    private HorizontalLayout createEventsStats() {
        long totalEvents = eventService.findAll().size();
        long brouillons = eventService.findByStatut(EventStatus.BROUILLON).size();
        long publies = eventService.findByStatut(EventStatus.PUBLIE).size();
        long annules = eventService.findByStatut(EventStatus.ANNULE).size();
        long termines = eventService.findByStatut(EventStatus.TERMINE).size();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        layout.add(
                createStatCard("📅 Total événements", String.valueOf(totalEvents), "Tous statuts", "#1976D2"),
                createStatCard("✏️ Brouillons", String.valueOf(brouillons), "En attente", "#FFA500"),
                createStatCard("✅ Publiés", String.valueOf(publies), "En ligne", "#28A745"),
                createStatCard("❌ Annulés", String.valueOf(annules), "Annulés", "#DC3545"),
                createStatCard("🏁 Terminés", String.valueOf(termines), "Événements passés", "#6C757D")
        );

        return layout;
    }

    private HorizontalLayout createReservationsStats() {
        Map<String, Object> stats = reservationService.getStatistiquesReservation();

        HorizontalLayout layout = new HorizontalLayout();
        layout.setWidthFull();
        layout.setSpacing(true);

        layout.add(
                createStatCard("🎫 Total réservations",
                        stats.get("nombreTotalReservations").toString(),
                        "Toutes les réservations", "#1976D2"),
                createStatCard("✅ Confirmées",
                        stats.get("reservationsConfirmees").toString(),
                        "Validées", "#28A745"),
                createStatCard("⏳ En attente",
                        stats.get("reservationsEnAttente").toString(),
                        "À confirmer", "#FFA500"),
                createStatCard("💰 Revenus totaux",
                        String.format("%.2f DH", stats.get("revenuTotal")),
                        "Plateforme", "#28A745"),
                createStatCard("👥 Places réservées",
                        stats.get("nombreTotalPlaces").toString(),
                        "Total", "#9C27B0")
        );

        return layout;
    }

    private VerticalLayout createStatCard(String title, String value, String subtitle, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setWidth("220px");
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "12px")
                .set("box-shadow", "0 2px 8px rgba(0,0,0,0.1)")
                .set("border-left", "4px solid " + color);

        Span titleSpan = new Span(title);
        titleSpan.getStyle()
                .set("font-size", "0.9em")
                .set("color", "#666");

        H1 valueH1 = new H1(value);
        valueH1.getStyle()
                .set("margin", "10px 0")
                .set("color", color)
                .set("font-size", "1.8em");

        Paragraph subtitleP = new Paragraph(subtitle);
        subtitleP.getStyle()
                .set("font-size", "0.85em")
                .set("color", "#999")
                .set("margin", "0");

        card.add(titleSpan, valueH1, subtitleP);

        return card;
    }

    private HorizontalLayout createQuickActions() {
        Button usersButton = new Button("👥 Gérer les utilisateurs", e ->
                getUI().ifPresent(ui -> ui.navigate("admin/users"))
        );
        usersButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button eventsButton = new Button("📅 Gérer les événements", e ->
                getUI().ifPresent(ui -> ui.navigate("admin/events"))
        );
        eventsButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        Button reservationsButton = new Button("🎫 Gérer les réservations", e ->
                getUI().ifPresent(ui -> ui.navigate("admin/reservations"))
        );
        reservationsButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        HorizontalLayout layout = new HorizontalLayout(usersButton, eventsButton, reservationsButton);
        layout.setSpacing(true);

        return layout;
    }
}

