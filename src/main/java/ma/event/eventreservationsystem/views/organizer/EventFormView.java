package ma.event.eventreservationsystem.views.organizer;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datetimepicker.DateTimePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.router.*;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Route("organizer/event/:action/:id?")
@PageTitle("Formulaire Événement | Event Reservation System")
public class EventFormView extends VerticalLayout implements HasUrlParameter<String> {

    private final EventService eventService;

    // TODO: Récupérer de la session
    private final Long currentUserId = 1L;

    private Event event;
    private boolean isNewEvent;
    private Long eventId;

    // Champs du formulaire
    private final TextField titreField = new TextField("Titre");
    private final TextArea descriptionField = new TextArea("Description");
    private final ComboBox<EventCategory> categorieField = new ComboBox<>("Catégorie");
    private final DateTimePicker dateDebutField = new DateTimePicker("Date de début");
    private final DateTimePicker dateFinField = new DateTimePicker("Date de fin");
    private final TextField lieuField = new TextField("Lieu");
    private final TextField villeField = new TextField("Ville");
    private final IntegerField capaciteField = new IntegerField("Capacité maximale");
    private final NumberField prixField = new NumberField("Prix unitaire (DH)");
    private final TextField imageUrlField = new TextField("URL de l'image");

    private final Button saveDraftButton = new Button("Sauvegarder en brouillon");
    private final Button publishButton = new Button("Publier");
    private final Button cancelButton = new Button("Annuler");

    private final Binder<Event> binder = new Binder<>(Event.class);

