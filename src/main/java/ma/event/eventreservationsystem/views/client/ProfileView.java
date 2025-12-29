package ma.event.eventreservationsystem.views.client;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Route("profile")
@PageTitle("Mon Profil | Event Reservation System")
public class ProfileView extends VerticalLayout {

    private final UserService userService;

    // TODO: Récupérer de la session (Spring Security)
    private final Long currentUserId = 1L;

    private User currentUser;

    // Formulaire de profil
    private final TextField nomField = new TextField("Nom");
    private final TextField prenomField = new TextField("Prénom");
    private final EmailField emailField = new EmailField("Email");
    private final TextField telephoneField = new TextField("Téléphone");
    private final Button saveProfileButton = new Button("Enregistrer les modifications");

    // Formulaire de changement de mot de passe
    private final PasswordField oldPasswordField = new PasswordField("Ancien mot de passe");
    private final PasswordField newPasswordField = new PasswordField("Nouveau mot de passe");
    private final PasswordField confirmPasswordField = new PasswordField("Confirmer le nouveau mot de passe");
    private final Button changePasswordButton = new Button("Changer le mot de passe");

    // Binder pour la liaison données/formulaire
    private final Binder<User> binder = new Binder<>(User.class);

    public ProfileView(@Autowired UserService userService) {
        this.userService = userService;

        // --- CONFIGURATION DU LAYOUT PRINCIPAL ---
        setSizeFull();
        setPadding(true);
        setSpacing(true);

        // ✅ C'est ici que l'on centre le contenu horizontalement
        setAlignItems(FlexComponent.Alignment.CENTER);
        // -----------------------------------------------------

        // Charger les données utilisateur
        loadUserData();

        // Titre
        H1 title = new H1("Mon Profil");
        title.getStyle().set("color", "#1976D2");

        // Section informations personnelles
        VerticalLayout profileSection = createProfileSection();

        // Section changement de mot de passe
        VerticalLayout passwordSection = createPasswordSection();

        // Section statistiques
        VerticalLayout statsSection = createStatsSection();

        // Section désactivation de compte
        VerticalLayout deactivateSection = createDeactivateSection();

        // Assemblage
        add(title, profileSection, passwordSection, statsSection, deactivateSection);
    }

    private void loadUserData() {
        try {
            currentUser = userService.findById(currentUserId);
            binder.readBean(currentUser);
        } catch (Exception e) {
            showError("Erreur lors du chargement du profil");
        }
    }

    private VerticalLayout createProfileSection() {
        VerticalLayout section = new VerticalLayout();
        section.setMaxWidth("600px");
        section.setWidth("100%"); // Assure que la section prend de la place jusqu'à 600px
        section.setPadding(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)");

        H2 sectionTitle = new H2("Informations Personnelles");

        // Configuration des champs
        nomField.setWidthFull();
        nomField.setRequired(true);

        prenomField.setWidthFull();
        prenomField.setRequired(true);

        emailField.setWidthFull();
        emailField.setRequired(true);

        telephoneField.setWidthFull();

        // Liaison avec le Binder
        binder.forField(nomField)
                .asRequired("Le nom est obligatoire")
                .bind(User::getNom, User::setNom);

        binder.forField(prenomField)
                .asRequired("Le prénom est obligatoire")
                .bind(User::getPrenom, User::setPrenom);

        binder.forField(emailField)
                .asRequired("L'email est obligatoire")
                .bind(User::getEmail, User::setEmail);

        binder.forField(telephoneField)
                .bind(User::getTelephone, User::setTelephone);

        // Configuration du bouton
        saveProfileButton.setWidthFull();
        saveProfileButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveProfileButton.addClickListener(e -> saveProfile());

        // FormLayout pour l'organisation
        FormLayout formLayout = new FormLayout();
        formLayout.add(nomField, prenomField, emailField, telephoneField);
        formLayout.setResponsiveSteps(
                new FormLayout.ResponsiveStep("0", 1),
                new FormLayout.ResponsiveStep("500px", 2)
        );

        section.add(sectionTitle, formLayout, saveProfileButton);

        return section;
    }

