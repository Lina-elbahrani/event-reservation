package ma.event.eventreservationsystem.views.publicviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.enums.EventCategory;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Route("events")
@PageTitle("Événements | Event Reservation System")
public class EventListView extends VerticalLayout {

    private final EventService eventService;

    private final Grid<Event> grid = new Grid<>(Event.class, false);

    // Filtres
    private final TextField searchField = new TextField("Recherche");
    private final ComboBox<EventCategory> categoryFilter = new ComboBox<>("Catégorie");
    private final TextField villeFilter = new TextField("Ville");
    private final DatePicker dateDebutFilter = new DatePicker("Date début");
    private final DatePicker dateFinFilter = new DatePicker("Date fin");
    private final NumberField prixMinFilter = new NumberField("Prix min");
    private final NumberField prixMaxFilter = new NumberField("Prix max");
    private final Button filterButton = new Button("Filtrer");
    private final Button resetButton = new Button("Réinitialiser");

    public EventListView(@Autowired EventService eventService) {
        this.eventService = eventService;

        setSizeFull();
        setPadding(true);

        // Titre
        H1 title = new H1("Tous les Événements");
        title.getStyle().set("color", "#1976D2");

        // Configuration des filtres
        configureFilters();

        // Layout des filtres
        HorizontalLayout filtersRow1 = new HorizontalLayout(
                searchField, categoryFilter, villeFilter
        );
        filtersRow1.setWidthFull();
        filtersRow1.setDefaultVerticalComponentAlignment(Alignment.END);

        HorizontalLayout filtersRow2 = new HorizontalLayout(
                dateDebutFilter, dateFinFilter, prixMinFilter, prixMaxFilter
        );
        filtersRow2.setWidthFull();
        filtersRow2.setDefaultVerticalComponentAlignment(Alignment.END);

        HorizontalLayout buttonsLayout = new HorizontalLayout(filterButton, resetButton);

        VerticalLayout filtersLayout = new VerticalLayout(
                filtersRow1, filtersRow2, buttonsLayout
        );
        filtersLayout.setPadding(true);
        filtersLayout.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        // Configuration de la grille
        configureGrid();

        // Assemblage
        add(title, filtersLayout, grid);

        // Charger les données initiales
        updateList();
    }

    private void configureFilters() {
        searchField.setPlaceholder("Rechercher par titre...");
        searchField.setClearButtonVisible(true);

        categoryFilter.setItems(EventCategory.values());
        categoryFilter.setItemLabelGenerator(EventCategory::getLabel);
        categoryFilter.setClearButtonVisible(true);

        villeFilter.setPlaceholder("Nom de la ville");
        villeFilter.setClearButtonVisible(true);

        prixMinFilter.setPlaceholder("0");
        prixMinFilter.setMin(0);

        prixMaxFilter.setPlaceholder("1000");
        prixMaxFilter.setMin(0);

        filterButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        filterButton.addClickListener(e -> updateList());

        resetButton.addClickListener(e -> resetFilters());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonnes
        grid.addColumn(Event::getTitre)
                .setHeader("Titre")
                .setSortable(true)
                .setAutoWidth(true);

        grid.addColumn(event -> event.getCategorie().getLabel())
                .setHeader("Catégorie")
                .setSortable(true);

        grid.addColumn(event -> event.getDateDebut().toLocalDate())
                .setHeader("Date")
                .setSortable(true);

        grid.addColumn(Event::getVille)
                .setHeader("Ville")
                .setSortable(true);

        grid.addColumn(event -> event.getPrixUnitaire() + " DH")
                .setHeader("Prix")
                .setSortable(true);

        grid.addColumn(event -> {
                    int dispo = event.getCapaciteMax() - 0; // TODO: calculer vraiment
                    return dispo + " / " + event.getCapaciteMax();
                })
                .setHeader("Places dispo")
                .setSortable(false);

        // Bouton détails
        grid.addComponentColumn(event -> {
            Button detailsButton = new Button("Voir détails");
            detailsButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            detailsButton.addClickListener(e ->
                    getUI().ifPresent(ui -> ui.navigate("event/" + event.getId()))
            );
            return detailsButton;
        }).setHeader("Actions");
    }

    private void updateList() {
        try {
            // Récupération des valeurs des filtres
            EventCategory category = categoryFilter.getValue();
            String ville = villeFilter.getValue();
            Double prixMin = prixMinFilter.getValue();
            Double prixMax = prixMaxFilter.getValue();

            LocalDateTime dateDebut = null;
            LocalDateTime dateFin = null;

            if (dateDebutFilter.getValue() != null) {
                dateDebut = dateDebutFilter.getValue().atStartOfDay();
            }
            if (dateFinFilter.getValue() != null) {
                dateFin = dateFinFilter.getValue().atTime(LocalTime.MAX);
            }

            // Recherche avec filtres
            List<Event> events;

            if (!searchField.isEmpty()) {
                // Recherche par titre
                events = eventService.searchByTitre(searchField.getValue());
            } else if (category != null || dateDebut != null || ville != null || prixMin != null) {
                // Recherche avancée
                events = eventService.searchEvents(category, dateDebut, dateFin, ville, prixMin, prixMax);
            } else {
                // Tous les événements disponibles
                events = eventService.findAvailableEvents();
            }

            // Filtrer uniquement les événements publiés
            events = events.stream()
                    .filter(e -> e.getStatut() == EventStatus.PUBLIE)
                    .toList();

            grid.setItems(events);

        } catch (Exception e) {
            grid.setItems();
        }
    }

    private void resetFilters() {
        searchField.clear();
        categoryFilter.clear();
        villeFilter.clear();
        dateDebutFilter.clear();
        dateFinFilter.clear();
        prixMinFilter.clear();
        prixMaxFilter.clear();
        updateList();
    }
}
