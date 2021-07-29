package com.arhibale.netty;

import com.arhibale.handlers.ClientCommandHandler;
import com.arhibale.netty.CallBack;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import lombok.extern.slf4j.Slf4j;
import model.AbstractCommand;
import model.list.ListRequest;

@Slf4j
public class NettyNetwork {

    private final String IP = "localhost";
    private final int PORT = 8189;
    private final EventLoopGroup worker;

    private SocketChannel channel;

    public NettyNetwork(CallBack callBack) {
        worker = new NioEventLoopGroup();
        Thread thread = new Thread(() -> {
            try {
                Bootstrap bootstrap = new Bootstrap();
                bootstrap.group(worker);
                bootstrap.channel(NioSocketChannel.class);
                bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel c) {
                        channel = c;
                        c.pipeline().addLast(
                                new ObjectEncoder(),
                                new ObjectDecoder(ClassResolvers.cacheDisabled(null)),
                                new ClientCommandHandler(callBack)
                        );
                    }
                });
                ChannelFuture future = bootstrap.connect(IP, PORT).sync();
                future.channel().closeFuture().sync();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                doStop();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void writeMessage(AbstractCommand command) {
        channel.writeAndFlush(command);
    }

    public void doStop() {
        worker.shutdownGracefully();
    }
}
