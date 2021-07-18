import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.AbstractCommand;
import model.ListResponse;

import java.nio.file.Paths;


@Slf4j
public class CommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws Exception {
        log.debug("received: {}", command.getType());
        switch (command.getType()) {
            case LIST_REQUEST:
                ctx.writeAndFlush(new ListResponse(Paths.get("server", "serverFiles")));
                break;
        }
    }
}
