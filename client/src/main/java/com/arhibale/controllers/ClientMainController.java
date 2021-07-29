package com.arhibale.controllers;

import com.arhibale.ClientApp;
import com.arhibale.netty.NettyNetwork;
import com.arhibale.netty.NetworkInstance;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.CommandType;
import model.auth.AuthDisconnect;
import model.auth.AuthResponse;
import model.file.FileDeleteRequest;
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
import java.util.Objects;
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
    private File copyFile;
    private final Path beginning = Paths.get("client/localFiles");
    private final Path root = beginning;
    private final String separator = "\\";

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setStatusBar("(>^_^)> hello! <(^_^<)");
        path = root;
        clientDir.setText(path.toString());

        network = new NettyNetwork(command -> {
            if (command.getType() == CommandType.AUTH_RESPONSE) {
                AuthResponse response = (AuthResponse) command;
                if (response.getAuthResponse().equals("/auth")) {
                    NetworkInstance.auth = response.getAuthResponse();
                    NetworkInstance.login = response.getLogin();
                    network.writeMessage(new ListRequest("", NetworkInstance.login));
                } else if (response.getAuthResponse().equals("/warn")){
                    NetworkInstance.auth = response.getAuthResponse();
                }
            }
            if (command.getType() == CommandType.LIST_RESPONSE) {
                ListResponse files = (ListResponse) command;
                refresh(files.getNames());
                serverDir.setText(files.getRoot());
                log.debug("from the server: {}", command.getType());
            } else if (command.getType() == CommandType.FILE_INFO) {
                FileInfo fileInfo = (FileInfo) command;
                Platform.runLater(() -> fileInfoServer.setText("size: " + fileInfo.getFileLength() + " bytes"));
            } else if (command.getType() == CommandType.FILE_MESSAGE) {
                FileMessage fileMessage = (FileMessage) command;
                copyFileAction(fileMessage.getFile(), new File(path + separator + fileMessage.getFileName()));
                refreshRequest();
                setStatusBar("the file is uploaded!(^-^)");
                log.debug("from the server: {}", command.getType());
            }
        });
        NetworkInstance.nettyNetworkInstance = network;
        openAuthWindow();

        closeWindowListener();
        listViewClickedClientListener();
        listViewClickedServerListener();
        sizeFocusItemListener();
        contextMenuClientListener();
        contextMenuServerListener();
    }

    @FXML
    private void fileExitAction() {
        Platform.exit();
        network.doStop();
    }

    @FXML
    private void refreshRequest() {
        network.writeMessage(new ListRequest("", NetworkInstance.login));
    }

    private void refreshClient() {
        Platform.runLater(() -> {
            listViewClient.getItems().clear();
            listViewClient.getItems().addAll(path.toFile().list());
            listViewClient.getItems().sort((o1, o2) -> new Long(new File(path + separator + o1).length() -
                    new File(path + separator + o2).length()).intValue());
        });
    }

    private void refresh(List<String> server) {
        Platform.runLater(() -> {
            refreshClient();
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
        if (!path.equals(root)) {
            Path p = path.getParent();
            goToPathClient(p);
        }
    }

    @FXML
    private void upDirServer() {
        network.writeMessage(new ListRequest("/up", NetworkInstance.login));
    }

    public void setStatusBar(String str) {
        statusBar.setText(str);
    }

    private void closeWindowListener() {
        Platform.runLater(() -> ClientApp.mainStage.setOnCloseRequest(event -> network.doStop()));
    }

    private void copyFileAction(File oldFile, File newFile) throws IOException {
        Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
    }

    private void goToPathClient(Path p) {
        path = p;
        clientDir.setText(path.toString());
        refreshClient();
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
                network.writeMessage(new ListRequest(listViewServer.getFocusModel().getFocusedItem(), NetworkInstance.login));
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

    private void contextMenuClientListener() {
        listViewClient.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem renameItem = new MenuItem();
            renameItem.textProperty().bind(Bindings.format("Rename"));
            renameItem.setOnAction(event -> {
                String item = cell.getItem();
                try {
                    openFileManipulationWindow(item, true);
                    refreshClient();
                } catch (IOException e) {
                    log.error("", e);
                }
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete"));
            deleteItem.setOnAction(event -> {
                try {
                    Files.deleteIfExists(Paths.get(path + separator + cell.getItem()));
                    refreshClient();
                    setStatusBar("delete " + cell.getItem() + "(P-P)");
                } catch (IOException e) {
                    log.error("", e);
                }
            });
            MenuItem copyItem = new MenuItem();
            copyItem.textProperty().bind(Bindings.format("Copy"));
            copyItem.setOnAction(event -> {
                copyFile = new File(path + separator + cell.getItem());
                setStatusBar("copy " + copyFile.getName() + "(_-_)");
            });

            contextMenu.getItems().addAll(copyItem, renameItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
    }

    private void contextMenuServerListener() {
        listViewServer.setCellFactory(lv -> {
            ListCell<String> cell = new ListCell<>();
            ContextMenu contextMenu = new ContextMenu();

            MenuItem renameItem = new MenuItem();
            renameItem.textProperty().bind(Bindings.format("Rename"));
            renameItem.setOnAction(event -> {
                try {
                    String item = cell.getItem();
                    openFileManipulationWindow(item, false);
                } catch (IOException e) {
                    log.error("", e);
                }
                refreshRequest();
            });
            MenuItem deleteItem = new MenuItem();
            deleteItem.textProperty().bind(Bindings.format("Delete"));
            deleteItem.setOnAction(event -> {
                network.writeMessage(new FileDeleteRequest(cell.getItem()));
                refreshRequest();
                setStatusBar("delete " + cell.getItem() + "(P-P)");
            });

            contextMenu.getItems().addAll(renameItem, deleteItem);
            cell.textProperty().bind(cell.itemProperty());
            cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
                if (isNowEmpty) {
                    cell.setContextMenu(null);
                } else {
                    cell.setContextMenu(contextMenu);
                }
            });
            return cell ;
        });
    }

    private void openFileManipulationWindow(String item, boolean is) throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/window/rename.fxml")));
        Stage renameStage = new Stage();
        renameStage.initModality(Modality.APPLICATION_MODAL);
        renameStage.setScene(new Scene(parent));
        renameStage.setResizable(false);
        if (is) {
            renameStage.setTitle(path + separator + item);
        } else {
            renameStage.setTitle("server: " + item);
        }
        log.debug("open rename " + item + " window");
        renameStage.showAndWait();
        setStatusBar("rename " + item + "(d-d)");
    }

    private void openAuthWindow() throws IOException {
        Parent parent = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/window/auth.fxml")));
        Stage auth = new Stage();
        auth.initModality(Modality.APPLICATION_MODAL);
        auth.setScene(new Scene(parent, 300, 140));
        auth.setResizable(false);
        auth.setTitle("Auth");
        log.debug("open auth window");
        auth.showAndWait();
    }

    @FXML
    private void addFile() throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Add file");
        List<File> list = fileChooser.showOpenMultipleDialog(ClientApp.mainStage);
        if (!list.isEmpty()) {
            for(File file : list) {
                if (file.exists()) {
                    copyFileAction(file.getAbsoluteFile(), new File(path + separator + file.getName()));
                }
            }
            refreshClient();
        }
    }

    @FXML
    private void createNewFolderClient() throws IOException {
        openFileManipulationWindow("-1", true);
        setStatusBar("create new folder!\\(^-^)/");
        refreshClient();
    }

    @FXML
    private void createNewFolderServer() throws IOException {
        openFileManipulationWindow("-2", false);
        setStatusBar("create new folder!\\(^-^)/");
        refreshRequest();
    }

    @FXML
    private void insertFileClient() throws IOException {
        if (copyFile != null) {
            copyFileAction(copyFile, new File(path + separator + copyFile.getName()));
            refreshClient();
            setStatusBar("insert " + copyFile.getName() + "(-_-)");
        }
    }

    @FXML
    private void insertFileServer() {
        if (copyFile != null) {
            network.writeMessage(new FileMessage(copyFile, copyFile.getName(), copyFile.length()));
            setStatusBar("insert " + copyFile.getName() + "(-_-)");
        }
    }

    @FXML
    private void disconnect() throws IOException {
        network.writeMessage(new AuthDisconnect(NetworkInstance.login));
        openAuthWindow();
    }
}