package Auth;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.Parent;

public class Controller {

    @FXML
    private StackPane formContainer;
    @FXML
    private Button adminToggle;
    @FXML
    private Button userToggle;

    @FXML
    public void initialize() {
        showAdminForm();
    }

    public void showAdminForm() {
        loadForm("/Auth/resource/AdminForm.fxml");
        setActiveButton(adminToggle, userToggle);
    }

    public void showUserForm() {
        loadForm("/Auth/resource/UserForm.fxml");
        setActiveButton(userToggle, adminToggle);
    }

    private void loadForm(String fxml) {
        try {
            Parent pane = FXMLLoader.load(getClass().getResource(fxml));
            formContainer.getChildren().setAll(pane);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button active, Button inactive) {
        active.getStyleClass().add("toggle-active");
        inactive.getStyleClass().remove("toggle-active");
    }
}
