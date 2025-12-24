package ma.event.eventreservationsystem.service;

import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import java.util.List;
import java.util.Map;

public interface UserService {
    // Inscription et authentification
    User inscription(User user);
    User authenticate(String email, String password);

    // Gestion du profil
    User updateProfile(Long id, User user);
    void changePassword(Long id, String oldPassword, String newPassword);

    // Gestion du compte
    void toggleActif(Long id);
    void desactiverCompte(Long id);
    void activerCompte(Long id);

    // Recherche et récupération
    User findById(Long id);
    User findByEmail(String email);
    List<User> findAll();
    List<User> findByRole(UserRole role);
    List<User> findActifs();
    List<User> searchByNomOrPrenom(String keyword);

    // Statistiques
    Map<String, Object> getStatistiquesUtilisateur(Long id);
    long countByRole(UserRole role);
}