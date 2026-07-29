package dev.minixr9k;

import dev.minixr9k.handlers.HandshakeState;
import dev.minixr9k.utils.ProtocolDetector;
import dev.minixr9k.utils.VarIntFrameDecoder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NetworkServer {
    private final int port;

    public NetworkServer(int port) {
        this.port = port;
    }

    public void start() {
        boolean useEpoll = Epoll.isAvailable();

        System.out.println("[Network] Ивент-луп выбран: " + (useEpoll ? "Epoll (Linux Native)" : "NIO (Java)"));

        // 1. Создаем группы потоков в зависимости от ОС
        EventLoopGroup bossGroup = useEpoll ? new EpollEventLoopGroup(1) : new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = useEpoll ? new EpollEventLoopGroup(4) : new NioEventLoopGroup(4);

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
                    .channel(useEpoll ? EpollServerSocketChannel.class : NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
//                            ch.pipeline().addLast("splitter", new VarIntFrameDecoder());
//                            ch.pipeline().addLast("handler", new HandshakeState());
                            ch.pipeline().addLast("detector", new ProtocolDetector());
                        }
                    });

            ChannelFuture f = b.bind(port).sync();
            System.out.println("Server bound to port " + port);
            f.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }
}