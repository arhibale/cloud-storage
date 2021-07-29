package com.arhibale.handlers;

import com.arhibale.sql.AuthService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.auth.AuthRequest;
import model.auth.AuthResponse;
import model.file.*;
import model.list.ListRequest;
import model.list.ListResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.Objects;


@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private ChannelHandlerContext ctx;
    private final String theBeginningOfTheBeginning = "server/serverFiles/";
    private Path root = Paths.get(theBeginningOfTheBeginning);
    private Path path;
    private final String SEP = "\\";
    private String login;

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws Exception {
        this.ctx = ctx;

        if (command.getType() == CommandType.AUTH_REQUEST) {
            AuthService authService = new AuthService();
            authService.connectToSQL();
            AuthRequest authRequest = (AuthRequest) command;
            login = authService.getLoginByLoginPass(authRequest.getLogin(), authRequest.getPassword());
            if (login != null) {
                ctx.writeAndFlush(new AuthResponse("/auth", login));
                root = Paths.get(root + SEP + login);
                if (!Files.exists(root)) {
                    Files.createDirectory(root);
                }
                path = root;
            } else {
                ctx.writeAndFlush(new AuthResponse("/warn", ""));
            }
            authService.disconnectToSQL();
        } else if (command.getType() == CommandType.DISCONNECT) {
            root = Paths.get(theBeginningOfTheBeginning);
        } else if (command.getType() == CommandType.LIST_REQUEST) {
            ListRequest request = (ListRequest) command;
            if (request.getUpDirectory().equals("/up")) {
                Path p = path.getParent();
                if (p.equals(root)) {
                    path = p;
                }
            } else if (!request.getUpDirectory().equals("")) {
                File file = new File(path + SEP + request.getUpDirectory());
                if (file.isDirectory()) {
                    path = file.toPath();
                }
            }
            refresh();
        } else if (command.getType() == CommandType.FILE_REQUEST) {
            FileRequest fileRequest = (FileRequest) command;
            searchAndSend(fileRequest.getFileName(), fileRequest.isFileInfo());
        } else if (command.getType() == CommandType.FILE_MESSAGE) {
            FileMessage fileMessage = (FileMessage) command;
            copyFile(fileMessage.getFile(), new File(path + SEP + fileMessage.getFileName()));
            refresh();
        } else if (command.getType() == CommandType.FILE_DELETE) {
            try {
                FileDeleteRequest request = (FileDeleteRequest) command;
                Files.deleteIfExists(new File(path + SEP + request.getFileName()).toPath());
                refresh();
            } catch (DirectoryNotEmptyException e) {
                refresh();
            }
        } else if (command.getType() == CommandType.FILE_RENAME) {
            FileRenameRequest request = (FileRenameRequest) command;
            File file = new File(path + SEP + request.getOldName());
            log.debug("{} - {} - {} - {} - {}", request.getNewName(), request.getOldName(), path, file.exists(), file.getPath());
            if (file.exists()) {
                if (file.renameTo(new File(path + SEP + request.getNewName()))) {
                    refresh();
                }
            }
        } else if (command.getType() == CommandType.NEW_FOLDER) {
            CreateNewFolderRequest request = (CreateNewFolderRequest) command;
            Files.createDirectory(Paths.get(path + SEP + request.getName()));
            refresh();
        }
        log.debug("received {}: {}", login, command.getType());
    }

    private void copyFile(File oldFile, File newFile) {
        try {
            Files.copy(oldFile.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("an error has occurred: [" + oldFile + "] and [" + newFile + "]");
        }
    }

    private void refresh() throws IOException {
        ctx.writeAndFlush(new ListResponse(path));
    }

    private void searchAndSend(String str, boolean isFileInfo) {
        for (String fileName : Objects.requireNonNull(new File(path + SEP).list())) {
            if (fileName.equals(str)) {
                File file = new File(path + SEP + fileName);
                if (isFileInfo) {
                    ctx.writeAndFlush(new FileInfo(file.getName(), file.length()));
                } else {
                    if (!Files.isDirectory(file.toPath()) && file.exists()) {
                        ctx.writeAndFlush(new FileMessage(file, file.getName(), file.length()));
                    }
                }
            }
        }
    }
}
