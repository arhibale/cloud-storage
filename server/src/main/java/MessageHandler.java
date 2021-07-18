import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import model.Message;

import java.io.File;


@Slf4j
public class MessageHandler extends SimpleChannelInboundHandler<Message> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message message) throws Exception {
        log.debug("received: {}", message);
        if (message.getContent().equals("dir")) {
            dirFromClient(ctx);
        }
    }

    private void dirFromClient(ChannelHandlerContext ctx) {
        File file = new File("./server/serverFiles");
        ctx.writeAndFlush(new Message(file.list()));
    }
}
