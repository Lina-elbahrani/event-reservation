package ma.event.eventreservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import ma.event.eventreservationsystem.entity.Event;
import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.EventStatus;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.EventService;
import ma.event.eventreservationsystem.service.ReservationService;
import ma.event.eventreservationsystem.service.UserService;
import ma.event.eventreservationsystem.exception.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserService userService;
    private final EventService eventService;

    @Override
    public Reservation createReservation(Reservation reservation, Long utilisateurId, Long evenementId) {
        User utilisateur = userService.findById(utilisateurId);
        Event evenement = eventService.findById(evenementId);

        // RÈGLE 1 : Vérifier que l'événement est publié
        if (evenement.getStatut() != EventStatus.PUBLIE) {
            throw new BusinessException("Cet événement n'est pas disponible pour les réservations");
        }

        // RÈGLE 2 : Vérifier que l'événement n'est pas terminé
        if (evenement.getStatut() == EventStatus.TERMINE) {
            throw new BusinessException("Impossible de réserver pour un événement terminé");
        }

        // RÈGLE 3 : Vérifier que l'événement n'est pas dans le passé
        if (evenement.getDateDebut().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Impossible de réserver pour un événement passé");
        }

        // RÈGLE 4 : Une réservation ne peut pas dépasser 10 places
        if (reservation.getNombrePlaces() > 10) {
            throw new BusinessException("Une réservation ne peut pas dépasser 10 places");
        }

        if (reservation.getNombrePlaces() < 1) {
            throw new BadRequestException("Le nombre de places doit être au moins 1");
        }

        // RÈGLE 5 : Vérifier la disponibilité des places
        int placesDisponibles = eventService.getPlacesDisponibles(evenementId);
        if (reservation.getNombrePlaces() > placesDisponibles) {
            throw new ConflictException(
                    String.format("Places insuffisantes. Disponibles : %d, Demandées : %d",
                            placesDisponibles, reservation.getNombrePlaces())
            );
        }

        // RÈGLE 6 : Le nombre total de places réservées ne peut pas dépasser la capacité maximale
        Integer placesReservees = reservationRepository.countTotalPlacesReserveesForEvent(evenementId);
        if (placesReservees + reservation.getNombrePlaces() > evenement.getCapaciteMax()) {
            throw new ConflictException("La capacité maximale de l'événement serait dépassée");
        }

        // Configuration de la réservation
        reservation.setUtilisateur(utilisateur);
        reservation.setEvenement(evenement);
        reservation.setStatut(ReservationStatus.EN_ATTENTE);

        // RÈGLE 7 : Le montant total = nombre de places × prix unitaire
        double montantTotal = evenement.getPrixUnitaire() * reservation.getNombrePlaces();
        reservation.setMontantTotal(montantTotal);

        // RÈGLE 8 : Le code de réservation doit être unique et au format EVT-XXXXX
        String codeReservation = genererCodeUniqueReservation();
        reservation.setCodeReservation(codeReservation);

        return reservationRepository.save(reservation);
    }

    @Override
    public Reservation confirmerReservation(Long id, Long utilisateurId) {
        Reservation reservation = findById(id);

        // Vérifier que l'utilisateur est bien le propriétaire
        if (!reservation.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour confirmer cette réservation");
        }

        // Vérifier que la réservation est en attente
        if (reservation.getStatut() != ReservationStatus.EN_ATTENTE) {
            throw new BusinessException("Seules les réservations en attente peuvent être confirmées");
        }

        reservation.setStatut(ReservationStatus.CONFIRMEE);
        return reservationRepository.save(reservation);
    }

    @Override
    public void annulerReservation(Long id, Long utilisateurId) {
        Reservation reservation = findById(id);

        // Vérifier que l'utilisateur est bien le propriétaire
        if (!reservation.getUtilisateur().getId().equals(utilisateurId)) {
            throw new ForbiddenException("Vous n'avez pas les droits pour annuler cette réservation");
        }

        // Vérifier que la réservation n'est pas déjà annulée
        if (reservation.getStatut() == ReservationStatus.ANNULEE) {
            throw new BusinessException("Cette réservation est déjà annulée");
        }

        // RÈGLE 9 : Les réservations peuvent être annulées jusqu'à 48h avant l'événement
        LocalDateTime dateEvenement = reservation.getEvenement().getDateDebut();
        LocalDateTime maintenant = LocalDateTime.now();
        long heuresRestantes = ChronoUnit.HOURS.between(maintenant, dateEvenement);

        if (heuresRestantes < 48) {
            throw new BusinessException(
                    "Impossible d'annuler : les réservations doivent être annulées au moins 48h avant l'événement"
            );
        }

        reservation.setStatut(ReservationStatus.ANNULEE);
        reservationRepository.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findById(Long id) {
        return reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec l'ID : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Reservation findByCode(String codeReservation) {
        return reservationRepository.findByCodeReservation(codeReservation)
                .orElseThrow(() -> new ResourceNotFoundException("Réservation non trouvée avec le code : " + codeReservation));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByUtilisateur(Long utilisateurId) {
        User utilisateur = userService.findById(utilisateurId);
        return reservationRepository.findByUtilisateur(utilisateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByEvenement(Long evenementId) {
        Event evenement = eventService.findById(evenementId);
        return reservationRepository.findByEvenement(evenement);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findByUtilisateurAndStatut(Long utilisateurId, ReservationStatus statut) {
        return reservationRepository.findByUtilisateurIdAndStatut(utilisateurId, statut);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findUpcomingReservationsByUser(Long utilisateurId) {
        return reservationRepository.findUpcomingReservationsByUser(utilisateurId, LocalDateTime.now());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesReservation() {
        Map<String, Object> stats = new HashMap<>();

        // Nombre total de réservations
        long totalReservations = reservationRepository.count();
        stats.put("nombreTotalReservations", totalReservations);

        // Réservations par statut (utilisation des Streams Java)
        List<Reservation> allReservations = reservationRepository.findAll();

        long enAttente = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.EN_ATTENTE)
                .count();
        long confirmees = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .count();
        long annulees = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.ANNULEE)
                .count();

        stats.put("reservationsEnAttente", enAttente);
        stats.put("reservationsConfirmees", confirmees);
        stats.put("reservationsAnnulees", annulees);

        // Montant total des revenus (réservations confirmées uniquement)
        double revenuTotal = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToDouble(Reservation::getMontantTotal)
                .sum();
        stats.put("revenuTotal", revenuTotal);

        // Nombre total de places réservées (confirmées)
        int totalPlaces = allReservations.stream()
                .filter(r -> r.getStatut() == ReservationStatus.CONFIRMEE)
                .mapToInt(Reservation::getNombrePlaces)
                .sum();
        stats.put("nombreTotalPlaces", totalPlaces);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getRecapitulatifReservation(Long id) {
        Reservation reservation = findById(id);
        Map<String, Object> recap = new HashMap<>();

        // Informations réservation
        recap.put("codeReservation", reservation.getCodeReservation());
        recap.put("dateReservation", reservation.getDateReservation());
        recap.put("nombrePlaces", reservation.getNombrePlaces());
        recap.put("montantTotal", reservation.getMontantTotal());
        recap.put("statut", reservation.getStatut().getLabel());
        recap.put("commentaire", reservation.getCommentaire());

        // Informations utilisateur
        User utilisateur = reservation.getUtilisateur();
        Map<String, String> infoUtilisateur = new HashMap<>();
        infoUtilisateur.put("nom", utilisateur.getNom());
        infoUtilisateur.put("prenom", utilisateur.getPrenom());
        infoUtilisateur.put("email", utilisateur.getEmail());
        infoUtilisateur.put("telephone", utilisateur.getTelephone());
        recap.put("utilisateur", infoUtilisateur);

        // Informations événement
        Event evenement = reservation.getEvenement();
        Map<String, Object> infoEvenement = new HashMap<>();
        infoEvenement.put("titre", evenement.getTitre());
        infoEvenement.put("categorie", evenement.getCategorie().getLabel());
        infoEvenement.put("dateDebut", evenement.getDateDebut());
        infoEvenement.put("dateFin", evenement.getDateFin());
        infoEvenement.put("lieu", evenement.getLieu());
        infoEvenement.put("ville", evenement.getVille());
        infoEvenement.put("prixUnitaire", evenement.getPrixUnitaire());
        recap.put("evenement", infoEvenement);

        return recap;
    }

    // Méthode privée pour générer un code unique de réservation
    // Format : EVT-XXXXX (5 chiffres aléatoires)
    private String genererCodeUniqueReservation() {
        String code;
        Random random = new Random();

        // Boucle jusqu'à trouver un code unique
        do {
            // Générer un nombre entre 10000 et 99999
            int randomNum = 10000 + random.nextInt(90000);
            code = "EVT-" + randomNum;
        } while (reservationRepository.existsByCodeReservation(code));

        return code;
    }
    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAll() {
        return reservationRepository.findAll();
    }
}