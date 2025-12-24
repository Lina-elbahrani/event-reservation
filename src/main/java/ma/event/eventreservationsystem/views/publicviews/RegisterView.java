package ma.event.eventreservationsystem.views.publicviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.entity.enums.UserRole;
import ma.event.eventreservationsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

@Route("register")
@PageTitle("Inscription | Event Reservation System")
public class RegisterView extends VerticalLayout {

    private final UserService userService;

    private final TextField nomField = new TextField("Nom");
    private final TextField prenomField = new TextField("Prénom");
    private final EmailField emailField = new EmailField("Email");
    private final TextField telephoneField = new TextField("Téléphone");
    private final PasswordField passwordField = new PasswordField("Mot de passe");
    private final PasswordField confirmPasswordField = new PasswordField("Confirmer le mot de passe");
    private final ComboBox<UserRole> roleComboBox = new ComboBox<>("Type de compte");
    private final Button registerButton = new Button("S'inscrire");
    private final RouterLink loginLink = new RouterLink("Déjà un compte ? Se connecter", LoginView.class);

    public RegisterView(@Autowired UserService userService) {
        this.userService = userService;

        // Configuration de la page
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();

        // Titre
        H1 title = new H1("Inscription");
        title.getStyle().set("color", "#1976D2");

        // Configuration des champs
        nomField.setWidthFull();
        nomField.setRequired(true);
        nomField.setPlaceholder("Votre nom");

        prenomField.setWidthFull();
        prenomField.setRequired(true);
        prenomField.setPlaceholder("Votre prénom");

        emailField.setWidthFull();
        emailField.setRequired(true);
        emailField.setPlaceholder("exemple@email.com");

        telephoneField.setWidthFull();
        telephoneField.setPlaceholder("0600000000");

        passwordField.setWidthFull();
        passwordField.setRequired(true);
        passwordField.setPlaceholder("Minimum 8 caractères");
        passwordField.setHelperText("Le mot de passe doit contenir au moins 8 caractères");

        confirmPasswordField.setWidthFull();
        confirmPasswordField.setRequired(true);

        // ComboBox des rôles
        roleComboBox.setWidthFull();
        roleComboBox.setItems(UserRole.CLIENT, UserRole.ORGANIZER);
        roleComboBox.setValue(UserRole.CLIENT);
        roleComboBox.setItemLabelGenerator(UserRole::getLabel);
        roleComboBox.setRequired(true);

        // Bouton d'inscription
        registerButton.setWidthFull();
        registerButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        registerButton.addClickListener(e -> register());

        // Layout du formulaire
        VerticalLayout formLayout = new VerticalLayout(
                title,
                nomField,
                prenomField,
                emailField,
                telephoneField,
                roleComboBox,
                passwordField,
                confirmPasswordField,
                registerButton,
                loginLink
        );
        formLayout.setMaxWidth("500px");
        formLayout.setWidth("100%");
        formLayout.setPadding(true);
        formLayout.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        add(formLayout);
    }

    private void register() {
        // Validation des champs
        if (nomField.isEmpty() || prenomField.isEmpty() || emailField.isEmpty() ||
                passwordField.isEmpty() || confirmPasswordField.isEmpty()) {
            showError("Veuillez remplir tous les champs obligatoires");
            return;
        }

        // RÈGLE : Vérifier que les mots de passe correspondent
        if (!passwordField.getValue().equals(confirmPasswordField.getValue())) {
            showError("Les mots de passe ne correspondent pas");
            return;
        }

        // RÈGLE : Mot de passe minimum 8 caractères (validation côté client)
        if (passwordField.getValue().length() < 8) {
            showError("Le mot de passe doit contenir au moins 8 caractères");
            return;
        }

        try {
            // Création de l'utilisateur
            User user = User.builder()
                    .nom(nomField.getValue())
                    .prenom(prenomField.getValue())
                    .email(emailField.getValue())
                    .telephone(telephoneField.getValue())
                    .password(passwordField.getValue())
                    .role(roleComboBox.getValue())
                    .build();

            // Inscription via le service (qui vérifie l'unicité de l'email et hashe le mot de passe)
            userService.inscription(user);

            // Succès
            showSuccess("Inscription réussie ! Vous pouvez maintenant vous connecter");

            // Redirection vers la page de login après 2 secondes
            getUI().ifPresent(ui ->
                    ui.getPage().executeJs("setTimeout(() => window.location.href = 'login', 2000)")
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
