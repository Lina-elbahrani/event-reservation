package ma.event.eventreservationsystem.repository;

import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Trouver un utilisateur par email
    Optional<User> findByEmail(String email);

    // Trouver tous les utilisateurs actifs par rôle
    List<User> findByActifAndRole(Boolean actif, UserRole role);

    // Vérifier l'existence d'un email
    boolean existsByEmail(String email);

    // Trouver les utilisateurs par nom ou prénom (recherche insensible à la casse)
    @Query("SELECT u FROM User u WHERE LOWER(u.nom) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(u.prenom) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<User> searchByNomOrPrenom(@Param("keyword") String keyword);

    // Compter les utilisateurs par rôle
    long countByRole(UserRole role);

    // Trouver tous les utilisateurs par rôle
    List<User> findByRole(UserRole role);

    // Trouver tous les utilisateurs actifs
    List<User> findByActif(Boolean actif);
}