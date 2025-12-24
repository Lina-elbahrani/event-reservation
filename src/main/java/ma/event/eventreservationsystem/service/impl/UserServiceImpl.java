package ma.event.eventreservationsystem.service.impl;

import lombok.RequiredArgsConstructor;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.repository.UserRepository;
import ma.event.eventreservationsystem.repository.EventRepository;
import ma.event.eventreservationsystem.repository.ReservationRepository;
import ma.event.eventreservationsystem.service.UserService;
import ma.event.eventreservationsystem.exception.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public User inscription(User user) {
        // RÈGLE : Email unique
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ConflictException("Cet email est déjà utilisé");
        }

        // RÈGLE : Mot de passe minimum 8 caractères
        if (user.getPassword().length() < 8) {
            throw new BadRequestException("Le mot de passe doit contenir au moins 8 caractères");
        }

        // Hashage du mot de passe pour la sécurité
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setActif(true);

        // Rôle par défaut CLIENT si non spécifié
        if (user.getRole() == null) {
            user.setRole(UserRole.CLIENT);
        }

        return userRepository.save(user);
    }

    @Override
    public User authenticate(String email, String password) {
        // Recherche de l'utilisateur par email
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Email ou mot de passe incorrect"));

        // Vérification du mot de passe
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UnauthorizedException("Email ou mot de passe incorrect");
        }

        // RÈGLE : Compte doit être actif
        if (!user.getActif()) {
            throw new UnauthorizedException("Compte désactivé");
        }

        return user;
    }

    @Override
    public User updateProfile(Long id, User updatedUser) {
        User user = findById(id);

        // Mise à jour des champs autorisés
        user.setNom(updatedUser.getNom());
        user.setPrenom(updatedUser.getPrenom());
        user.setTelephone(updatedUser.getTelephone());

        // Vérifier si l'email a changé et s'il est unique
        if (!user.getEmail().equals(updatedUser.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new ConflictException("Cet email est déjà utilisé");
            }
            user.setEmail(updatedUser.getEmail());
        }

        return userRepository.save(user);
    }

    @Override
    public void changePassword(Long id, String oldPassword, String newPassword) {
        User user = findById(id);

        // Vérifier l'ancien mot de passe
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BadRequestException("Ancien mot de passe incorrect");
        }

        // Valider le nouveau mot de passe
        if (newPassword.length() < 8) {
            throw new BadRequestException("Le nouveau mot de passe doit contenir au moins 8 caractères");
        }

        // Mettre à jour le mot de passe
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public void toggleActif(Long id) {
        User user = findById(id);
        user.setActif(!user.getActif());
        userRepository.save(user);
    }

    @Override
    public void desactiverCompte(Long id) {
        User user = findById(id);
        user.setActif(false);
        userRepository.save(user);
    }

    @Override
    public void activerCompte(Long id) {
        User user = findById(id);
        user.setActif(true);
        userRepository.save(user);
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findByRole(UserRole role) {
        return userRepository.findByRole(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findActifs() {
        return userRepository.findByActif(true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> searchByNomOrPrenom(String keyword) {
        return userRepository.searchByNomOrPrenom(keyword);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getStatistiquesUtilisateur(Long id) {
        User user = findById(id);
        Map<String, Object> stats = new HashMap<>();

        // Statistiques selon le rôle
        if (user.getRole() == UserRole.ORGANIZER || user.getRole() == UserRole.ADMIN) {
            // Nombre d'événements créés
            long nbEvents = eventRepository.findByOrganisateur(user).size();
            stats.put("nombreEvenementsCreés", nbEvents);
        }

        // Nombre de réservations
        long nbReservations = reservationRepository.countByUtilisateur(user);
        stats.put("nombreReservations", nbReservations);

        // Montant total dépensé
        Double montantTotal = reservationRepository.calculateTotalAmountByUser(id);
        stats.put("montantTotalDepense", montantTotal);

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByRole(UserRole role) {
        return userRepository.countByRole(role);
    }
}