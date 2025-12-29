package ma.event.eventreservationsystem.views.organizer;

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
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Route("organizer/dashboard")
@PageTitle("Dashboard Organisateur | Event Reservation System")
public class OrganizerDashboardView extends VerticalLayout {

    private final EventService eventService;

    // TODO: Récupérer de la session
    private final Long currentUserId = 1L;

    public OrganizerDashboardView(@Autowired EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // Titre
        H1 title = new H1("Dashboard Organisateur");
        title.getStyle().set("color", "#1976D2");

        // Cartes de statistiques
        HorizontalLayout statsCards = createStatsCards();

        // Boutons d'action rapide
        H2 actionsTitle = new H2("Actions Rapides");
        HorizontalLayout quickActions = createQuickActions();

        // Assemblage
        add(title, statsCards, actionsTitle, quickActions);
    }

    private HorizontalLayout createStatsCards() {
        Map<String, Object> stats = eventService.getStatistiquesOrganisateur(currentUserId);

        // Carte événements
        VerticalLayout eventsCard = createStatCard(
                "📅 Événements",
                stats.get("nombreEvenements").toString(),
                "Total créés",
                "#1976D2"
        );

        // Carte brouillons
        VerticalLayout brouillonsCard = createStatCard(
                "✏️ Brouillons",
                stats.get("nombreBrouillons").toString(),
                "En attente de publication",
                "#FFA500"
        );

        // Carte publiés
        VerticalLayout publiesCard = createStatCard(
                "✅ Publiés",
                stats.get("nombrePublies").toString(),
                "Événements actifs",
                "#28A745"
        );

        // Carte réservations
        VerticalLayout reservationsCard = createStatCard(
                "🎫 Réservations",
                stats.get("nombreTotalReservations").toString(),
                "Total reçues",
                "#9C27B0"
        );

        // Carte revenus
        VerticalLayout revenusCard = createStatCard(
                "💰 Revenus",
                String.format("%.2f DH", stats.get("revenuTotal")),
                "Total généré",
                "#28A745"
        );

        HorizontalLayout layout = new HorizontalLayout(
                eventsCard, brouillonsCard, publiesCard, reservationsCard, revenusCard
        );
        layout.setWidthFull();
        layout.setSpacing(true);

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
                .set("font-size", "2em");

        Paragraph subtitleP = new Paragraph(subtitle);
        subtitleP.getStyle()
                .set("font-size", "0.85em")
                .set("color", "#999")
                .set("margin", "0");

        card.add(titleSpan, valueH1, subtitleP);

        return card;
    }

    private HorizontalLayout createQuickActions() {
        Button createEventButton = new Button("➕ Créer un événement", e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/event/new"))
        );
        createEventButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button myEventsButton = new Button("📋 Mes événements", e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/events"))
        );
        myEventsButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        HorizontalLayout layout = new HorizontalLayout(createEventButton, myEventsButton);
        layout.setSpacing(true);

        return layout;
    }
}