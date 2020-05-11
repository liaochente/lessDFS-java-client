package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.client.handler.LessClientAuthHandler;
import com.liaochente.lessdfs.protcotol.LessMessageType;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class ClientBootstrap {
    public static Channel CHANNEL = null;

    public static void main(String[] args) {
        //1.定义服务类
        Bootstrap clientBootstap = new Bootstrap();

        //2.定义执行线程组
        EventLoopGroup worker = new NioEventLoopGroup();

        //3.设置线程池
        clientBootstap.group(worker);

        clientBootstap.option(ChannelOption.TCP_NODELAY, true);
        //4.设置通道
        clientBootstap.channel(NioSocketChannel.class);

        //5.添加Handler
        clientBootstap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) throws Exception {
                System.out.println("client channel init!");
                ChannelPipeline pipeline = channel.pipeline();
                pipeline.addLast(new LoggingHandler(LogLevel.DEBUG));
                //进站handler
                pipeline.addLast(new LengthFieldBasedFrameDecoder(1024 * 100, 0, 4,
                        0, 4));

                //出站handler
                pipeline.addLast(new LengthFieldPrepender(4));
                pipeline.addLast(new LessClientAuthHandler());
            }
        });

        //6.建立连接
        ChannelFuture channelFuture = clientBootstap.connect("127.0.0.1", 8888);
        try {
            CHANNEL = channelFuture.sync().channel();
            CHANNEL.closeFuture().sync();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //8.关闭连接
            worker.shutdownGracefully();
        }
    }
}
