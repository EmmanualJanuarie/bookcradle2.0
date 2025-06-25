package Auth;

import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AuthController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private Button authButton;

    @FXML
    private Label messageLabel;
    @FXML
    private Hyperlink messageLink;

    @FXML
    private Label adminPromptLabel;
    @FXML
    private Hyperlink adminPromptLink;

    @FXML
    private TextField nameField;

    @FXML
    private TextField surnameField;

    @FXML
    private TextField emailField;

    private enum Mode {
        ADMIN_SIGNIN, USER_LOGIN, USER_SIGNUP
    }

    private Mode currentMode = Mode.ADMIN_SIGNIN;

    @FXML
    private void initialize() {
        updateUIForMode();
    }

    @FXML
    private void updateUIForMode() {
        boolean showExtraFields = (currentMode == Mode.USER_SIGNUP);

        nameField.setVisible(showExtraFields);
        nameField.setManaged(showExtraFields);

        surnameField.setVisible(showExtraFields);
        surnameField.setManaged(showExtraFields);

        emailField.setVisible(showExtraFields);
        emailField.setManaged(showExtraFields);

        switch (currentMode) {
            case ADMIN_SIGNIN:
                titleLabel.setText("Sign In | Admin");
                authButton.setText("Sign In");
                messageLabel.setText("Already a registered user?");
                messageLink.setText("Sign In as User");
                adminPromptLabel.setVisible(false);
                adminPromptLink.setVisible(false);
                break;

            case USER_LOGIN:
                titleLabel.setText("Sign In | User");
                authButton.setText("Sign In");
                messageLabel.setText("Not a registered user?");
                messageLink.setText("Sign Up");
                adminPromptLabel.setVisible(true);
                adminPromptLink.setVisible(true);
                break;

            case USER_SIGNUP:
                titleLabel.setText("Sign Up | User");
                authButton.setText("Sign Up");
                messageLabel.setText("Already a registered user?");
                messageLink.setText("Sign In");
                adminPromptLabel.setVisible(true);
                adminPromptLink.setVisible(true);
                usernameField.setVisible(false);
                break;
        }

        usernameField.clear();
        passwordField.clear();
        nameField.clear();
        surnameField.clear();
        emailField.clear();
    }

    @FXML
    private void onMessageLinkClicked() {
        if (currentMode == Mode.ADMIN_SIGNIN) {
            currentMode = Mode.USER_LOGIN;
        } else if (currentMode == Mode.USER_LOGIN) {
            currentMode = Mode.USER_SIGNUP;
        } else {
            currentMode = Mode.USER_LOGIN;
        }
        updateUIForMode();
    }

    @FXML
    private void onAdminPromptLinkClicked() {
        currentMode = Mode.ADMIN_SIGNIN;
        updateUIForMode();
    }

    @FXML
    private void handleAuth() {
        String user = usernameField.getText();
        String pass = passwordField.getText();

        switch (currentMode) {
            case ADMIN_SIGNIN:
                System.out.println("Admin Sign In: " + user + " / " + pass);
                // TODO: Add admin sign in logic
                break;

            case USER_LOGIN:
                System.out.println("User Sign In: " + user + " / " + pass);
                // TODO: Add user sign in logic
                break;

            case USER_SIGNUP:
                System.out.println("User Sign Up: " + user + " / " + pass);
                // TODO: Add user sign up logic
                break;
        }
    }
}
