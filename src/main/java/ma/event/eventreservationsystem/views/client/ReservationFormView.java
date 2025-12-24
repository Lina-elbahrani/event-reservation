package ma.event.eventreservationsystem.views.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.router.*;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.service.EventService;
import ma.event.eventreservationsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;

@Route("event/:id/reserve")
@PageTitle("Réserver | Event Reservation System")
public class ReservationFormView extends VerticalLayout implements HasUrlParameter<Long> {

    private final EventService eventService;
    private final ReservationService reservationService;

    // TODO: Récupérer de la session
    private final Long currentUserId = 1L;

    private Long eventId;
    private Event event;

    private final IntegerField nombrePlacesField = new IntegerField("Nombre de places");
    private final TextArea commentaireField = new TextArea("Commentaire (optionnel)");
    private final Span montantTotalSpan = new Span();
    private final Button reserverButton = new Button("Confirmer la réservation");

    private final VerticalLayout contentLayout = new VerticalLayout();

    public ReservationFormView(
            @Autowired EventService eventService,
            @Autowired ReservationService reservationService
    ) {
        this.eventService = eventService;
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);

        add(contentLayout);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, Long eventId) {
        this.eventId = eventId;
        loadEventAndDisplayForm();
    }

    private void loadEventAndDisplayForm() {
        try {
            event = eventService.findById(eventId);

            // Vérifier que l'événement est réservable
            if (!isEventReservable()) {
                showError("Cet événement n'est pas disponible pour les réservations");
                getUI().ifPresent(ui -> ui.navigate("event/" + eventId));
                return;
            }

            displayReservationForm();

        } catch (Exception e) {
            showError("Événement non trouvé");
            getUI().ifPresent(ui -> ui.navigate("events"));
        }
    }

    private boolean isEventReservable() {
        // L'événement doit être publié
        return event.getStatut() == ma.event.eventreservationsystem.entity.enums.EventStatus.PUBLIE;
    }

    private void displayReservationForm() {
        contentLayout.removeAll();
        contentLayout.setMaxWidth("700px");
        contentLayout.setPadding(true);
        contentLayout.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        // Bouton retour
        Button backButton = new Button("← Retour à l'événement", e ->
                getUI().ifPresent(ui -> ui.navigate("event/" + eventId))
        );

        // Titre
        H1 title = new H1("Réserver des places");
        title.getStyle().set("color", "#1976D2");

        // Informations de l'événement
        VerticalLayout eventInfo = createEventInfoSection();

        // Formulaire de réservation
        VerticalLayout reservationForm = createReservationForm();

        // Récapitulatif
        VerticalLayout recapSection = createRecapSection();

        contentLayout.add(backButton, title, eventInfo, reservationForm, recapSection, reserverButton);
    }

    private VerticalLayout createEventInfoSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("background", "#F5F5F5")
                .set("border-radius", "8px");

        H2 eventTitle = new H2(event.getTitre());
        eventTitle.getStyle().set("margin", "0 0 10px 0");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy à HH:mm");

        Paragraph date = new Paragraph("📅 " + event.getDateDebut().format(formatter));
        Paragraph lieu = new Paragraph("📍 " + event.getLieu() + ", " + event.getVille());
        Paragraph prix = new Paragraph("💰 Prix unitaire : " + event.getPrixUnitaire() + " DH");

        int placesDisponibles = eventService.getPlacesDisponibles(eventId);
        Paragraph dispo = new Paragraph("✅ Places disponibles : " + placesDisponibles);

        section.add(eventTitle, date, lieu, prix, dispo);

        return section;
    }

    private VerticalLayout createReservationForm() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(false);
        section.setSpacing(true);

        H2 formTitle = new H2("Détails de votre réservation");

        // Configuration du champ nombre de places
        nombrePlacesField.setWidthFull();
        nombrePlacesField.setValue(1);
        nombrePlacesField.setMin(1);
        nombrePlacesField.setMax(10); // RÈGLE : Maximum 10 places
        nombrePlacesField.setStepButtonsVisible(true);
        nombrePlacesField.setHelperText("Maximum 10 places par réservation");

        // Calcul automatique du montant à chaque changement
        nombrePlacesField.addValueChangeListener(e -> updateMontantTotal());

        // Configuration du champ commentaire
        commentaireField.setWidthFull();
        commentaireField.setMaxLength(500);
        commentaireField.setPlaceholder("Informations supplémentaires...");

        section.add(formTitle, nombrePlacesField, commentaireField);

        return section;
    }

    private VerticalLayout createRecapSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.getStyle()
                .set("background", "#E3F2FD")
                .set("border-radius", "8px")
                .set("border", "2px solid #1976D2");

        H2 recapTitle = new H2("Récapitulatif");
        recapTitle.getStyle().set("margin", "0 0 15px 0");

        // Montant total avec style
        montantTotalSpan.getStyle()
                .set("font-size", "2em")
                .set("font-weight", "bold")
                .set("color", "#1976D2");

        updateMontantTotal(); // Calcul initial

        HorizontalLayout montantLayout = new HorizontalLayout();
        montantLayout.setDefaultVerticalComponentAlignment(Alignment.CENTER);
        Span label = new Span("Montant total : ");
        label.getStyle().set("font-size", "1.2em");
        montantLayout.add(label, montantTotalSpan);

        section.add(recapTitle, montantLayout);

        return section;
    }

    private void updateMontantTotal() {
        if (nombrePlacesField.getValue() != null) {
            double montant = nombrePlacesField.getValue() * event.getPrixUnitaire();
            montantTotalSpan.setText(String.format("%.2f DH", montant));

            // Vérifier la disponibilité
            int placesDisponibles = eventService.getPlacesDisponibles(eventId);
            if (nombrePlacesField.getValue() > placesDisponibles) {
                nombrePlacesField.setErrorMessage(
                        "Seulement " + placesDisponibles + " places disponibles"
                );
                nombrePlacesField.setInvalid(true);
                reserverButton.setEnabled(false);
            } else {
                nombrePlacesField.setInvalid(false);
                reserverButton.setEnabled(true);
                configureReserverButton();
            }
        }
    }

    private void configureReserverButton() {
        reserverButton.setWidthFull();
        reserverButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        reserverButton.addClickListener(e -> confirmerReservation());
    }

    private void confirmerReservation() {
        // Validations finales
        if (nombrePlacesField.getValue() == null || nombrePlacesField.getValue() < 1) {
            showError("Veuillez sélectionner au moins 1 place");
            return;
        }

        // RÈGLE : Maximum 10 places
        if (nombrePlacesField.getValue() > 10) {
            showError("Maximum 10 places par réservation");
            return;
        }

        try {
            // Création de la réservation
            Reservation reservation = Reservation.builder()
                    .nombrePlaces(nombrePlacesField.getValue())
                    .commentaire(commentaireField.getValue())
                    .build();

            // Appel du service qui vérifie toutes les règles métier
            Reservation savedReservation = reservationService.createReservation(
                    reservation,
                    currentUserId,
                    eventId
            );

            // Succès - Afficher le code de réservation
            showReservationSuccess(savedReservation);

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showReservationSuccess(Reservation reservation) {
        // Clear le formulaire
        contentLayout.removeAll();

        // Message de succès avec le code
        VerticalLayout successLayout = new VerticalLayout();
        successLayout.setAlignItems(Alignment.CENTER);
        successLayout.setPadding(true);

        H1 successTitle = new H1("✅ Réservation Confirmée !");
        successTitle.getStyle().set("color", "#28A745");

        H2 codeTitle = new H2("Votre code de réservation :");

        Span codeSpan = new Span(reservation.getCodeReservation());
        codeSpan.getStyle()
                .set("font-size", "2.5em")
                .set("font-weight", "bold")
                .set("color", "#1976D2")
                .set("padding", "20px")
                .set("background", "#E3F2FD")
                .set("border-radius", "8px");

        Paragraph info = new Paragraph(
                "Votre réservation a été enregistrée avec succès. " +
                        "Conservez précieusement ce code pour accéder à l'événement."
        );
        info.getStyle().set("text-align", "center").set("max-width", "500px");

        Paragraph details = new Paragraph(
                "📝 Nombre de places : " + reservation.getNombrePlaces() + "\n" +
                        "💰 Montant total : " + reservation.getMontantTotal() + " DH\n" +
                        "📅 Événement : " + event.getTitre()
        );
        details.getStyle()
                .set("background", "#F5F5F5")
                .set("padding", "15px")
                .set("border-radius", "8px")
                .set("white-space", "pre-line");

        Button mesReservationsButton = new Button("Voir mes réservations", e ->
                getUI().ifPresent(ui -> ui.navigate("my-reservations"))
        );
        mesReservationsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);

        Button accueilButton = new Button("Retour à l'accueil", e ->
                getUI().ifPresent(ui -> ui.navigate(""))
        );
        accueilButton.addThemeVariants(ButtonVariant.LUMO_LARGE);

        HorizontalLayout buttonsLayout = new HorizontalLayout(mesReservationsButton, accueilButton);

        successLayout.add(
                successTitle,
                codeTitle,
                codeSpan,
                info,
                details,
                buttonsLayout
        );

        contentLayout.add(successLayout);
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }
}