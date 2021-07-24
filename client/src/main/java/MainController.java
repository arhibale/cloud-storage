import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MultipleSelectionModel;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.file.FileMessage;
import model.file.FileRequest;
import model.list.ListRequest;
import model.list.ListResponse;
import com.arhibale.netty.NettyNetwork;

@Slf4j
public class MainController implements Initializable {
    @FXML
    private TextField clientDir;
    @FXML
    private Label fileInfoServer;
    @FXML
    private Label fileInfoClient;
    @FXML
    private ListView<String> listViewClient;
    @FXML
    private ListView<String> listViewServer;
    @FXML
    private TextField statusBar;

    private NettyNetwork network;
    private File root;
    private Path thisPath;
    private Path pathFile;
    private final String solidus = "/";

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setStatusBar("(>^_^)> hello! <(^_^<)");
        clientDir.setText(thisPath.toString());
        root = new File("./client/clientFiles");
        thisPath = root.toPath();
        refresh();
        network = new NettyNetwork(command -> {
            if (command.getType().equals(solidus)) {
//                FileInfoMessage message = (FileInfoMessage) command;
//                Platform.runLater(() ->
//                        fileInfoServer.setText("name: " + message.getFileName() + " size: " + message.getLength() + " byte"));
//                log.debug("from the server: {}", command.getType());
            } else if (command.getType() == CommandType.LIST_RESPONSE) {
                ListResponse files = (ListResponse) command;
                Platform.runLater(() -> {
                    listViewServer.getItems().clear();
                    listViewServer.getItems().addAll(files.getNames());
                });
                log.debug("from the server: {}", command.getType());
            } else if (command.getType() == CommandType.FILE_MESSAGE) {
                FileMessage fileMessage = (FileMessage) command;
                copyFile(fileMessage.getFile(), new File(root + solidus + fileMessage.getFileName()));
                refreshAll();
                setStatusBar("the file is uploaded!(^-^)");
                log.debug("from the server: {}", command.getType());
            }
        });
        listViewListener();
        listViewClientClickEvent();
        //TODO меню бар сделать
    }

    @FXML
    private void toTheServer() {
        String fileName = listViewClient.getFocusModel().getFocusedItem();
        File file = new File(root + solidus + fileName);
        network.writeMessage(new FileMessage(file, file.getName(), file.length()));
        setStatusBar("the file has been sent!(>_<)");
    }

    @FXML
    private void fromTheServer() {
        network.writeMessage(new FileRequest(listViewServer.getFocusModel().getFocusedItem(), false));
    }

    @SneakyThrows
    @FXML
    private void refreshAll() {
        network.writeMessage(new ListRequest());
        refresh();
    }

    private void refresh() {
        Platform.runLater(() -> {
            listViewClient.getItems().clear();
            listViewClient.getItems().addAll(root.list());
        });
    }

    private void copyFile(File oldFile, File newFile) throws IOException {
        if (oldFile.exists()) {
            Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void listViewListener() {
//        Platform.runLater(() -> {
//            MultipleSelectionModel<String> selectionModelClient = listViewClient.getSelectionModel();
//            MultipleSelectionModel<String> selectionModelServer = listViewServer.getSelectionModel();
//            selectionModelClient.selectedItemProperty()
//                    .addListener((observable, oldValue, newValue) -> fileInfoClient.setText("name: " + newValue +
//                            " size: " + new File(root + solidus + newValue).length() + " byte"));
//            selectionModelServer.selectedItemProperty()
//                    .addListener((observable, oldValue, newValue) -> network.writeMessage(new FileInfoRequest(newValue)));
//        });
    }

    private void listViewClientClickEvent() {
        Platform.runLater(() -> listViewClient.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && !(listViewClient.getFocusModel().getFocusedItem().isEmpty())) {
                //TODO перемещение по папкам
                System.out.println("даблклик");
            }
        }));
    }

    private void setStatusBar(String str) {
        statusBar.setText(str);
    }

    @FXML
    private void deleteFileClient(ActionEvent actionEvent) throws IOException {
        File file = new File(root + solidus + listViewClient.getFocusModel().getFocusedItem());
        if (file.exists()) {
            Files.deleteIfExists(file.toPath());
            statusBar.setText("file deleted!•_•)");
            refresh();
            log.debug("file {} deleted", file.getName());
        } else {
            statusBar.setText("ヽ(`д´*)ノ");
        }
    }

    @FXML
    private void copyFileClient(ActionEvent actionEvent) throws IOException {
        //TODO посмотреть про фм и доделать операции иодтфикации
        selectAFile();
    }

    private void selectAFile() {
        File file = new File(root + solidus + listViewClient.getFocusModel().getFocusedItem());
        if (!file.isDirectory()) {
            pathFile = file.toPath();
        }
    }

    @FXML
    private void fileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    @FXML
    private void cutFileClient(ActionEvent actionEvent) {
        selectAFile();
    }

    @FXML
    private void insertFileClient(ActionEvent actionEvent) throws IOException {
        Files.move(pathFile, thisPath, StandardCopyOption.REPLACE_EXISTING);
    }
    private void goToPathClient(Path path) {
        thisPath = path;
        clientDir.setText(path.toString());
    }
}
