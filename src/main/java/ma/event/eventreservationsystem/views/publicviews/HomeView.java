package ma.event.eventreservationsystem.views.publicviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Route("")
@PageTitle("Accueil | Event Reservation System")
public class HomeView extends VerticalLayout {

    private final EventService eventService;

    public HomeView(@Autowired EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(false);
        setSpacing(false);

        /* ================= HEADER ================= */
        VerticalLayout header = new VerticalLayout();
        header.setWidthFull();
        header.setPadding(true);
        header.setAlignItems(Alignment.CENTER);
        header.getStyle()
                .set("background", "linear-gradient(135deg, #1976D2, #42A5F5)")
                .set("color", "white")
                .set("padding", "40px");

        H1 title = new H1("Event Reservation System");
        title.getStyle().set("margin", "0");

        Paragraph description = new Paragraph(
                "Découvrez et réservez vos places pour les meilleurs événements : concerts, théâtres, conférences..."
        );
        description.getStyle().set("font-size", "1.1em");

        Button exploreButton = new Button("Explorer les événements",
                e -> getUI().ifPresent(ui -> ui.navigate("events")));
        exploreButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button loginButton = new Button("Se connecter",
                e -> getUI().ifPresent(ui -> ui.navigate("login")));
        loginButton.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);

        HorizontalLayout buttonsLayout = new HorizontalLayout(exploreButton, loginButton);
        buttonsLayout.setSpacing(true);

        header.add(title, description, buttonsLayout);

        /* ================= CONTENT ================= */
        VerticalLayout content = new VerticalLayout();
        content.setWidthFull();
        content.setPadding(true);
        content.setAlignItems(Alignment.CENTER);

        H2 popularTitle = new H2("🔥 Événements Populaires");
        popularTitle.getStyle().set("margin-top", "30px");

        List<Event> popularEvents = eventService.findPopularEvents();

        FlexLayout cardsLayout = new FlexLayout();
        cardsLayout.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        cardsLayout.setJustifyContentMode(FlexComponent.JustifyContentMode.CENTER);
        cardsLayout.getStyle().set("gap", "20px");
        cardsLayout.setWidthFull();

        if (popularEvents.isEmpty()) {
            content.add(new Paragraph("Aucun événement disponible pour le moment"));
        } else {
            popularEvents.stream()
                    .limit(3)
                    .forEach(event -> cardsLayout.add(createEventCard(event)));
        }

        content.add(popularTitle, cardsLayout);

        /* ================= ASSEMBLAGE ================= */
        add(header, content);
    }

    /* ================= EVENT CARD ================= */
    private VerticalLayout createEventCard(Event event) {
        VerticalLayout card = new VerticalLayout();
        card.setWidth("320px");
        card.setPadding(true);
        card.setSpacing(false);

        card.getStyle()
                .set("background", "#FFFFFF")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.12)")
                .set("transition", "transform 0.3s")
                .set("cursor", "pointer");

        card.getElement().addEventListener("mouseover",
                e -> card.getStyle().set("transform", "scale(1.03)"));
        card.getElement().addEventListener("mouseout",
                e -> card.getStyle().set("transform", "scale(1)"));

        H2 eventTitle = new H2(event.getTitre());
        eventTitle.getStyle()
                .set("margin", "0")
                .set("color", "#1976D2")
                .set("font-size", "1.3em");

        Paragraph category = new Paragraph("🎭 " + event.getCategorie().getLabel());
        Paragraph date = new Paragraph("📅 " + event.getDateDebut().toLocalDate());
        Paragraph lieu = new Paragraph("📍 " + event.getLieu() + ", " + event.getVille());

        Paragraph prix = new Paragraph(event.getPrixUnitaire() + " DH");
        prix.getStyle()
                .set("font-weight", "bold")
                .set("color", "#2E7D32")
                .set("font-size", "1.1em");

        Button detailsButton = new Button("Voir détails",
                e -> getUI().ifPresent(ui -> ui.navigate("event/" + event.getId())));
        detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        detailsButton.setWidthFull();

        card.add(eventTitle, category, date, lieu, prix, detailsButton);
        return card;
    }
}
