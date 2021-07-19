import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.*;
import model.file.FileInfoMessage;
import model.file.FileInfoRequest;
import model.file.FileMessage;
import model.file.FileRequest;
import model.list.ListResponse;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;


@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private final String root = "./server/serverFiles";

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws Exception {
        log.debug("received: {}", command.getType());

        if (command.getType() == CommandType.FILE_INFO_REQUEST) {
            FileInfoRequest fileInfoRequest = (FileInfoRequest) command;
            searchAndSend(ctx, fileInfoRequest.getFileName(), command);
        } else if (command.getType() == CommandType.LIST_REQUEST) {
            ctx.writeAndFlush(new ListResponse(Paths.get(root)));
        } else if (command.getType() == CommandType.FILE_REQUEST) {
            FileRequest request = (FileRequest) command;
            searchAndSend(ctx, request.getFileName(), command);
        } else if (command.getType() == CommandType.FILE_MESSAGE) {
            FileMessage fileMessage = (FileMessage) command;
            copyFile(fileMessage.getFile(), new File(root + "/" + fileMessage.getFileName()));
            ctx.writeAndFlush(new ListResponse(Paths.get(root)));
        }
    }

    private void searchAndSend(ChannelHandlerContext ctx, String str, AbstractCommand commandType) {
        for (String fileName : Objects.requireNonNull(new File(root).list())) {
            if (fileName.equals(str)) {
                File file = new File(root + "/" + fileName);
                if (commandType.getType() == CommandType.FILE_REQUEST) {
                    ctx.writeAndFlush(new FileMessage(file, file.getName(), file.length()));
                } else if (commandType.getType() == CommandType.FILE_INFO_REQUEST) {
                    ctx.writeAndFlush(new FileInfoMessage(file.getName(), file.length()));
                }
            }
        }
    }

    private void copyFile(File fileClient, File newFile) throws IOException {
        if (fileClient.exists()) {
            Files.copy(fileClient.toPath(), newFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}
