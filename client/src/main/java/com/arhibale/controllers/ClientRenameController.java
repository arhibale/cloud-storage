package com.arhibale.controllers;

import com.arhibale.netty.NettyNetwork;
import com.arhibale.netty.NetworkInstance;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import model.file.CreateNewFolderRequest;
import model.file.FileRenameRequest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
public class ClientRenameController {
    @FXML
    private TextField renameTextField;
    private final NettyNetwork network = NetworkInstance.nettyNetworkInstance;
    private String fileName;
    private String path;

    @FXML
    private void rename() {
        if (!renameTextField.getText().trim().isEmpty()) {
            Platform.runLater(() -> {
                Stage stage = (Stage) renameTextField.getScene().getWindow();
                if (stage.getTitle().startsWith("server")) {
                    if (network != null) {
                        if (stage.getTitle().substring(8).equals("-2")) {
                            network.writeMessage(new CreateNewFolderRequest(renameTextField.getText()));
                        } else {
                            network.writeMessage(new FileRenameRequest(renameTextField.getText().trim(), stage.getTitle().substring(8)));
                        }
                    }
                } else {
                    init(stage.getTitle());
                    if (fileName.equals("-1")) {
                        try {
                            Files.createDirectory(Paths.get(path + "\\" + renameTextField.getText()));
                        } catch (IOException e) {
                            log.error("", e);
                        }
                    } else {
                        File file = new File(path + "\\" + fileName);
                        log.debug("{} - {} - {} - {}", fileName, path, file.exists(), file.getPath());
                        if (file.exists()) {
                            if (file.renameTo(new File(path + "\\" + renameTextField.getText().trim()))) {
                                cancel();
                            }
                        }
                    }
                }
                cancel();
            });
        }
    }

    @FXML
    private void cancel() {
        Platform.runLater(() -> {
            Stage stage = (Stage) renameTextField.getScene().getWindow();
            stage.close();
        });
    }

    private void init(String str) {
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) == '\\') {
                fileName = str.substring(i + 1);
                path = str.substring(0, i);
                return;
            }
        }
    }
}
