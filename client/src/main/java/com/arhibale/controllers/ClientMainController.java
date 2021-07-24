package com.arhibale.controllers;

import com.arhibale.ClientApp;
import com.arhibale.netty.NettyNetwork;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import lombok.extern.slf4j.Slf4j;
import model.CommandType;
import model.file.FileInfo;
import model.file.FileMessage;
import model.file.FileRequest;
import model.list.ListRequest;
import model.list.ListResponse;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.ResourceBundle;

@Slf4j
public class ClientMainController implements Initializable {

    @FXML
    private ListView<String> listViewClient;
    @FXML
    private ListView<String> listViewServer;
    @FXML
    private TextField statusBar;
    @FXML
    private Label fileInfoClient;
    @FXML
    private Label fileInfoServer;
    @FXML
    private TextField clientDir;
    @FXML
    private TextField serverDir;

    private NettyNetwork network;
    private Path path;
    private final Path root = Paths.get("client/clientFiles");
    private final String separator = "/";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        closeWindowListener();
        setStatusBar("(>^_^)> hello! <(^_^<)");
        path = root;
        clientDir.setText(path.toString());

        network = new NettyNetwork(command -> {
            if (command.getType() == CommandType.LIST_RESPONSE) {
                ListResponse files = (ListResponse) command;
                refresh(path, files.getNames());
                serverDir.setText(files.getRoot());
                log.debug("from the server: {}", command.getType());
            } else if (command.getType() == CommandType.FILE_INFO) {
                FileInfo fileInfo = (FileInfo) command;
                Platform.runLater(() -> {
                    fileInfoServer.setText("size: " + fileInfo.getFileLength() + " bytes");
                });
            } else if (command.getType() == CommandType.FILE_MESSAGE) {
                FileMessage fileMessage = (FileMessage) command;
                copyFile(fileMessage.getFile(), new File(path + separator + fileMessage.getFileName()));
                refreshAll();
                setStatusBar("the file is uploaded!(^-^)");
                log.debug("from the server: {}", command.getType());
            }
        });

        listViewClickedClientListener();
        listViewClickedServerListener();
        sizeFocusItemListener();
    }

    @FXML
    private void fileExitAction() {
        Platform.exit();
        network.doStop();
    }

    @FXML
    private void refreshAll() {
        network.writeMessage(new ListRequest());
    }

    private void refresh(Path client, List<String> server) {
        Platform.runLater(() -> {
            listViewClient.getItems().clear();
            listViewClient.getItems().addAll(client.toFile().list());
            listViewClient.getItems().sort((o1, o2) -> new Long(new File(path + separator + o1).length() -
                    new File(path + separator + o2).length()).intValue());

            listViewServer.getItems().clear();
            listViewServer.getItems().addAll(server);
        });
    }

    @FXML
    private void fromTheServer() {
        String fileName = listViewServer.getFocusModel().getFocusedItem();
        if (!fileName.isEmpty()) {
            network.writeMessage(new FileRequest(fileName, false));
        }
    }

    @FXML
    private void toTheServer() {
        File file = new File(path + separator + listViewClient.getFocusModel().getFocusedItem());
        if (!Files.isDirectory(file.toPath()) && file.exists()) {
            FileMessage fileMessage = new FileMessage(file, file.getName(), file.length());
            network.writeMessage(fileMessage);
            setStatusBar("the file has been sent!(>_<)");
        }
    }

    @FXML
    private void upDirClient() {
        Path p = path.getParent();
        if (p.equals(root)) {
            goToPathClient(p);
        }
    }

    @FXML
    private void upDirServer() {
        network.writeMessage(new ListRequest("/up"));
    }

    public void setStatusBar(String str) {
        statusBar.setText(str);
    }

    private void closeWindowListener() {
        Platform.runLater(() -> {
            ClientApp.mainStage.setOnCloseRequest(event -> network.doStop());
        });
    }

    private void copyFile(File oldFile, File newFile) throws IOException {
        Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void goToPathClient(Path p) {
        path = p;
        clientDir.setText(path.toString());
        refreshAll();
    }

    private void listViewClickedClientListener() {
        listViewClient.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                File file = new File(path + separator + listViewClient.getFocusModel().getFocusedItem());
                if (file.isDirectory()) {
                    goToPathClient(file.toPath());
                }
            }
        });
    }

    private void listViewClickedServerListener() {
        listViewServer.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                network.writeMessage(new ListRequest(listViewServer.getFocusModel().getFocusedItem()));
            }
        });
    }

    private void sizeFocusItemListener() {
        Platform.runLater(() -> {
            MultipleSelectionModel<String> selectionModelClient = listViewClient.getSelectionModel();
            MultipleSelectionModel<String> selectionModelServer = listViewServer.getSelectionModel();

            selectionModelClient.selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> fileInfoClient.setText(
                            "size: " + new File(path + separator + newValue).length() + " byte"));

            selectionModelServer.selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> network.writeMessage(new FileRequest(newValue, true)));
        });
    }
}