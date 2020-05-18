package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.DownloadCallback;
import com.liaochente.lessdfs.client.constant.LessClientConfig;
import com.liaochente.lessdfs.client.constant.LessStatus;
import com.liaochente.lessdfs.client.util.StopWatch;
import com.liaochente.lessdfs.protcotol.LessMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 默认客户端实现
 * 提供文件上传、下载、删除功能
 */
public class DefaultLessDFSClient implements ILessDFSClient {

    private final static Logger LOG = LoggerFactory.getLogger(DefaultLessDFSClient.class);

    /**
     * ChannelHandler日志对象
     */
    private final static Logger HANDLER_LOG = LoggerFactory.getLogger(ClientInHandler.class);

    /**
     * 客户端实例
     */
    private final static DefaultLessDFSClient DEFAULT_LESS_DFS_CLIENT = new DefaultLessDFSClient();

    /**
     * 客户端异步请求结果池
     * 功能：存放所有异步请求结果
     */
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

    /**
     * 客户端异步请求结果类
     * 功能：用于获取客户端的异步请求结果
     *
     * @param <T>
     */
    private class ClientWriteFuture<T> implements AutoCloseable {

        /**
         * 请求标识
         */
        private long sessionId;

        private CountDownLatch countDownLatch = new CountDownLatch(1);

        /**
         * 请求是否成功标识
         */
        private boolean success;

        /**
         * 请求结果状态
         */
        private byte status;

        /**
         * 请求结果数据
         */
        private T result;

        public ClientWriteFuture(long sessionId) {
            this.sessionId = sessionId;
            ClientWriteFuturePool.put(this.sessionId, this);
        }

        /**
         * 获得请求结果，在结果返回之前阻塞当前线程
         *
         * @return
         * @throws InterruptedException
         */
        public T get() throws InterruptedException {
            LOG.debug("等待请求结果返回 [SESSIONID={}]", this.sessionId);
            this.countDownLatch.await(60, TimeUnit.SECONDS);
            LOG.debug("结果已返回 [SESSIONID={}, success={}, status={}, result={}]", this.sessionId, this.success, this.status, this.result);
            return result;
        }

        /**
         * 设置请求结果
         *
         * @param status
         */
        public void set(byte status) {
            set(status, null);
        }

        /**
         * 设置请求结果
         *
         * @param result
         */
        public void set(T result) {
            set((byte) LessStatus.OK.getStatus(), result);
        }

        /**
         * 设置请求结果
         *
         * @param status
         * @param result
         */
        public void set(byte status, T result) {
            this.result = result;
            this.status = status;
            this.success = LessStatus.convert(status) == LessStatus.OK;
            this.countDownLatch.countDown();
            LOG.debug("请求结果已返回 token={}", this.sessionId);
        }

        public boolean isSuccess() {
            return success;
        }

        public byte getStatus() {
            return status;
        }

        @Override
        public void close() throws Exception {
            LOG.debug("dispose clientWriteFuture object [SESSIONID={}]", this.sessionId);
            ClientWriteFuturePool.remove(this.sessionId);
        }
    }

    /**
     * Netty Client
     */
    private ClientBootstrap clientBootstrap;

    /**
     * 处理服务器响应报文
     */
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

            if (LessStatus.convert(status) == LessStatus.OK) {
                //处理上传响应
                if (LessMessageType.UPLOAD_FILE_OUT == LessMessageType.convert(type)) {
                    int fileNameLength = buf.readInt();
                    byte[] temps = new byte[fileNameLength];
                    buf.readBytes(temps);
                    String fileName = new String(temps);

                    int fileExtLength = buf.readInt();
                    temps = new byte[fileExtLength];
                    buf.readBytes(temps);
                    String fileExt = new String(temps);
                    ClientWriteFuturePool.get(sessionId).set(fileName);
                }

                //处理下载响应
                if (LessMessageType.DOWNLOAD_FILE_OUT == LessMessageType.convert(type)) {
                    int fileNameLength = buf.readInt();
                    byte[] buffers = new byte[fileNameLength];
                    buf.readBytes(buffers);
                    String fileName = new String(buffers);

                    int fileLength = buf.readInt();
                    byte[] fileBytes = new byte[fileLength];
                    buf.readBytes(fileBytes);
                    ClientWriteFuturePool.get(sessionId).set(new ByteArrayInputStream(fileBytes));
                }

                //处理删除响应
                if (LessMessageType.DELETE_FILE_OUT == LessMessageType.convert(type)) {
                    ClientWriteFuturePool.get(sessionId).set(true);
                }
            } else {
                ClientWriteFuturePool.get(sessionId).set(status);
            }

//            HANDLER_LOG.debug("sessionId = {}, type = {}", sessionId, LessMessageType.convert(type));

