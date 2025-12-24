package ma.event.eventreservationsystem.views.publicviews;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.EmailField;
import com.vaadin.flow.component.textfield.PasswordField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import ma.event.eventreservationsystem.entity.User;
import ma.event.eventreservationsystem.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

@Route("login")
@PageTitle("Connexion | Event Reservation System")
public class LoginView extends VerticalLayout {

    private final UserService userService;

    private final EmailField emailField = new EmailField("Email");
    private final PasswordField passwordField = new PasswordField("Mot de passe");
    private final Button loginButton = new Button("Se connecter");
    private final RouterLink registerLink = new RouterLink("Créer un compte", RegisterView.class);

    public LoginView(@Autowired UserService userService) {
        this.userService = userService;

        // Configuration de la page
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        setSizeFull();
        addClassName("login-view");

        // Titre
        H1 title = new H1("Connexion");
        title.getStyle().set("color", "#1976D2");

        // Configuration des champs
        emailField.setWidthFull();
        emailField.setPlaceholder("exemple@email.com");
        emailField.setRequired(true);
        emailField.setErrorMessage("Email invalide");

        passwordField.setWidthFull();
        passwordField.setPlaceholder("Votre mot de passe");
        passwordField.setRequired(true);

        // Configuration du bouton
        loginButton.setWidthFull();
        loginButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        loginButton.addClickListener(e -> login());

        // Lien d'inscription
        Span registerText = new Span("Pas encore de compte ? ");
        registerText.getStyle().set("margin-top", "20px");

        // Layout du formulaire
        VerticalLayout formLayout = new VerticalLayout(
                title,
                emailField,
                passwordField,
                loginButton,
                registerText,
                registerLink
        );
        formLayout.setMaxWidth("400px");
        formLayout.setWidth("100%");
        formLayout.setPadding(true);
        formLayout.getStyle()
                .set("background", "white")
                .set("border-radius", "8px")
                .set("box-shadow", "0 2px 10px rgba(0,0,0,0.1)");

        add(formLayout);
    }

    private void login() {
        String email = emailField.getValue();
        String password = passwordField.getValue();

        // Validation côté client
        if (email.isEmpty() || password.isEmpty()) {
            showError("Veuillez remplir tous les champs");
            return;
        }

        try {
            // Authentification via le service
            User user = userService.authenticate(email, password);

            // Succès
            showSuccess("Connexion réussie ! Bienvenue " + user.getPrenom());

            // Redirection selon le rôle
            switch (user.getRole()) {
                case ADMIN:
                    getUI().ifPresent(ui -> ui.navigate("admin/dashboard"));
                    break;
                case ORGANIZER:
                    getUI().ifPresent(ui -> ui.navigate("organizer/dashboard"));
                    break;
                case CLIENT:
                default:
                    getUI().ifPresent(ui -> ui.navigate("dashboard"));
                    break;
            }

        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    private void showError(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void showSuccess(String message) {
        Notification notification = Notification.show(message, 3000, Notification.Position.TOP_CENTER);
        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
    }
}
