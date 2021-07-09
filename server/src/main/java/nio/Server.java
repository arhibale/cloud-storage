package nio;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Server {

    private static int cnt = 1;
    private ServerSocketChannel sc;
    private Selector selector;
    private String name = "user";
    private String root = "./server/serverFiles/";

    public Server() throws IOException {
        sc = ServerSocketChannel.open();
        selector = Selector.open();
        sc.bind(new InetSocketAddress(8189));
        sc.configureBlocking(false);
        sc.register(selector, SelectionKey.OP_ACCEPT);

        while (sc.isOpen()) {
            selector.select();
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                if (key.isAcceptable()) {
                    handleAccept(key);
                }
                if (key.isReadable()) {
                    handleRead(key);
                }
                iterator.remove();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }

    private void handleRead(SelectionKey key) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(256);
        SocketChannel channel = (SocketChannel) key.channel();
        String name = (String) key.attachment();
        int read;
        StringBuilder sb = new StringBuilder();
        String str = "";
        while (true) {
            read = channel.read(buffer);
            buffer.flip();
            if (read == -1) {
                channel.close();
                break;
            }
            if (read > 0) {
                while (buffer.hasRemaining()) {
                    sb.append((char) buffer.get());
                }
                buffer.clear();
            } else {
                break;
            }
        }
        System.out.println(name + ": " + sb);
        if (isCommand(sb.toString())) {
            str = readCom(sb);
            channel.write(ByteBuffer.wrap((str + "\n").getBytes(StandardCharsets.UTF_8)));
        }
    }

    private boolean isCommand(String str) {
        return str.startsWith("ls") || str.startsWith("cat");
    }

    private void handleAccept(SelectionKey key) throws IOException {
        SocketChannel channel = sc.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, name + cnt);
        cnt++;
    }

    private String readCom(StringBuilder sb) throws IOException {
        String[] str = sb.toString().trim().split(" ");
        StringBuilder builder = new StringBuilder();
        Path path = Paths.get(root);

        if (str[0].equals("ls")) {
            List<Path> paths = Files.walk(path).collect(Collectors.toList());
            for (Path path1 : paths) {
                builder.append(path1.toString().substring(root.length() - 1)).append("\n");
            }
            System.out.println(builder);
            return builder.toString();
        } else if (str.length > 1 && str[0].equals("cat")) {
            if (Files.exists(Paths.get(root + str[1]))) {
                //Без понятия как быть с картинками.
                return String.valueOf(Files.readAllLines(Paths.get(root + str[1])));
            } else {
                return "the file does not exist";
            }
        } else {
            return "incorrect command input";
        }
    }

}