    private void saveProfile() {
        try {
            // Valider le formulaire
            if (binder.validate().isOk()) {
                // Écrire les valeurs dans l'objet User
                binder.writeBean(currentUser);

                // Sauvegarder via le service
                userService.updateProfile(currentUserId, currentUser);

                showSuccess("Profil mis à jour avec succès");

                // Recharger les données
                loadUserData();
            } else {
                showError("Veuillez corriger les erreurs dans le formulaire");
            }
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private VerticalLayout createPasswordSection() {
        VerticalLayout section = new VerticalLayout();
        section.setMaxWidth("600px");
        section.setWidth("100%");
        section.setPadding(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        H2 sectionTitle = new H2("Changer le Mot de Passe");

        // Configuration des champs
        oldPasswordField.setWidthFull();
        oldPasswordField.setRequired(true);

        newPasswordField.setWidthFull();
        newPasswordField.setRequired(true);
        newPasswordField.setHelperText("Minimum 8 caractères");

        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequired(true);

        // Configuration du bouton
        changePasswordButton.setWidthFull();
        changePasswordButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        changePasswordButton.addClickListener(e -> changePassword());

        FormLayout formLayout = new FormLayout();
        formLayout.add(oldPasswordField, newPasswordField, confirmPasswordField);
        formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1));

        section.add(sectionTitle, formLayout, changePasswordButton);

        return section;
    }

    private void changePassword() {
        String oldPassword = oldPasswordField.getValue();
        String newPassword = newPasswordField.getValue();
        String confirmPassword = confirmPasswordField.getValue();

        // Validations côté client
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        // RÈGLE : Nouveau mot de passe minimum 8 caractères
        if (newPassword.length() < 8) {
            showError("Le nouveau mot de passe doit contenir au moins 8 caractères");
            return;
        }

        // RÈGLE : Les deux nouveaux mots de passe doivent correspondre
        if (!newPassword.equals(confirmPassword)) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        try {
            // Appel du service
            userService.changePassword(currentUserId, oldPassword, newPassword);

            showSuccess("Mot de passe changé avec succès");

            // Réinitialiser les champs
            oldPasswordField.clear();
            newPasswordField.clear();
            confirmPasswordField.clear();

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private VerticalLayout createStatsSection() {
        VerticalLayout section = new VerticalLayout();
        section.setMaxWidth("600px");
        section.setWidth("100%");
        section.setPadding(true);
        section.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 4px rgba(0,0,0,0.1)")
                .set("margin-top", "20px");

        H2 sectionTitle = new H2("Statistiques Personnelles");

        try {
            Map<String, Object> stats = userService.getStatistiquesUtilisateur(currentUserId);

            Paragraph reservations = new Paragraph(
                    "📝 Nombre de réservations : " + stats.get("nombreReservations")
            );

            Paragraph montant = new Paragraph(
                    "💰 Montant total dépensé : " + stats.get("montantTotalDepense") + " DH"
            );

            section.add(sectionTitle, reservations, montant);

        } catch (Exception e) {
            section.add(sectionTitle, new Paragraph("Erreur lors du chargement des statistiques"));
        }

        return section;
    }

    private VerticalLayout createDeactivateSection() {
        VerticalLayout section = new VerticalLayout();
        section.setMaxWidth("600px");
        section.setWidth("100%");
        section.setPadding(true);
        section.getStyle()
                .set("background", "#FFF3E0")
                .set("border-radius", "8px")
                .set("border", "1px solid #FF9800")
                .set("margin-top", "20px");

        H2 sectionTitle = new H2("Zone Dangereuse");
        sectionTitle.getStyle().set("color", "#E65100");

        Paragraph warning = new Paragraph(
                "⚠️ La désactivation de votre compte empêchera toute connexion future. " +
                        "Cette action est réversible uniquement par un administrateur."
        );

        Button deactivateButton = new Button("Désactiver mon compte");
        deactivateButton.addThemeVariants(ButtonVariant.LUMO_ERROR);
        deactivateButton.addClickListener(e -> confirmDeactivation());

        section.add(sectionTitle, warning, deactivateButton);

        return section;
    }

    private void confirmDeactivation() {
        ConfirmDialog dialog = new ConfirmDialog();
        dialog.setHeader("⚠️ Confirmer la Désactivation");
        dialog.setText(
                "Êtes-vous vraiment sûr de vouloir désactiver votre compte ?\n\n" +
                        "Vous ne pourrez plus vous connecter après cette action.\n" +
                        "Seul un administrateur pourra réactiver votre compte."
        );

        dialog.setCancelable(true);
        dialog.setCancelText("Annuler");
        dialog.setConfirmText("Oui, désactiver mon compte");
        dialog.setConfirmButtonTheme("error primary");

        dialog.addConfirmListener(e -> deactivateAccount());

        dialog.open();
    }

    private void deactivateAccount() {
        try {
            userService.desactiverCompte(currentUserId);
            showSuccess("Compte désactivé. Vous allez être déconnecté...");

            // Redirection vers la page d'accueil après 2 secondes
            getUI().ifPresent(ui ->
                    ui.getPage().executeJs("setTimeout(() => window.location.href = '/', 2000)")
            );

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 4000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}