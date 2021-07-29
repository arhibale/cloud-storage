package com.arhibale.controllers;

import com.arhibale.netty.NettyNetwork;
import com.arhibale.netty.NetworkInstance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import model.auth.AuthRequest;
import model.list.ListRequest;

public class ClientAuthController {

    @FXML
    private TextField loginTF;
    @FXML
    private PasswordField passwordTF;
    private Thread thread;
    private final NettyNetwork network = NetworkInstance.nettyNetworkInstance;

    @FXML
    private void initialize() {
        closeWindowListener();
        authentication();
    }

    private void authentication() {
        thread = new Thread(() -> {
            label:
            while (true) {
                switch (NetworkInstance.auth) {
                    case "/auth":
                        network.writeMessage(new ListRequest("", NetworkInstance.login));
                        NetworkInstance.auth = "exit";
                        close();
                        break label;
                    case "/warn":
                        warn();
                        NetworkInstance.auth = "";
                        break;
                    case "/exit":
                        break label;
                }
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    @FXML
    private void auth() {
        network.writeMessage(new AuthRequest(loginTF.getText(), passwordTF.getText()));
    }

    private void close() {
        Platform.runLater(() -> {
            Stage stage = (Stage) loginTF.getScene().getWindow();
            stage.close();
        });
    }

    private void warn() {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("Login error!");
            alert.setHeaderText("Login error:");
            alert.setContentText("Incorrect login or password.");
            alert.show();
        });
    }

    private void closeWindowListener() {
        Platform.runLater(() -> {
            Stage stage = (Stage) loginTF.getScene().getWindow();
            stage.setOnCloseRequest(event -> {
                network.doStop();
                thread.interrupt();
                Platform.exit();
            });
        });
    }
}
