package ma.event.eventreservationsystem.views.admin;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.ColumnTextAlign;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.*;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.theme.lumo.LumoUtility;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Route("admin/reservations")
@PageTitle("Gestion des Réservations | Event Reservation System")
public class AllReservationsView extends VerticalLayout {

    private final ReservationService reservationService;

    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);
    private final ComboBox<ReservationStatus> statusFilter = new ComboBox<>();
    private final TextField searchField = new TextField();

    public AllReservationsView(@Autowired ReservationService reservationService) {
        this.reservationService = reservationService;

        // Configuration de la page (Fond gris clair pour faire ressortir les cartes)
        addClassNames(LumoUtility.Background.CONTRAST_5);
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // 1. En-tête + Statistiques
        VerticalLayout headerSection = createHeaderAndStats();

        // 2. Zone principale (Filtres + Grille) dans une carte blanche
        VerticalLayout mainCard = createMainContentCard();

        add(headerSection, mainCard);

        updateList(); // Chargement initial
    }

    // --- SECTION EN-TÊTE & STATS ---

    private VerticalLayout createHeaderAndStats() {
        // Titre
        H2 title = new H2("Gestion des Réservations");
        title.addClassNames(LumoUtility.TextColor.PRIMARY, LumoUtility.Margin.Bottom.NONE);
        Paragraph subtitle = new Paragraph("Suivi et gestion de toutes les réservations de la plateforme.");
        subtitle.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.Margin.Top.NONE);

        // Statistiques
        HorizontalLayout statsLayout = new HorizontalLayout();
        statsLayout.setWidthFull();
        statsLayout.setSpacing(true);

        // Récupération des données
        Map<String, Object> stats = reservationService.getStatistiquesReservation();

        if (stats != null) {
            statsLayout.add(
                    createStatCard("Total", stats.get("nombreTotalReservations").toString(), VaadinIcon.TICKET, "blue"),
                    createStatCard("Confirmées", stats.get("reservationsConfirmees").toString(), VaadinIcon.CHECK_CIRCLE, "green"),
                    createStatCard("En Attente", stats.get("reservationsEnAttente").toString(), VaadinIcon.CLOCK, "orange"),
                    createStatCard("Chiffre d'Affaires", String.format("%.0f DH", stats.get("revenuTotal")), VaadinIcon.WALLET, "purple")
            );
        }

        VerticalLayout wrapper = new VerticalLayout(title, subtitle, statsLayout);
        wrapper.setPadding(false);
        wrapper.setSpacing(true);
        return wrapper;
    }

    private Component createStatCard(String title, String value, VaadinIcon icon, String color) {
        Div card = new Div();
        card.addClassNames(
                LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL,
                LumoUtility.Padding.MEDIUM, LumoUtility.Display.FLEX, LumoUtility.AlignItems.CENTER, LumoUtility.Gap.MEDIUM
        );
        card.setWidthFull(); // Pour que les cartes prennent toute la largeur disponible

        Icon i = icon.create();
        i.setSize("24px");
        i.setColor(getColorHex(color));

        Div iconBox = new Div(i);
        iconBox.getStyle().set("background-color", getBgColorHex(color));
        iconBox.addClassNames(LumoUtility.Padding.SMALL, LumoUtility.BorderRadius.MEDIUM);

        VerticalLayout textLayout = new VerticalLayout();
        textLayout.setSpacing(false);
        textLayout.setPadding(false);

        Span valSpan = new Span(value);
        valSpan.addClassNames(LumoUtility.FontWeight.EXTRABOLD, LumoUtility.FontSize.XLARGE);
        Span titleSpan = new Span(title);
        titleSpan.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.XSMALL, LumoUtility.FontWeight.BOLD);

        textLayout.add(titleSpan, valSpan);
        card.add(iconBox, textLayout);
        return card;
    }

    // --- SECTION PRINCIPALE (FILTRES + GRILLE) ---

    private VerticalLayout createMainContentCard() {
        VerticalLayout card = new VerticalLayout();
        card.addClassNames(LumoUtility.Background.BASE, LumoUtility.BorderRadius.LARGE, LumoUtility.BoxShadow.SMALL);
        card.setPadding(true);
        card.setSizeFull();

        // Barre d'outils (Filtres)
        HorizontalLayout toolbar = createToolbar();

        // Configuration de la grille
        configureGrid();

        card.add(toolbar, grid);
        return card;
    }

    private HorizontalLayout createToolbar() {
        // Configuration Recherche
        searchField.setPlaceholder("Rechercher (Code, Nom, Event)...");
        searchField.setPrefixComponent(VaadinIcon.SEARCH.create());
        searchField.setClearButtonVisible(true);
        searchField.setValueChangeMode(ValueChangeMode.LAZY);
        searchField.setWidth("300px");
        searchField.addValueChangeListener(e -> updateList());

        // Configuration Filtre Statut
        statusFilter.setPlaceholder("Filtrer par statut");
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());

        HorizontalLayout toolbar = new HorizontalLayout(searchField, statusFilter);
        toolbar.setWidthFull();
        toolbar.addClassNames(LumoUtility.Padding.Bottom.MEDIUM);
        return toolbar;
    }

    private void configureGrid() {
        grid.setSizeFull();
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER);

        // Colonne Code (Gras)
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setAutoWidth(true)
                .setFlexGrow(0)
                .setSortable(true);

        // Colonne Utilisateur (Nom + Email)
        grid.addColumn(new ComponentRenderer<>(r -> {
            Span name = new Span(r.getUtilisateur().getPrenom() + " " + r.getUtilisateur().getNom());
            name.addClassNames(LumoUtility.FontWeight.BOLD);
            Span email = new Span(r.getUtilisateur().getEmail());
            email.addClassNames(LumoUtility.FontSize.XSMALL, LumoUtility.TextColor.SECONDARY);

            VerticalLayout layout = new VerticalLayout(name, email);
            layout.setSpacing(false);
            layout.setPadding(false);
            return layout;
        })).setHeader("Client").setAutoWidth(true);

        // Colonne Événement
        grid.addColumn(r -> r.getEvenement().getTitre())
                .setHeader("Événement")
                .setSortable(true);

        // Colonne Date
        grid.addColumn(res -> res.getDateReservation().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .setHeader("Date Réservation")
                .setAutoWidth(true);

        // Colonne Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setTextAlign(ColumnTextAlign.CENTER)
                .setAutoWidth(true);

        // Colonne Montant (Aligné à droite, en gras)
        grid.addColumn(new ComponentRenderer<>(r -> {
            Span amount = new Span(String.format("%.2f DH", r.getMontantTotal()));
            amount.addClassNames(LumoUtility.FontWeight.BOLD, LumoUtility.TextColor.SUCCESS);
            return amount;
        })).setHeader("Montant").setTextAlign(ColumnTextAlign.END);

        // Colonne Statut (Badge)
        grid.addColumn(new ComponentRenderer<>(this::createStatusBadge))
                .setHeader("Statut")
                .setAutoWidth(true)
                .setTextAlign(ColumnTextAlign.CENTER);
    }

    // --- HELPER : BADGES DE STATUT ---

    private Span createStatusBadge(Reservation reservation) {
        Span badge = new Span(reservation.getStatut().getLabel());
        String theme;

        switch (reservation.getStatut()) {
            case CONFIRMEE:
                theme = "badge success"; // Vert
                break;
            case EN_ATTENTE:
                theme = "badge contrast"; // Gris/Noir (ou "badge" tout court pour bleu clair)
                break;
            case ANNULEE:
                theme = "badge error"; // Rouge
                break;
            default:
                theme = "badge";
        }

        badge.getElement().getThemeList().add(theme);
        return badge;
    }

    // --- LOGIQUE METIER ---

    private void updateList() {
        // 1. Récupération
        List<Reservation> reservations = reservationService.getAllReservations();

        // 2. Filtrage (Streams)
        if (statusFilter.getValue() != null) {
            reservations = reservations.stream()
                    .filter(r -> r.getStatut() == statusFilter.getValue())
                    .collect(Collectors.toList());
        }

        // 3. Recherche
        if (!searchField.isEmpty()) {
            String search = searchField.getValue().toLowerCase();
            reservations = reservations.stream()
                    .filter(r ->
                            (r.getCodeReservation() != null && r.getCodeReservation().toLowerCase().contains(search)) ||
                                    (r.getUtilisateur().getNom() != null && r.getUtilisateur().getNom().toLowerCase().contains(search)) ||
                                    (r.getUtilisateur().getPrenom() != null && r.getUtilisateur().getPrenom().toLowerCase().contains(search)) ||
                                    (r.getEvenement().getTitre() != null && r.getEvenement().getTitre().toLowerCase().contains(search))
                    )
                    .collect(Collectors.toList());
        }

        // 4. Update
        grid.setItems(reservations);
    }

    // Helpers Couleurs
    private String getColorHex(String color) {
        return switch (color) {
            case "blue" -> "#2563eb";
            case "green" -> "#16a34a";
            case "orange" -> "#ea580c";
            case "purple" -> "#9333ea";
            default -> "#000000";
        };
    }

    private String getBgColorHex(String color) {
        return switch (color) {
            case "blue" -> "#dbeafe";
            case "green" -> "#dcfce7";
            case "orange" -> "#ffedd5";
            case "purple" -> "#f3e8ff";
            default -> "#f3f4f6";
        };
    }
}