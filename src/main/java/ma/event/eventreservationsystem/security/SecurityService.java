package ma.event.eventreservationsystem.security;

import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {

    private final UserRepository userRepository;

    public SecurityService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Récupère l'utilisateur actuellement connecté
     * @return L'utilisateur connecté
     * @throws RuntimeException si aucun utilisateur n'est connecté
     */
    public User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("Aucun utilisateur connecté");
        }

        Object principal = authentication.getPrincipal();
        String email;

        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        // Si l'utilisateur est anonyme
        if ("anonymousUser".equals(email)) {
            throw new RuntimeException("Aucun utilisateur connecté");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé: " + email));
    }

    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté
     */
    public boolean isUserAuthenticated() {
        try {
            getAuthenticatedUser();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Récupère l'email de l'utilisateur connecté
     * @return L'email ou null si non connecté
     */
    public String getAuthenticatedUserEmail() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}