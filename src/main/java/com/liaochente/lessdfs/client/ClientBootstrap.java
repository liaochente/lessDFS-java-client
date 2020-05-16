package com.liaochente.lessdfs.client;

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

public class ClientBootstrap {

    private final static Logger LOG = LoggerFactory.getLogger(ClientBootstrap.class);

    //1.定义服务类
    private Bootstrap clientBootstap = new Bootstrap();

    //2.定义执行线程组
    private EventLoopGroup worker = new NioEventLoopGroup();

    private ChannelHandler channelHandler;

    private ChannelFuture channelFuture;

    private Channel channel;

    public ClientBootstrap(ChannelHandler channelHandler) {
        this.channelHandler = channelHandler;
        try {
            this.start();
        } catch (InterruptedException e) {
            try {
                shutdown();
            } catch (InterruptedException interruptedException) {
                throw new RuntimeException(e);
            }
            throw new RuntimeException(e);
        }
    }

    private ChannelFuture start() throws InterruptedException {
        //3.设置线程池
        clientBootstap.group(worker);

        clientBootstap.option(ChannelOption.TCP_NODELAY, true);
        //4.设置通道
        clientBootstap.channel(NioSocketChannel.class);

        //5.添加Handler
        clientBootstap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                //进站handler
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 100, 0, 4,
                        0, 4));
                pipeline.addLast(channelHandler);
                //出站handler
                pipeline.addLast(new LengthFieldPrepender(4));
            }
        });

        //6.建立连接
        channelFuture = clientBootstap.connect("127.0.0.1", 8888).sync();
        LOG.debug("clientBootstap.connect finished");
        channel = channelFuture.channel();
        return channelFuture;
    }

    public void shutdown() throws InterruptedException {
        channelFuture.channel().closeFuture().sync();
        worker.shutdownGracefully();
    }

    public Channel getChannel() {
        return channel;
    }
}
