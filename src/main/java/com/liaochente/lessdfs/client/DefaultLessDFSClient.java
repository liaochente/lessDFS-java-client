package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.DownloadCallback;
import com.liaochente.lessdfs.client.constant.LessClientConfig;
import com.liaochente.lessdfs.client.util.LessMessageUtils;
import com.liaochente.lessdfs.protcotol.LessMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

public class DefaultLessDFSClient implements ILessDFSClient {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultLessDFSClient.class);
    private final static Logger HANDLER_LOG = LoggerFactory.getLogger(ClientInHandler.class);

    private static class ClientWriteFuturePool {
        private final static Map<String, ClientWriteFuture> FUTURE_POOL = new ConcurrentHashMap<>();

        private final static String TOKEN_PREFIX = "tokn_";

        public final static void put(long sessionId, ClientWriteFuture future) {
            FUTURE_POOL.put(TOKEN_PREFIX + sessionId, future);
        }

        public final static ClientWriteFuture get(long sessionId) {
            return FUTURE_POOL.get(TOKEN_PREFIX + sessionId);
        }

        public final static void remove(long sessionId) {
            FUTURE_POOL.remove(TOKEN_PREFIX + sessionId);
        }
    }

    private class ClientWriteFuture<T> {

        private long sessionId;

        private CountDownLatch countDownLatch = new CountDownLatch(1);

        private T result;

        public ClientWriteFuture(long sessionId) {
            this.sessionId = sessionId;

            ClientWriteFuturePool.put(this.sessionId, this);
        }

        public T get() {
            try {
                LOG.debug("等待请求结果返回 sessionId={}", this.sessionId);
                this.countDownLatch.await();
                LOG.debug("结果已返回 result={}", this.result);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                LOG.debug("释放ClientWriteFuture.sessionId={}", this.sessionId);
                ClientWriteFuturePool.remove(this.sessionId);
            }
            LOG.debug("返回数据");
            return result;
        }

        public void set(T result) {
            this.result = result;
            this.countDownLatch.countDown();
            LOG.debug("请求结果已返回 token={}", this.sessionId);
        }
    }

    private ClientBootstrap clientBootstrap;

    private class ClientInHandler extends ChannelInboundHandlerAdapter {

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            HANDLER_LOG.debug("收到响应报文");
            //解析应答报文
            ByteBuf buf = (ByteBuf) msg;
            Integer magicCode = buf.readInt();
            Long sessionId = buf.readLong();
            byte type = buf.readByte();
            byte priority = buf.readByte();
            byte status = buf.readByte();
            HANDLER_LOG.debug("sessionId = {}, type = {}", sessionId, LessMessageType.convert(type));
            if (LessMessageType.UPLOAD_FILE_OUT == LessMessageType.convert(type)) {
                int fileNameLength = buf.readInt();
                byte[] temps = new byte[fileNameLength];
                buf.readBytes(temps);
                String fileName = new String(temps);

                int fileExtLength = buf.readInt();
                temps = new byte[fileExtLength];
                buf.readBytes(temps);
                String fileExt = new String(temps);

                HANDLER_LOG.debug("解析到已上传成功的文件名  >>> {}, 文件扩展名 >>> {}, status >>> {}", fileName, fileExt, status);
                ClientWriteFuturePool.get(sessionId).set(fileName);
            }
            ctx.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LOG.error("应答报文解析异常", cause);
            super.exceptionCaught(ctx, cause);
        }
    }

    public DefaultLessDFSClient() {
        LOG.debug("init clientBootstrap start");
        this.clientBootstrap = new ClientBootstrap(new ClientInHandler());
        LOG.debug("init clientBootstrap finished");
    }

    @Override
    public String upload(byte[] fileBytes, String fileExt) {
        byte type = (byte) LessMessageType.UPLOAD_FILE_IN.getType();
        byte priority = 0;
        byte[] passwords = LessClientConfig.PASSWORD.getBytes();
        Long token = null;
        try {
            token = TokenFactory.getToken();
            ByteBuf byteBuf = Unpooled.buffer(15 + 4 + passwords.length + 4 + fileExt.length() + 4 + fileBytes.length);
            byteBuf.writeInt(LessClientConfig.MAGIC_CODE);
            byteBuf.writeLong(token);
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeByte(0);//status
            byteBuf.writeInt(passwords.length);
            byteBuf.writeBytes(passwords);
            byteBuf.writeInt(fileExt.length());
            byteBuf.writeBytes(fileExt.getBytes());
            byteBuf.writeInt(fileBytes.length);
            byteBuf.writeBytes(fileBytes);

            clientBootstrap.getChannel().writeAndFlush(byteBuf).sync();
            ClientWriteFuture<String> writeFuture = new ClientWriteFuture<>(token);
            return writeFuture.get();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (token != null) {
                TokenFactory.releaseToken(token);
            }

        }
        return null;
    }

    @Override
    public String upload(String filePath, String fileExt) throws FileNotFoundException {
        return upload(Paths.get(filePath), fileExt);
    }

    @Override
    public String upload(Path filePath, String fileExt) throws FileNotFoundException {
        return upload(filePath.toFile(), fileExt);
    }

    @Override
    public String upload(File file, String fileExt) throws FileNotFoundException {
        return upload(new FileInputStream(file), fileExt);
    }

    @Override
    public String upload(InputStream inputStream, String fileExt) {
        try {
            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);
            return this.upload(fileBytes, fileExt);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public <T> T download(String fileName, DownloadCallback<T> callback) {
        return null;
    }

    @Override
    public void delete(String fileName) {

    }
}
