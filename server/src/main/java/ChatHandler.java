import java.io.*;
import java.net.Socket;

public class ChatHandler implements Runnable {

    private Socket socket;
    private byte [] buffer;
    private DataInputStream is;
    private DataOutputStream os;

    public ChatHandler(Socket socket) {
        this.socket = socket;
        buffer = new byte[256];
    }

    @Override
    public void run() {
        try {
            is = new DataInputStream(socket.getInputStream());
            os = new DataOutputStream(socket.getOutputStream());
            while (true) {
                receivingFile();
                int read = is.read(buffer);
                System.out.println("Received: " + new String(buffer, 0, read));
                os.write(buffer, 0, read);
                os.flush();
            }
        } catch (Exception e) {
            System.err.println("Client connection exception");
        } finally {
            try {
                is.close();
                os.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    private void receivingFile() throws IOException {
        String fileName = is.readUTF();
        Long fileSize = is.readLong();
        FileOutputStream fOut = new FileOutputStream("./server/" + fileName);
        byte[] bt = new byte[256];
        while ((is.read(bt)) > 0) {
            fOut.write(bt);
        }
    }
}