            ctx.fireChannelRead(msg);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
            LOG.error("应答报文解析异常", cause);
            super.exceptionCaught(ctx, cause);
        }
    }

    /**
     * 构造方法
     */
    private DefaultLessDFSClient() {
        LOG.debug("start the task in the token factory.");
        TokenFactory.startTokenTask();
        LOG.debug("init clientBootstrap start");
        this.clientBootstrap = new ClientBootstrap(new ClientInHandler());
        LOG.debug("init clientBootstrap finished");
    }

    /**
     * 获取客户端实例
     *
     * @return
     */
    public final static DefaultLessDFSClient newInstance() {
        return DEFAULT_LESS_DFS_CLIENT;
    }

    @Override
    public String upload(byte[] fileBytes, String fileExt) {
        StopWatch stopWatch = new StopWatch();

        byte type = (byte) LessMessageType.UPLOAD_FILE_IN.getType();
        byte priority = 0;
        byte[] passwords = LessClientConfig.PASSWORD.getBytes();
        stopWatch.start("获得token");
        try (
                TokenFactory.Token token = TokenFactory.getToken();
                ClientWriteFuture<String> writeFuture = new ClientWriteFuture<>(token.longValue())
        ) {
            stopWatch.stop();

            ByteBuf byteBuf = Unpooled.buffer(15 + 4 + passwords.length + 4 + fileExt.length() + 4 + fileBytes.length);
            byteBuf.writeInt(LessClientConfig.MAGIC_CODE);
            byteBuf.writeLong(token.longValue());
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeByte(0);//status
            byteBuf.writeInt(passwords.length);
            byteBuf.writeBytes(passwords);
            byteBuf.writeInt(fileExt.length());
            byteBuf.writeBytes(fileExt.getBytes());
            byteBuf.writeInt(fileBytes.length);
            byteBuf.writeBytes(fileBytes);

            stopWatch.start("发送文件");
            clientBootstrap.getChannel().writeAndFlush(byteBuf).sync();
            stopWatch.stop();
            return writeFuture.get();
        } catch (Exception e) {
            LOG.error("upload exception", e);
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
            StopWatch stopWatch = new StopWatch();
            stopWatch.start("read file");

            byte[] fileBytes = new byte[inputStream.available()];
            inputStream.read(fileBytes);

            stopWatch.stop();

            return this.upload(fileBytes, fileExt);
        } catch (IOException e) {
            LOG.error("upload exception", e);
        }
        return null;
    }

    @Override
    public InputStream download(String fileName) {
        byte type = (byte) LessMessageType.DOWNLOAD_FILE_IN.getType();
        byte priority = 0;
        byte[] passwords = LessClientConfig.PASSWORD.getBytes();
        try (
                TokenFactory.Token token = TokenFactory.getToken();
                ClientWriteFuture<InputStream> writeFuture = new ClientWriteFuture<>(token.longValue())
        ) {
            ByteBuf byteBuf = Unpooled.buffer(15 + 4 + passwords.length + 4 + fileName.length());
            byteBuf.writeInt(LessClientConfig.MAGIC_CODE);
            byteBuf.writeLong(token.longValue());
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeByte(0);//status
            byteBuf.writeInt(passwords.length);
            byteBuf.writeBytes(passwords);
            byteBuf.writeInt(fileName.length());
            byteBuf.writeBytes(fileName.getBytes());

            clientBootstrap.getChannel().writeAndFlush(byteBuf).sync();
            return writeFuture.get();
        } catch (Exception e) {
            LOG.error("download exception", e);
        }
        return null;
    }

    @Override
    public <T> T download(String fileName, DownloadCallback<T> callback) {
        InputStream inputStream = this.download(fileName);
        return callback.receive(inputStream);
    }

    @Override
    public Boolean delete(String fileName) {
        byte type = (byte) LessMessageType.DELETE_FILE_IN.getType();
        byte priority = 0;
        byte[] passwords = LessClientConfig.PASSWORD.getBytes();
        try (
                TokenFactory.Token token = TokenFactory.getToken();
                ClientWriteFuture<Boolean> writeFuture = new ClientWriteFuture<>(token.longValue())
        ) {
            ByteBuf byteBuf = Unpooled.buffer(15 + 4 + passwords.length + 4 + fileName.length());
            byteBuf.writeInt(LessClientConfig.MAGIC_CODE);
            byteBuf.writeLong(token.longValue());
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeByte(0);//status
            byteBuf.writeInt(passwords.length);
            byteBuf.writeBytes(passwords);
            byteBuf.writeInt(fileName.length());
            byteBuf.writeBytes(fileName.getBytes());

            clientBootstrap.getChannel().writeAndFlush(byteBuf).sync();
            return writeFuture.get();
        } catch (Exception e) {
            LOG.error("delete exception", e);
        }
        return false;
    }
}
