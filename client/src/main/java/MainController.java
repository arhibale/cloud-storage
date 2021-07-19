import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
import model.*;
import model.file.FileInfoMessage;
import model.file.FileInfoRequest;
import model.file.FileMessage;
import model.file.FileRequest;
import model.list.ListRequest;
import model.list.ListResponse;

public class MainController implements Initializable {
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

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setStatusBar("(>^_^)> hello! <(^_^<)");
        root = new File("./client/clientFiles");
        refresh();
        network = new NettyNetwork(command -> {
            if (command.getType() == CommandType.FILE_INFO_MESSAGE) {
                FileInfoMessage message = (FileInfoMessage) command;
                Platform.runLater(() -> {
                    fileInfoServer.setText("name: " + message.getFileName() + " size: " + message.getFileSize() + " byte");
                });
            } else if (command.getType() == CommandType.LIST_RESPONSE) {
                ListResponse files = (ListResponse) command;
                Platform.runLater(() -> {
                    listViewServer.getItems().clear();
                    listViewServer.getItems().addAll(files.getNames());
                });
            } else if (command.getType() == CommandType.FILE_MESSAGE) {
                FileMessage fileMessage = (FileMessage) command;
                copyFile(fileMessage.getFile(), new File(root + "/" + fileMessage.getFileName()));
                refreshAll();
                setStatusBar("the file is uploaded!(^-^)");
            }
        });
        listViewListener();
    }

    @FXML
    public void toTheServer(ActionEvent actionEvent) {
        String fileName = listViewClient.getFocusModel().getFocusedItem();
        File file = new File(root + "/" + fileName);
        network.writeMessage(new FileMessage(file, file.getName(), file.length()));
        setStatusBar("the file has been sent!(>_<)");
    }

    @FXML
    public void fromTheServer(ActionEvent actionEvent) {
        network.writeMessage(new FileRequest(listViewServer.getFocusModel().getFocusedItem()));
    }

    @SneakyThrows
    @FXML
    public void refreshAll() {
        network.writeMessage(new ListRequest());
        refresh();
    }

    private void refresh() {
        Platform.runLater(() -> {
            listViewClient.getItems().clear();
            listViewClient.getItems().addAll(root.list());
        });
    }

    private void copyFile(File fileServer, File newFile) throws IOException {
        if (fileServer.exists()) {
            Files.copy(fileServer.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private void listViewListener() {
        Platform.runLater(() -> {
            MultipleSelectionModel<String> selectionModelClient = listViewClient.getSelectionModel();
            MultipleSelectionModel<String> selectionModelServer = listViewServer.getSelectionModel();
            selectionModelClient.selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> fileInfoClient.setText("name: " + newValue +
                            " size: " + new File(root + "/" + newValue).length() + " byte"));
            selectionModelServer.selectedItemProperty()
                    .addListener((observable, oldValue, newValue) -> network.writeMessage(new FileInfoRequest(newValue)));
        });
    }

    private void setStatusBar(String str) {
        statusBar.setText(str);
    }
}
