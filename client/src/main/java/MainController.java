import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import lombok.SneakyThrows;
import model.CommandType;
import model.ListRequest;
import model.ListResponse;

public class MainController implements Initializable {
    @FXML
    private ListView<String> listViewClient;
    @FXML
    private ListView<String> listViewServer;
    @FXML
    private TextField statusBar;

    private NettyNetwork network;

    @SneakyThrows
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refresh();
    }

    @FXML
    public void toTheServer(ActionEvent actionEvent) {

    }

    @FXML
    public void fromTheServer(ActionEvent actionEvent) {

    }

    private void refresh() {
        File dir = new File("./client/clientFiles");
        listViewClient.getItems().clear();
        listViewClient.getItems().addAll(dir.list());

        network = new NettyNetwork(command -> {
            switch (command.getType()) {
                case LIST_RESPONSE:
                    ListResponse files = (ListResponse) command;
                    Platform.runLater(() -> {
                        listViewServer.getItems().clear();
                        listViewServer.getItems().addAll(files.getNames());
                        statusBar.setText("true");
                    });
                    break;
            }
        });
    }

    @SneakyThrows
    @FXML
    public void refreshAll() {
        network.writeMessage(new ListRequest());
        refresh();
    }
}
