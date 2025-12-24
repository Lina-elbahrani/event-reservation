package ma.event.eventreservationsystem.views.admin;

import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.service.ReservationService;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Route("admin/reservations")
@PageTitle("Gestion des Réservations | Event Reservation System")
public class AllReservationsView extends VerticalLayout {

    private final ReservationService reservationService;

    private final Grid<Reservation> grid = new Grid<>(Reservation.class, false);
    private final ComboBox<ReservationStatus> statusFilter = new ComboBox<>("Filtrer par statut");
    private final TextField searchField = new TextField("Rechercher");

    public AllReservationsView(@Autowired ReservationService reservationService) {
        this.reservationService = reservationService;

        setSizeFull();
        setPadding(true);

        // Titre
        H1 title = new H1("Gestion de Toutes les Réservations");
        title.getStyle().set("color", "#1976D2");

        // Statistiques globales
        VerticalLayout statsSection = createStatsSection();

        // Filtres
        configureFilters();
        HorizontalLayout filtersLayout = new HorizontalLayout(statusFilter, searchField);
        filtersLayout.setDefaultVerticalComponentAlignment(Alignment.END);

        // Grille
        configureGrid();

        // Assemblage
        add(title, statsSection, filtersLayout, grid);

        // Charger les données
        updateList();
    }

    private VerticalLayout createStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setPadding(true);
        section.setSpacing(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H2 statsTitle = new H2("Statistiques Globales");
        statsTitle.getStyle().set("margin", "0 0 15px 0");

        Map<String, Object> stats = reservationService.getStatistiquesReservation();

        HorizontalLayout statsRow = new HorizontalLayout();
        statsRow.setWidthFull();
        statsRow.setSpacing(true);

        Paragraph total = new Paragraph(
                "📝 Total : " + stats.get("nombreTotalReservations") + " réservations"
        );
        Paragraph confirmees = new Paragraph(
                "✅ Confirmées : " + stats.get("reservationsConfirmees")
        );
        Paragraph enAttente = new Paragraph(
                "⏳ En attente : " + stats.get("reservationsEnAttente")
        );
        Paragraph annulees = new Paragraph(
                "❌ Annulées : " + stats.get("reservationsAnnulees")
        );
        Paragraph revenus = new Paragraph(
                "💰 Revenus totaux : " + String.format("%.2f DH", stats.get("revenuTotal"))
        );

        statsRow.add(total, confirmees, enAttente, annulees, revenus);

        section.add(statsTitle, statsRow);

        return section;
    }

    private void configureFilters() {
        statusFilter.setItems(ReservationStatus.values());
        statusFilter.setItemLabelGenerator(ReservationStatus::getLabel);
        statusFilter.setClearButtonVisible(true);
        statusFilter.addValueChangeListener(e -> updateList());

        searchField.setPlaceholder("Code, utilisateur, événement...");
        searchField.setClearButtonVisible(true);
        searchField.addValueChangeListener(e -> updateList());
    }

    private void configureGrid() {
        grid.setSizeFull();

        // Colonne Code
        grid.addColumn(Reservation::getCodeReservation)
                .setHeader("Code")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Utilisateur
        grid.addColumn(res ->
                        res.getUtilisateur().getPrenom() + " " + res.getUtilisateur().getNom()
                )
                .setHeader("Utilisateur")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Email
        grid.addColumn(res -> res.getUtilisateur().getEmail())
                .setHeader("Email")
                .setSortable(true);

        // Colonne Événement
        grid.addColumn(res -> res.getEvenement().getTitre())
                .setHeader("Événement")
                .setSortable(true)
                .setAutoWidth(true);

        // Colonne Date réservation
        grid.addColumn(res -> res.getDateReservation().format(
                        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                ))
                .setHeader("Date réservation")
                .setSortable(true);

        // Colonne Places
        grid.addColumn(Reservation::getNombrePlaces)
                .setHeader("Places")
                .setSortable(true);

        // Colonne Montant
        grid.addColumn(res -> res.getMontantTotal() + " DH")
                .setHeader("Montant")
                .setSortable(true);

        // Colonne Statut avec badge
        grid.addComponentColumn(reservation -> {
            Span badge = new Span(reservation.getStatut().getLabel());
            String color = switch (reservation.getStatut()) {
                case CONFIRMEE -> "#28A745";
                case EN_ATTENTE -> "#FFA500";
                case ANNULEE -> "#DC3545";
            };
            badge.getStyle()
                    .set("background", color)
                    .set("color", "white")
                    .set("padding", "5px 10px")
                    .set("border-radius", "12px")
                    .set("font-size", "0.85em");
            return badge;
        }).setHeader("Statut");
    }

    private void updateList() {
        try {
            // Créer une méthode findAll() dans ReservationService si elle n'existe pas
            // Pour l'instant, on utilise une astuce :
            List<Reservation> reservations = reservationService.getStatistiquesReservation() != null
                    ? getAllReservations()
                    : List.of();

            // Filtrage par statut
            if (statusFilter.getValue() != null) {
                reservations = reservations.stream()
                        .filter(r -> r.getStatut() == statusFilter.getValue())
                        .toList();
            }

            // Recherche
            if (!searchField.isEmpty()) {
                String search = searchField.getValue().toLowerCase();
                reservations = reservations.stream()
                        .filter(r ->
                                r.getCodeReservation().toLowerCase().contains(search) ||
                                        r.getUtilisateur().getNom().toLowerCase().contains(search) ||
                                        r.getUtilisateur().getPrenom().toLowerCase().contains(search) ||
                                        r.getEvenement().getTitre().toLowerCase().contains(search)
                        )
                        .toList();
            }

            grid.setItems(reservations);

        } catch (Exception e) {
            grid.setItems();
        }
    }

    // Méthode temporaire - À REMPLACER par reservationService.findAll()
    private List<Reservation> getAllReservations() {
        // TODO: Ajouter cette méthode dans ReservationService et ReservationRepository
        // Pour l'instant, retourner une liste vide
        // Quand vous ajouterez la méthode dans le service, supprimez cette méthode
        return List.of();
    }
}