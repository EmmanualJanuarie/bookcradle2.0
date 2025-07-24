package com.bookcradle.auth;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.bookcradle.utils.PasswordUtils;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ResetPasswordController {

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField newPasswordField;

    @FXML
    private Label statusLabel;

    @FXML
    private void onResetPasswordClicked() {
        String email = emailField.getText();
        String newPassword = newPasswordField.getText();

        if (email.isEmpty() || newPassword.isEmpty()) {
            statusLabel.setText("Please fill in all fields.");
            return;
        }

        boolean resetSuccess = resetPassword(email, newPassword);

        if (resetSuccess) {
            statusLabel.setText("Password reset successful!");
        } else {
            statusLabel.setText("User not found or error occurred.");
        }
    }

    private boolean resetPassword(String email, String newPassword) {
    File userFile = new File("SignUp_data.txt"); // Adjust path if needed
    List<String> updatedLines = new ArrayList<>();
    boolean found = false;

    try (BufferedReader reader = new BufferedReader(new FileReader(userFile))) {
        String line;
        while ((line = reader.readLine()) != null) {
            String[] parts = line.split(",");
            if (parts.length >= 4) {
                String emailPart = parts[2].trim(); // e.g., "email=indiphe@example.com"
                String[] emailKeyValue = emailPart.split("=");
                if (emailKeyValue.length == 2 && emailKeyValue[1].equalsIgnoreCase(email.trim())) {
                    // Update password (parts[3])
                String hashed = PasswordUtils.hashPassword(newPassword);
parts[3] = "password=" + hashed;


                    updatedLines.add(String.join(",", parts));
                    found = true;
                } else {
                    updatedLines.add(line);
                }
            } else {
                updatedLines.add(line);
            }
        }
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }

    if (!found) {
        return false; // user email not found
    }

    // Write the updated lines back to the file
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(userFile))) {
        for (String updatedLine : updatedLines) {
            writer.write(updatedLine);
            writer.newLine();
        }
    } catch (IOException e) {
        e.printStackTrace();
        return false;
    }

    return true;
}

@FXML
private Button backToSignInButton;
@FXML
private void onBackToSignInClicked(ActionEvent event) {
    try {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/AuthUI.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/styles/AuthStyle.css").toExternalForm()); // ✅ Add CSS

        Stage stage = (Stage) backToSignInButton.getScene().getWindow();
        stage.setScene(scene);
        stage.setTitle("Sign In");
        stage.show();
    } catch (IOException e) {
        e.printStackTrace();
    }
}


}


