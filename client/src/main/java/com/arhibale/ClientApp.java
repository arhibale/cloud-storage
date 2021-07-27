package com.arhibale;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.Objects;

public class ClientApp extends Application {
    public static Stage mainStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/window/main-w.fxml")));
        primaryStage.setScene(new Scene(parent));
        primaryStage.setResizable(false);
        primaryStage.setTitle("Client Cloud Storage");
        primaryStage.show();
        mainStage = primaryStage;
    }
}
