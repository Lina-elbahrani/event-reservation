package ma.event.eventreservationsystem.service;

import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.exception.ConflictException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest  // Lance l'application Spring pour les tests
class UserServiceTest {

    @Autowired
    private UserService userService;

    // ============================================
    // TEST 1 : Inscription réussie
    // ============================================
    @Test
    void testInscription_Success() {
        // ARRANGE : Préparer un utilisateur valide
        User user = User.builder()
                .nom("Dupont")
                .prenom("Jean")
                .email("jean.dupont.test@example.com")  // Email unique !
                .password("password123")  // Au moins 8 caractères
                .role(UserRole.CLIENT)
                .build();

        // ACT : Créer l'utilisateur
        User saved = userService.inscription(user);

        // ASSERT : Vérifier les résultats
        assertNotNull(saved.getId(), "L'ID doit être généré");
        assertEquals("Dupont", saved.getNom());
        assertEquals("jean.dupont.test@example.com", saved.getEmail());
        assertTrue(saved.getActif(), "Le compte doit être actif");
        assertNotEquals("password123", saved.getPassword(), "Le mot de passe doit être hashé");
    }

    // ============================================
    // TEST 2 : Email déjà utilisé (doit échouer)
    // ============================================
    @Test
    void testInscription_EmailDuplique() {
        // ARRANGE : Créer le premier utilisateur
        User user1 = User.builder()
                .nom("Martin")
                .prenom("Paul")
                .email("paul.martin.test@example.com")
                .password("password123")
                .role(UserRole.CLIENT)
                .build();

        userService.inscription(user1);

        // Créer un deuxième utilisateur avec le MÊME email
        User user2 = User.builder()
                .nom("Durand")
                .prenom("Marie")
                .email("paul.martin.test@example.com")  // ← Même email !
                .password("password456")
                .role(UserRole.CLIENT)
                .build();

        // ACT & ASSERT : Vérifier qu'une exception est levée
        assertThrows(ConflictException.class, () -> {
            userService.inscription(user2);
        }, "Une exception ConflictException doit être levée");
    }

    // ============================================
    // TEST 3 : Mot de passe trop court (doit échouer)
    // ============================================
    @Test
    void testInscription_MotDePasseTropCourt() {
        // ARRANGE : Utilisateur avec mot de passe < 8 caractères
        User user = User.builder()
                .nom("Bernard")
                .prenom("Sophie")
                .email("sophie.bernard.test@example.com")
                .password("pass")  // ← Seulement 4 caractères !
                .role(UserRole.CLIENT)
                .build();

        // ACT & ASSERT : Vérifier qu'une exception est levée
        assertThrows(Exception.class, () -> {
            userService.inscription(user);
        }, "Le mot de passe doit avoir au moins 8 caractères");
    }

    // ============================================
    // TEST 4 : Authentification réussie
    // ============================================
    @Test
    void testAuthentication_Success() {
        // ARRANGE : Créer un utilisateur
        User user = User.builder()
                .nom("Leroy")
                .prenom("Thomas")
                .email("thomas.leroy.test@example.com")
                .password("password123")
                .role(UserRole.CLIENT)
                .build();

        userService.inscription(user);

        // ACT : Essayer de se connecter
        User authenticated = userService.authenticate(
                "thomas.leroy.test@example.com",
                "password123"
        );

        // ASSERT : Vérifier que l'authentification réussit
        assertNotNull(authenticated);
        assertEquals("Leroy", authenticated.getNom());
    }

    // ============================================
    // TEST 5 : Authentification avec mauvais mot de passe
    // ============================================
    @Test
    void testAuthentication_MauvaisMotDePasse() {
        // ARRANGE : Créer un utilisateur
        User user = User.builder()
                .nom("Petit")
                .prenom("Lucie")
                .email("lucie.petit.test@example.com")
                .password("password123")
                .role(UserRole.CLIENT)
                .build();

        userService.inscription(user);

        // ACT & ASSERT : Tenter de se connecter avec mauvais mot de passe
        assertThrows(Exception.class, () -> {
            userService.authenticate("lucie.petit.test@example.com", "wrongpassword");
        }, "L'authentification doit échouer avec un mauvais mot de passe");
    }
}