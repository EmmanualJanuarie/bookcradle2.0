package com.bookcradle.auth;

import com.bookcradle.utils.PasswordUtils;
import com.bookcradle.user.UserController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.*;

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
                usernameField.setVisible(true);
                usernameField.setManaged(true);
                break;

            case USER_LOGIN:
                titleLabel.setText("Sign In | User");
                authButton.setText("Sign In");
                messageLabel.setText("Not a registered user?");
                messageLink.setText("Sign Up");
                usernameField.setText("Email");
                adminPromptLabel.setVisible(true);
                adminPromptLink.setVisible(true);
                usernameField.setVisible(true);
                usernameField.setManaged(true);
                break;

            case USER_SIGNUP:
                titleLabel.setText("Sign Up | User");
                authButton.setText("Sign Up");
                messageLabel.setText("Already a registered user?");
                messageLink.setText("Sign In");
                adminPromptLabel.setVisible(true);
                adminPromptLink.setVisible(true);
                usernameField.setVisible(false);
                usernameField.setManaged(false);
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
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();
        String hashedPass = PasswordUtils.hashPassword(pass);

        switch (currentMode) {
            case ADMIN_SIGNIN:
                if (user.equals("admin") && pass.equals("admin123")) {
                    loadAdminDashboard();
                } else {
                    messageLabel.setText("Invalid admin credentials");
                }
                break;

            case USER_LOGIN:
                if (isUserValid(user, hashedPass)) {
                    loadUserDashboard(user); // pass logged-in email here
                }
                break;

            case USER_SIGNUP:
                String name = nameField.getText().trim();
                String surname = surnameField.getText().trim();
                String email = emailField.getText().trim();

                if (isEmailTaken(email)) {
                    messageLabel.setText("Email already registered. Try logging in.");
                } else {
                    saveUserSignUp(name, surname, email, hashedPass);
                    messageLabel.setText("Signed up successfully! Please sign in.");
                    currentMode = Mode.USER_LOGIN;
                    updateUIForMode();
                }
                break;
        }
    }

    private void saveUserSignUp(String name, String surname, String email, String hashedPassword) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("SignUp_data.txt", true))) {
            writer.write("name=" + name + ",surname=" + surname + ",email=" + email + ",password=" + hashedPassword);
            writer.newLine();
        } catch (IOException e) {
            messageLabel.setText("Error saving user.");
            e.printStackTrace();
        }
    }

    private boolean isUserValid(String email, String hashedPassword) {
        try (BufferedReader reader = new BufferedReader(new FileReader("SignUp_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("email=" + email)) {
                    if (line.contains("password=" + hashedPassword)) {
                        return true;
                    } else {
                        messageLabel.setText("Incorrect password.");
                        return false;
                    }
                }
            }
            messageLabel.setText("Email not found.");
        } catch (IOException e) {
            messageLabel.setText("Error reading user data.");
            e.printStackTrace();
        }
        return false;
    }

    private boolean isEmailTaken(String email) {
        try (BufferedReader reader = new BufferedReader(new FileReader("SignUp_data.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.contains("email=" + email)) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void loadUserDashboard(String userEmail) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/UserDashboard.fxml"));
            Parent dashboardRoot = loader.load();

            // Pass email to UserController
            UserController controller = loader.getController();
            controller.setLoggedInEmail(userEmail);

            Stage stage = (Stage) authButton.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setTitle("User Dashboard");
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading dashboard.");
            e.printStackTrace();
        }
    }

    private void loadAdminDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/admin/AdminDashboard.fxml"));
            Parent dashboardRoot = loader.load();
            Stage stage = (Stage) authButton.getScene().getWindow();
            stage.setScene(new Scene(dashboardRoot));
            stage.setTitle("Admin Dashboard");
            stage.show();
        } catch (Exception e) {
            messageLabel.setText("Error loading dashboard.");
            e.printStackTrace();
        }
    }
}