    public EventFormView(@Autowired EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);
    }

    @Override
    public void setParameter(BeforeEvent beforeEvent, @OptionalParameter String parameter) {
        if (parameter == null) {
            // Route /organizer/event/new
            isNewEvent = true;
            event = Event.builder().build();
            createForm();
        } else {
            // Route /organizer/event/edit/123
            try {
                eventId = Long.parseLong(parameter);
                isNewEvent = false;
                loadEventAndCreateForm();
            } catch (NumberFormatException e) {
                showError("ID d'événement invalide");
                getUI().ifPresent(ui -> ui.navigate("organizer/events"));
            }
        }
    }

    private void loadEventAndCreateForm() {
        try {
            event = eventService.findById(eventId);

            // RÈGLE : Un événement terminé ne peut plus être modifié
            if (event.getStatut() == EventStatus.TERMINE) {
                showError("Impossible de modifier un événement terminé");
                getUI().ifPresent(ui -> ui.navigate("organizer/events"));
                return;
            }

            createForm();

        } catch (Exception e) {
            showError("Événement non trouvé");
            getUI().ifPresent(ui -> ui.navigate("organizer/events"));
        }
    }

    private void createForm() {
        removeAll();

        H1 title = new H1(isNewEvent ? "Créer un événement" : "Modifier l'événement");
        title.getStyle().set("color", "#1976D2");

        // Configuration des champs
        configureFields();

        // Liaison avec Binder
        configureBinder();

        // Layout du formulaire
        FormLayout formLayout = createFormLayout();

        // Configuration des boutons
        configureButtons();

        HorizontalLayout buttonsLayout = new HorizontalLayout(
                saveDraftButton, publishButton, cancelButton
        );
        buttonsLayout.setSpacing(true);

        // Assemblage
        VerticalLayout container = new VerticalLayout(title, formLayout, buttonsLayout);
        container.setMaxWidth("900px");
        container.setWidthFull();
        container.setPadding(true);
        container.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        add(container);

        // Charger les données si modification
        if (!isNewEvent) {
            binder.readBean(event);
        }
    }

    private void configureFields() {
        // RÈGLE : Titre 5-100 caractères
        titreField.setWidthFull();
        titreField.setRequiredIndicatorVisible(true);
        titreField.setMinLength(5);
        titreField.setMaxLength(100);
        titreField.setHelperText("Entre 5 et 100 caractères");

        // RÈGLE : Description max 1000 caractères
        descriptionField.setWidthFull();
        descriptionField.setMaxLength(1000);
        descriptionField.setHelperText("Maximum 1000 caractères");

        // Catégorie
        categorieField.setWidthFull();
        categorieField.setItems(EventCategory.values());
        categorieField.setItemLabelGenerator(EventCategory::getLabel);
        categorieField.setRequiredIndicatorVisible(true);

        // RÈGLE : Date de début doit être dans le futur
        dateDebutField.setWidthFull();
        dateDebutField.setRequiredIndicatorVisible(true);
        dateDebutField.setMin(LocalDateTime.now());
        dateDebutField.setHelperText("Doit être dans le futur");

        // RÈGLE : Date de fin après date de début
        dateFinField.setWidthFull();
        dateFinField.setRequiredIndicatorVisible(true);

        // Synchroniser les dates
        dateDebutField.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                dateFinField.setMin(e.getValue());
            }
        });

        lieuField.setWidthFull();
        lieuField.setRequiredIndicatorVisible(true);

        villeField.setWidthFull();
        villeField.setRequiredIndicatorVisible(true);

        // RÈGLE : Capacité > 0
        capaciteField.setWidthFull();
        capaciteField.setMin(1);
        capaciteField.setStepButtonsVisible(true);
        capaciteField.setRequiredIndicatorVisible(true);
        capaciteField.setHelperText("Au moins 1 place");

        // RÈGLE : Prix >= 0
        prixField.setWidthFull();
        prixField.setMin(0);
        prixField.setRequiredIndicatorVisible(true);
        prixField.setHelperText("0 ou plus");

        imageUrlField.setWidthFull();
        imageUrlField.setPlaceholder("https://example.com/image.jpg");
    }

    private void configureBinder() {
        // Validation et liaison
        binder.forField(titreField)
                .asRequired("Le titre est obligatoire")
                .withValidator(t -> t.length() >= 5 && t.length() <= 100,
                        "Le titre doit contenir entre 5 et 100 caractères")
                .bind(Event::getTitre, Event::setTitre);

        binder.forField(descriptionField)
                .withValidator(d -> d == null || d.length() <= 1000,
                        "La description ne peut pas dépasser 1000 caractères")
                .bind(Event::getDescription, Event::setDescription);

        binder.forField(categorieField)
                .asRequired("La catégorie est obligatoire")
                .bind(Event::getCategorie, Event::setCategorie);

        binder.forField(dateDebutField)
                .asRequired("La date de début est obligatoire")
                .withValidator(d -> d.isAfter(LocalDateTime.now()),
                        "La date de début doit être dans le futur")
                .bind(Event::getDateDebut, Event::setDateDebut);

        binder.forField(dateFinField)
                .asRequired("La date de fin est obligatoire")
                .withValidator(d -> {
                    LocalDateTime debut = dateDebutField.getValue();
                    return debut == null || d.isAfter(debut);
                }, "La date de fin doit être après la date de début")
                .bind(Event::getDateFin, Event::setDateFin);

        binder.forField(lieuField)
                .asRequired("Le lieu est obligatoire")
                .bind(Event::getLieu, Event::setLieu);

        binder.forField(villeField)
                .asRequired("La ville est obligatoire")
                .bind(Event::getVille, Event::setVille);

        binder.forField(capaciteField)
                .asRequired("La capacité est obligatoire")
                .withValidator(c -> c > 0, "La capacité doit être supérieure à 0")
                .bind(Event::getCapaciteMax, Event::setCapaciteMax);

        binder.forField(prixField)
                .asRequired("Le prix est obligatoire")
                .withValidator(p -> p >= 0, "Le prix doit être supérieur ou égal à 0")
                .bind(Event::getPrixUnitaire, Event::setPrixUnitaire);

        binder.forField(imageUrlField)
                .bind(Event::getImageUrl, Event::setImageUrl);
    }

    private FormLayout createFormLayout() {
        FormLayout formLayout = new FormLayout();

        formLayout.add(
                titreField, categorieField,
                dateDebutField, dateFinField,
                lieuField, villeField,
                capaciteField, prixField,
                imageUrlField, descriptionField
        );

        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        // Description sur toute la largeur
        formLayout.setColspan(descriptionField, 2);

        return formLayout;
    }

    private void configureButtons() {
        saveDraftButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveDraftButton.addClickListener(e -> saveAsDraft());

        publishButton.addThemeVariants(ButtonVariant.LUMO_SUCCESS);
        publishButton.addClickListener(e -> publishEvent());

        cancelButton.addClickListener(e ->
                getUI().ifPresent(ui -> ui.navigate("organizer/events"))
        );
    }

    private void saveAsDraft() {
        try {
            // Valider le formulaire
            if (!binder.isValid()) {
                showError("Veuillez corriger les erreurs dans le formulaire");
                return;
            }

            // Écrire les valeurs dans l'objet Event
            binder.writeBean(event);

            if (isNewEvent) {
                // Créer un nouvel événement
                eventService.createEvent(event, currentUserId);
                showSuccess("Événement créé en brouillon");
            } else {
                // Mettre à jour l'événement existant
                eventService.updateEvent(event.getId(), event, currentUserId);
                showSuccess("Événement mis à jour");
            }

            // Redirection
            getUI().ifPresent(ui -> ui.navigate("organizer/events"));

        } catch (ValidationException e) {
            showError("Erreur de validation du formulaire");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void publishEvent() {
        try {
            // Valider le formulaire
            if (!binder.isValid()) {
                showError("Veuillez corriger les erreurs dans le formulaire");
                return;
            }

            // Écrire les valeurs
            binder.writeBean(event);

            if (isNewEvent) {
                // Créer puis publier
                Event savedEvent = eventService.createEvent(event, currentUserId);
                eventService.publierEvent(savedEvent.getId(), currentUserId);
                showSuccess("Événement créé et publié avec succès");
            } else {
                // Mettre à jour puis publier
                eventService.updateEvent(event.getId(), event, currentUserId);
                if (event.getStatut() != EventStatus.PUBLIE) {
                    eventService.publierEvent(event.getId(), currentUserId);
                }
                showSuccess("Événement mis à jour et publié");
            }

            // Redirection
            getUI().ifPresent(ui -> ui.navigate("organizer/events"));

        } catch (ValidationException e) {
            showError("Erreur de validation du formulaire");
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