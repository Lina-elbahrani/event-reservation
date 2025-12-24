package ma.event.eventreservationsystem.service;

import ma.event.eventreservationsystem.entity.Reservation;
import ma.event.eventreservationsystem.entity.enums.ReservationStatus;
import java.util.List;
import java.util.Map;

public interface ReservationService {
    // Gestion des réservations
    Reservation createReservation(Reservation reservation, Long utilisateurId, Long evenementId);
    Reservation confirmerReservation(Long id, Long utilisateurId);
    void annulerReservation(Long id, Long utilisateurId);

    // Recherche et récupération
    Reservation findById(Long id);
    Reservation findByCode(String codeReservation);
    List<Reservation> findByUtilisateur(Long utilisateurId);
    List<Reservation> findByEvenement(Long evenementId);
    List<Reservation> findByUtilisateurAndStatut(Long utilisateurId, ReservationStatus statut);
    List<Reservation> findUpcomingReservationsByUser(Long utilisateurId);
    List<Reservation> findAll();

    // Statistiques
    Map<String, Object> getStatistiquesReservation();
    Map<String, Object> getRecapitulatifReservation(Long id);
}