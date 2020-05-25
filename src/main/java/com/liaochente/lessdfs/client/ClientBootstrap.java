package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.client.constant.LessClientConfig;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Netty 启动引导类
 */
public class ClientBootstrap {

    private final static Logger LOG = LoggerFactory.getLogger(ClientBootstrap.class);

    private Bootstrap bootstrap = new Bootstrap();

    private EventLoopGroup worker = new NioEventLoopGroup();

    private ChannelHandler channelHandler;

    private ChannelFuture channelFuture;

    private Channel channel;

    public ClientBootstrap(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
        try {
            this.connect();
        } catch (InterruptedException e) {
            try {
                shutdown();
            } catch (InterruptedException interruptedException) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException(e);
        }
    }

    public Channel getChannel() {
        return channel;
    }

    /**
     * 连接远程服务器
     *
     * @return
     * @throws InterruptedException
     */
    public ChannelFuture connect() throws InterruptedException {
        bootstrap.group(worker);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                pipeline.addLast(new LengthFieldBasedFrameDecoder(LessClientConfig.maxFrameLength, 0, 4,
                        0, 4));
                pipeline.addLast(channelHandler);
                pipeline.addLast(new LengthFieldPrepender(4));
            }
        });

        channelFuture = bootstrap.connect(LessClientConfig.serverAddress, LessClientConfig.port).sync();
        LOG.debug("bootstrap.connect finished");
        channel = channelFuture.channel();
        return channelFuture;
    }

    /**
     * 释放资源
     *
     * @throws InterruptedException
     */
    public void shutdown() throws InterruptedException {
        worker.shutdownGracefully();
    }
}
