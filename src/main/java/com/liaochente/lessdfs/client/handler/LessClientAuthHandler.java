package com.liaochente.lessdfs.client.handler;

import com.liaochente.lessdfs.client.util.LessMessageUtils;
import com.liaochente.lessdfs.protcotol.LessMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LessClientAuthHandler extends ChannelInboundHandlerAdapter {

    static final Logger LOG = LoggerFactory.getLogger(LessClientAuthHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        //TODO 发送认证请求
        ctx.writeAndFlush(LessMessageUtils.createAuthMessage("123456"));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf buf = (ByteBuf) msg;
        Integer magicCode = buf.readInt();
        if (magicCode != 0x76) {
            LOG.debug("error|文件头不正确");
            throw new RuntimeException("error: 文件头不正确");
        }
        long sessionId = buf.readLong();


        byte type = buf.readByte();
        byte priority = buf.readByte();
        byte status = buf.readByte();
        //丢弃48位保留字节
        ByteBuf placeholderByteBuf = buf.readBytes(5);

        LOG.debug("MESSAGE HEADER >>> magicCode={}, sessionId={}, type={}, status={}", magicCode, sessionId, type, status);
        //todo 处理认证应答
        if (LessMessageType.LOGIN_OUT == LessMessageType.convert(type)) {
            LessMessageUtils.SESSIONID = sessionId;

            LOG.debug("获得 登录认证应答 sessionId >>> {}, status >>> {}", sessionId, status);
            //todo 发送上传文件请求
            ByteBuf byteBuf = LessMessageUtils.createUploadFileMessage("sh", "/Users/liaochente/cleanMavenRepository.sh");
            LOG.debug("byteBuf 可写长度 = {}", byteBuf.readableBytes());
            ctx.writeAndFlush(byteBuf);
        }

        //todo 处理上传文件应答
        if (LessMessageType.UPLOAD_FILE_OUT == LessMessageType.convert(type)) {
            int fileNameLength = buf.readInt();
            byte[] temps = new byte[fileNameLength];
            buf.readBytes(temps);
            String fileName = new String(temps);

            int fileExtLength = buf.readInt();
            temps = new byte[fileExtLength];
            buf.readBytes(temps);
            String fileExt = new String(temps);

            //todo 发送文件下载请求
            LOG.debug("解析到已上传成功的文件名  >>> {}, 文件扩展名 >>> {}, status >>> {}", fileName, fileExt, status);

            LOG.debug("下载刚解析到的文件");
            ByteBuf byteBuf = LessMessageUtils.createDownloadFileMessage(fileName);
            ctx.writeAndFlush(byteBuf);
        }

        //todo 处理文件下载应答
        if (LessMessageType.DOWNLOAD_FILE_OUT == LessMessageType.convert(type)) {
            byte[] buffers;
            int fileNameLength = buf.readInt();
            buffers = new byte[fileNameLength];
            buf.readBytes(buffers);
            String fileName = new String(buffers);

            int fileExtLength = buf.readInt();
            buffers = new byte[fileExtLength];
            buf.readBytes(buffers);
            String fileExt = new String(buffers);

            int dataLength = buf.readInt();
            buffers = new byte[dataLength];
            buf.readBytes(buffers);

            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(buffers.length);
                byteBuffer.put(buffers);
                byteBuffer.flip();
                FileChannel fileChannel = new FileOutputStream(new File("/Users/liaochente/less_down/down." + fileExt), false).getChannel();
                fileChannel.write(byteBuffer);
                byteBuffer.clear();
                fileChannel.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOG.error("异常", cause);
        super.exceptionCaught(ctx, cause);
    }
}
