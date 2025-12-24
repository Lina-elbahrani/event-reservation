package ma.event.eventreservationsystem.views.client;

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
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.service.ReservationService;
import ma.event.eventreservationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

@Route("dashboard")
@PageTitle("Tableau de bord | Event Reservation System")
public class DashboardView extends VerticalLayout {

    private final UserService userService;
    private final ReservationService reservationService;

    // TODO: Récupérer l'utilisateur connecté (session)
    private final Long currentUserId = 1L; // Temporaire

    public DashboardView(
            @Autowired UserService userService,
            @Autowired ReservationService reservationService
    ) {
        this.userService = userService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Titre de bienvenue
        try {
            var user = userService.findById(currentUserId);
            H1 welcome = new H1("Bienvenue, " + user.getPrenom() + " !");
            welcome.getStyle().set("color", "#1976D2");
            add(welcome);
        } catch (Exception e) {
            add(new H1("Tableau de bord"));
        }

        // Cartes de statistiques
        HorizontalLayout statsLayout = createStatsCards();

        // Raccourcis rapides
        H2 shortcutsTitle = new H2("Accès rapide");
        HorizontalLayout shortcutsLayout = createShortcuts();

        // Réservations à venir
        H2 upcomingTitle = new H2("Mes prochaines réservations");
        VerticalLayout upcomingReservations = createUpcomingReservations();

        // Assemblage
        add(
                statsLayout,
                shortcutsTitle,
                shortcutsLayout,
                upcomingTitle,
                upcomingReservations
        );
    }

    private HorizontalLayout createStatsCards() {
        Map<String, Object> stats = userService.getStatistiquesUtilisateur(currentUserId);

        // Carte Nombre de réservations
        VerticalLayout reservationsCard = createStatCard(
                "📝 Réservations",
                stats.get("nombreReservations").toString(),
                "Total de vos réservations",
                "#1976D2"
        );

        // Carte Montant dépensé
        VerticalLayout montantCard = createStatCard(
                "💰 Dépenses",
                stats.get("montantTotalDepense") + " DH",
                "Montant total dépensé",
                "#28A745"
        );

        HorizontalLayout layout = new HorizontalLayout(reservationsCard, montantCard);
        layout.setWidthFull();

        return layout;
    }

    private VerticalLayout createStatCard(String title, String value, String subtitle, String color) {
        VerticalLayout card = new VerticalLayout();
        card.setPadding(true);
        card.setWidth("300px");
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
                .set("color", color);

        Paragraph subtitleP = new Paragraph(subtitle);
        subtitleP.getStyle()
                .set("font-size", "0.85em")
                .set("color", "#999")
                .set("margin", "0");

        card.add(titleSpan, valueH1, subtitleP);

        return card;
    }

    private HorizontalLayout createShortcuts() {
        Button exploreButton = new Button("🔍 Explorer les événements", e ->
                getUI().ifPresent(ui -> ui.navigate("events"))
        );
        exploreButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button reservationsButton = new Button("📋 Mes réservations", e ->
                getUI().ifPresent(ui -> ui.navigate("my-reservations"))
        );
        reservationsButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        Button profileButton = new Button("👤 Mon profil", e ->
                getUI().ifPresent(ui -> ui.navigate("profile"))
        );
        profileButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        HorizontalLayout layout = new HorizontalLayout(exploreButton, reservationsButton, profileButton);
        layout.setSpacing(true);

        return layout;
    }

    private VerticalLayout createUpcomingReservations() {
        VerticalLayout layout = new VerticalLayout();
        layout.setPadding(false);

        try {
            List<Reservation> upcomingReservations =
                    reservationService.findUpcomingReservationsByUser(currentUserId);

            if (upcomingReservations.isEmpty()) {
                layout.add(new Paragraph("Aucune réservation à venir"));
            } else {
                upcomingReservations.stream()
                        .limit(5)
                        .forEach(reservation -> layout.add(createReservationCard(reservation)));
            }
        } catch (Exception e) {
            layout.add(new Paragraph("Erreur lors du chargement des réservations"));
        }

        return layout;
    }

    private HorizontalLayout createReservationCard(Reservation reservation) {
        HorizontalLayout card = new HorizontalLayout();
        card.setWidthFull();
        card.setPadding(true);
        card.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 1px 4px rgba(0,0,0,0.1)")
                .set("margin-bottom", "10px");

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        H2 eventTitle = new H2(reservation.getEvenement().getTitre());
        eventTitle.getStyle().set("margin", "0");

        Paragraph details = new Paragraph(
                "📅 " + reservation.getEvenement().getDateDebut().toLocalDate() +
                        " | 🎫 " + reservation.getNombrePlaces() + " place(s)" +
                        " | 💰 " + reservation.getMontantTotal() + " DH"
        );
        details.getStyle().set("margin", "5px 0");

        Span statusBadge = new Span(reservation.getStatut().getLabel());
        String badgeColor = switch (reservation.getStatut()) {
            case CONFIRMEE -> "#28A745";
            case EN_ATTENTE -> "#FFA500";
            case ANNULEE -> "#DC3545";
        };
        statusBadge.getStyle()
                .set("background", badgeColor)
                .set("color", "white")
                .set("padding", "4px 12px")
                .set("border-radius", "12px")
                .set("font-size", "0.85em");

        infoLayout.add(eventTitle, details, statusBadge);

        Button detailsButton = new Button("Détails", e ->
                getUI().ifPresent(ui -> ui.navigate("event/" + reservation.getEvenement().getId()))
        );
        detailsButton.addThemeVariants(ButtonVariant.LUMO_SMALL);

        card.add(infoLayout, detailsButton);
        card.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        card.expand(infoLayout);

        return card;
    }
}
