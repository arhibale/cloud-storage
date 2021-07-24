package com.arhibale.handlers;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.file.FileInfo;
import model.file.FileMessage;
import model.file.FileRequest;
import model.list.ListRequest;
import model.list.ListResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private ChannelHandlerContext ctx;
    private final Path root = Paths.get("server/serverFiles");
    private Path path = root;
    private final String separator = "/";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws Exception {
        this.ctx = ctx;
        log.debug("received: {}", command.getType());

        if (command.getType() == CommandType.LIST_REQUEST) {
            ListRequest request = (ListRequest) command;
            if (request.getNameDirectory().equals("/up")) {
                Path p = path.getParent();
                if (p.equals(root)) {
                    path = p;
                }
            } else if (!request.getNameDirectory().equals("")) {
                File file = new File(path + separator + request.getNameDirectory());
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
            copyFile(fileMessage.getFile(), new File(path + separator + fileMessage.getFileName()));
            refresh();
        }
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
        for (String fileName : Objects.requireNonNull(new File(path + separator).list())) {
            if (fileName.equals(str)) {
                File file = new File(path + separator + fileName);
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
