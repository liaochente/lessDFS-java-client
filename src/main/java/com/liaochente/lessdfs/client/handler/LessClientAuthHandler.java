package com.liaochente.lessdfs.client.handler;

import com.liaochente.lessdfs.client.util.LessMessageUtils;
import com.liaochente.lessdfs.protcotol.LessMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.SocketAddress;

public class LessClientAuthHandler extends ChannelInboundHandlerAdapter {

    static final Logger LOG = LoggerFactory.getLogger(LessClientAuthHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        ctx.writeAndFlush(LessMessageUtils.createAuthMessage("123456"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        LOG.debug("LessClientAuthHandler.channelRead msg = {}", msg);
        LOG.debug("LessClientAuthHandler.channelRead msg->ByteBuf = {}", ((ByteBuf) msg).readableBytes());
        ByteBuf buf = (ByteBuf) msg;
        Integer magicCode = buf.readInt();
        if (magicCode != 0x76) {
            LOG.debug("error|文件头不正确");
            //todo exception
            throw new RuntimeException("error: 文件头不正确");
        }
        long sessionId = buf.readLong();


        byte type = buf.readByte();
        byte priority = buf.readByte();
        byte status = buf.readByte();
        //丢弃48位保留字节
        ByteBuf placeholderByteBuf = buf.readBytes(5);

        if (LessMessageType.LOGIN_OUT == LessMessageType.convert(type)) {
            LessMessageUtils.SESSIONID = sessionId;

            LOG.debug("获得 登录认证应答 >>> {}", sessionId);
            //todo测试发送文件

            ByteBuf byteBuf = LessMessageUtils.createUploadFileMessage("carp/sh/clean.sh", "sh", "/Users/liaochente/cleanMavenRepository.sh");
            LOG.debug("byteBuf 可写长度 = {}", byteBuf.readableBytes());
            ctx.writeAndFlush(byteBuf);
        }

    }
}
