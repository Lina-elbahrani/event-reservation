package ma.event.eventreservationsystem.views.publicviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

@Route("event")  // ← CHANGEMENT ICI : enlevez /:id
@PageTitle("Détails de l'événement | Event Reservation System")
public class EventDetailView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventService eventService;
    private Long eventId;
    private Event event;
    private final VerticalLayout contentLayout = new VerticalLayout();

    public EventDetailView(@Autowired EventService eventService) {
        this.eventService = eventService;
        setSizeFull();
        setPadding(true);
        add(contentLayout);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        System.out.println("================================");
        System.out.println("📝 setParameter appelé");
        System.out.println("ID reçu: " + eventId);
        System.out.println("Type: " + (eventId != null ? eventId.getClass().getName() : "NULL"));
        System.out.println("================================");

        this.eventId = eventId;
        loadEventDetails();
    }

    private void loadEventDetails() {
        System.out.println("🔄 Début loadEventDetails() pour ID: " + eventId);

        try {
            event = eventService.findById(eventId);

            if (event == null) {
                System.err.println("❌ L'événement retourné est NULL");
                showError("L'événement est null");
                getUI().ifPresent(ui -> ui.navigate("events"));
                return;
            }

            System.out.println("✅ Événement chargé avec succès: " + event.getTitre());
            displayEventDetails();

        } catch (Exception e) {
            System.err.println("================================");
            System.err.println("❌ EXCEPTION ATTRAPÉE");
            System.err.println("Type: " + e.getClass().getName());
            System.err.println("Message: " + e.getMessage());
            System.err.println("================================");
            e.printStackTrace();

            showError("Événement non trouvé");
            getUI().ifPresent(ui -> ui.navigate(""));
        }
    }

    private void displayEventDetails() {
        System.out.println("🎨 Début displayEventDetails()");

        contentLayout.removeAll();
        contentLayout.setMaxWidth("900px");
        contentLayout.setWidthFull();
        contentLayout.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        // Bouton retour
        Button backButton = new Button("← Retour à la liste", e ->
                getUI().ifPresent(ui -> ui.navigate("events"))
        );

        // Titre
        H1 title = new H1(event.getTitre());
        title.getStyle().set("color", "#1976D2").set("margin-bottom", "10px");

        // Badge de catégorie
        Span categoryBadge = new Span(event.getCategorie().getLabel());
        categoryBadge.getStyle()
                .set("background", "#E3F2FD")
                .set("color", "#1976D2")
                .set("padding", "5px 15px")
                .set("border-radius", "20px")
                .set("font-weight", "bold");

        // Badge de statut
        Span statusBadge = new Span(event.getStatut().getLabel());
        String statusColor = switch (event.getStatut()) {
            case PUBLIE -> "#28A745";
            case BROUILLON -> "#FFA500";
            case ANNULE -> "#DC3545";
            case TERMINE -> "#6C757D";
        };
        statusBadge.getStyle()
                .set("background", statusColor)
                .set("color", "white")
                .set("padding", "5px 15px")
                .set("border-radius", "20px")
                .set("font-weight", "bold")
                .set("margin-left", "10px");

        HorizontalLayout badgesLayout = new HorizontalLayout(categoryBadge, statusBadge);

        // Image (si disponible)
        if (event.getImageUrl() != null && !event.getImageUrl().isEmpty()) {
            Image image = new Image(event.getImageUrl(), event.getTitre());
            image.setWidth("100%");
            image.setMaxHeight("400px");
            image.getStyle().set("border-radius", "8px").set("object-fit", "cover");
            contentLayout.add(image);
        }

        // Description
        H2 descTitle = new H2("Description");
        Paragraph description = new Paragraph(
                event.getDescription() != null ? event.getDescription() : "Aucune description disponible"
        );
        description.getStyle().set("font-size", "1.1em").set("line-height", "1.6");

        // Informations pratiques
        H2 infoTitle = new H2("Informations Pratiques");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(true);
        infoLayout.getStyle().set("background", "#F5F5F5").set("border-radius", "8px");

        infoLayout.add(
                createInfoRow("📅 Date de début", event.getDateDebut().format(formatter)),
                createInfoRow("🕒 Date de fin", event.getDateFin().format(formatter)),
                createInfoRow("📍 Lieu", event.getLieu()),
                createInfoRow("🏙️ Ville", event.getVille()),
                createInfoRow("💰 Prix unitaire", event.getPrixUnitaire() + " DH"),
                createInfoRow("👥 Capacité totale", event.getCapaciteMax() + " places"),
                createInfoRow("✅ Places disponibles",
                        eventService.getPlacesDisponibles(eventId) + " places")
        );

        // Organisateur
        H2 orgTitle = new H2("Organisateur");
        Paragraph orgInfo = new Paragraph(
                event.getOrganisateur().getPrenom() + " " + event.getOrganisateur().getNom()
        );

        // Bouton de réservation
        Button reserveButton = new Button("Réserver des places");
        reserveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserveButton.setWidthFull();
        reserveButton.addClickListener(e -> {
            getUI().ifPresent(ui -> ui.navigate("login"));
        });

        // Si pas de places disponibles
        if (eventService.getPlacesDisponibles(eventId) == 0) {
            reserveButton.setText("Complet - Aucune place disponible");
            reserveButton.setEnabled(false);
        }

        // Assemblage
        contentLayout.add(
                backButton,
                title,
                badgesLayout,
                descTitle,
                description,
                infoTitle,
                infoLayout,
                orgTitle,
                orgInfo,
                reserveButton
        );

        System.out.println("✅ displayEventDetails() terminé avec succès");
    }

    private HorizontalLayout createInfoRow(String label, String value) {
        Span labelSpan = new Span(label);
        labelSpan.getStyle().set("font-weight", "bold").set("min-width", "200px");

        Span valueSpan = new Span(value);

        HorizontalLayout row = new HorizontalLayout(labelSpan, valueSpan);
        row.setDefaultVerticalComponentAlignment(Alignment.CENTER);

        return row;
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}