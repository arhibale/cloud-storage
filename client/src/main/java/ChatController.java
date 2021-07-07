import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;

public class ChatController implements Initializable {

    private DataInputStream is;
    private DataOutputStream os;
    private byte[] buffer;

    public ListView<String> listView;

    public TextField textField;

    public void send(ActionEvent actionEvent) throws IOException {
        String msg = textField.getText();
        os.write(msg.getBytes(StandardCharsets.UTF_8));
        os.flush();
        textField.clear();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        buffer = new byte[256];
        try {
            File dir = new File("./client");
            listView.getItems().clear();
            listView.getItems().addAll(dir.list());

            // ДЗ имя файла длина файла и сами байты
            // DataInputStream -> readUtf, readLong -> read(buffer)

            Socket socket = new Socket("localhost", 8189);
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            Thread readThread = new Thread(() -> {
                try {
                    while (true) {
                        sendingFile();
                        int read = is.read(buffer);
                        String msg = new String(buffer, 0, read);
                        Platform.runLater(() -> listView.getItems().add(msg));
                    }
                } catch (Exception e) {
                    System.err.println("Exception while read");
                }
            });
            readThread.setDaemon(true);
            readThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendingFile() throws IOException {
        File file = new File("./client/img.png");

        String fileName = file.getName();
        long fileSize = file.length();

        os.writeUTF(fileName);
        os.writeLong(fileSize);

        FileInputStream in = new FileInputStream(file);
        byte[] bt = new byte[256];
        while ((in.read(bt)) > 0) {
            os.write(bt);
        }
        os.flush();
    }
}