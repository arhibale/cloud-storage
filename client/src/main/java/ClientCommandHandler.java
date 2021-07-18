import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import model.AbstractCommand;

import java.io.IOException;

public class ClientCommandHandler extends SimpleChannelInboundHandler<AbstractCommand> {

    private final CallBack callBack;

    public ClientCommandHandler(CallBack callBack) {
        this.callBack = callBack;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractCommand command) throws IOException {
        callBack.call(command);
    }
}