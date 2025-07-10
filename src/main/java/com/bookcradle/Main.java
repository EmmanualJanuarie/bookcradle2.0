package com.bookcradle;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/auth/AuthUI.fxml"));
        Scene scene = new Scene(loader.load(), 500, 600);
        scene.getStylesheets().add(getClass().getResource("/styles/AuthStyle.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("BookCradle");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
