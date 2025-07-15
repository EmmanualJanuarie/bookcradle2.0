package com.bookcradle.auth;

import com.bookcradle.utils.PasswordUtils;
import com.bookcradle.user.UserController;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color;
import javafx.stage.Stage; // Added import for Stage
import java.io.*;
import java.security.SecureRandom;

public class AuthController {

    @FXML
    private Label titleLabel;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField visiblePasswordField;
    @FXML
    private Button authButton;
    @FXML
    private Label messageLabel;
    @FXML
    private Hyperlink messageLink;
    @FXML
    private Hyperlink adminPromptLink;
    @FXML
    private Label adminPromptLabel;
    @FXML
    private TextField nameField;
    @FXML
    private TextField surnameField;
    @FXML
    private TextField emailField;
    @FXML
    private Button generatePasswordButton;
    @FXML
    private HBox passwordStrengthBar;
    @FXML
    private Button togglePasswordButton;

    private boolean isPasswordVisible = false;

    private enum Mode {
        ADMIN_SIGNIN, USER_LOGIN, USER_SIGNUP
    }

    private Mode currentMode = Mode.ADMIN_SIGNIN;

    @FXML
    private void initialize() {
        updateUIForMode();
        setupPasswordStrengthListener();
        Rectangle strengthIndicator = new Rectangle(100, 10);
        strengthIndicator.setFill(Color.GRAY);
        passwordStrengthBar.getChildren().add(strengthIndicator);
    }

    @SuppressWarnings("unused") // Suppress warning for unused 'obs' parameter
    private void setupPasswordStrengthListener() {
        passwordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                int strength = calculatePasswordStrength(newValue);
                Rectangle strengthIndicator = (Rectangle) passwordStrengthBar.getChildren().get(0);
                
                if (strength <= 33) {
                    strengthIndicator.setFill(Color.RED);
                } else if (strength <= 66) {
                    strengthIndicator.setFill(Color.YELLOW);
                } else {
                    strengthIndicator.setFill(Color.GREEN);
                }
            }
        });
        visiblePasswordField.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals(oldValue)) {
                passwordField.setText(newValue);
                int strength = calculatePasswordStrength(newValue);
                Rectangle strengthIndicator = (Rectangle) passwordStrengthBar.getChildren().get(0);
                
                if (strength <= 33) {
                    strengthIndicator.setFill(Color.RED);
                } else if (strength <= 66) {
                    strengthIndicator.setFill(Color.YELLOW);
                } else {
                    strengthIndicator.setFill(Color.GREEN);
                }
            }
        });
    }

    private String generateStrongPassword(int length) {
        String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String special = "!@#$%^&*()-_=+";
        String allChars = upper + lower + numbers + special;
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        password.append(upper.charAt(random.nextInt(upper.length())));
        password.append(lower.charAt(random.nextInt(lower.length())));
        password.append(numbers.charAt(random.nextInt(numbers.length())));
        password.append(special.charAt(random.nextInt(special.length())));

        for (int i = 4; i < length; i++) {
            password.append(allChars.charAt(random.nextInt(allChars.length())));
        }

        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            char temp = passwordArray[i];
            passwordArray[i] = passwordArray[j];
            passwordArray[j] = temp;
        }

        return new String(passwordArray);
    }

    private int calculatePasswordStrength(String password) {
        int score = 0;
        if (password == null || password.isEmpty()) return 0;

        if (password.length() >= 8) score += 25;
        if (password.length() >= 12) score += 25;

        if (password.matches(".*[A-Z].*")) score += 15;
        if (password.matches(".*[a-z].*")) score += 15;
        if (password.matches(".*[0-9].*")) score += 15;
        if (password.matches(".*[!@#$%^&*()-_=+].*")) score += 15;

        if (password.matches(".*(.+)\\1.*")) score -= 10;
        if (password.length() > 16) score = Math.min(100, score + 10);

        return Math.min(100, Math.max(0, score));
    }

    @FXML
    private void onGeneratePasswordClicked() {
        String newPassword = generateStrongPassword(12);
        passwordField.setText(newPassword);
        visiblePasswordField.setText(newPassword);
    }

    @FXML
    private void onTogglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible;
        if (isPasswordVisible) {
            visiblePasswordField.setText(passwordField.getText());
            passwordField.setVisible(false);
            passwordField.setManaged(false);
            visiblePasswordField.setVisible(true);
            visiblePasswordField.setManaged(true);
            togglePasswordButton.setText("👁️‍🗨️");
        } else {
            passwordField.setText(visiblePasswordField.getText());
            visiblePasswordField.setVisible(false);
            visiblePasswordField.setManaged(false);
            passwordField.setVisible(true);
            passwordField.setManaged(true);
            togglePasswordButton.setText("👁️");
        }
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
        generatePasswordButton.setVisible(showExtraFields);
        generatePasswordButton.setManaged(showExtraFields);
        passwordStrengthBar.setVisible(showExtraFields);
        passwordStrengthBar.setManaged(showExtraFields);

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
        visiblePasswordField.clear();
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
                    loadUserDashboard(user);
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