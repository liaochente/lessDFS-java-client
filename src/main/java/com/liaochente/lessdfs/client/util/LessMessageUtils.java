package com.liaochente.lessdfs.client.util;

import com.liaochente.lessdfs.protcotol.LessMessageType;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class LessMessageUtils {

    public final static int MAGIC_CODE = 0x294;

    public static Long SESSIONID = null;

    public final static ByteBuf createAuthMessage(String password) {
        byte[] body = password.getBytes();
        Integer length = body.length;
        Long sessionId = 0L;
        byte type = (byte) LessMessageType.LOGIN_IN.getType();
        byte priority = 0;
        ByteBuf byteBuf = Unpooled.buffer(24 + length);
        try {
            byteBuf.writeInt(MAGIC_CODE);
            byteBuf.writeLong(sessionId);
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeBytes(new byte[1]);
            byteBuf.writeBytes(new byte[5]);
            byteBuf.writeInt(length);
            byteBuf.writeBytes(body);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return byteBuf;
    }

    public final static ByteBuf createUploadFileMessage(String fileExt, String path) {
        byte type = (byte) LessMessageType.UPLOAD_FILE_IN.getType();
        byte priority = 0;
        try {
            File file = new File(path);
            RandomAccessFile aFile = new RandomAccessFile("/Users/liaochente/cleanMavenRepository.sh", "rw");
            FileChannel fileChannel = aFile.getChannel();
            // 3 创建ByteBuffer缓存
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) file.length());

            // 4 把通道的数据写入到缓存
            fileChannel.read(byteBuffer);

            byte[] data = byteBuffer.array();

            ByteBuf byteBuf = Unpooled.buffer(20 + 4 + fileExt.length() + 4 + data.length);
            byteBuf.writeInt(MAGIC_CODE);
            byteBuf.writeLong(SESSIONID);
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeBytes(new byte[1]);//status
            byteBuf.writeBytes(new byte[5]);
            byteBuf.writeInt(fileExt.length());
            byteBuf.writeBytes(fileExt.getBytes());


            byteBuf.writeInt(data.length);
            byteBuf.writeBytes(data);
            return byteBuf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public final static ByteBuf createDownloadFileMessage(String path) {
        byte type = (byte) LessMessageType.DOWNLOAD_FILE_IN.getType();
        byte priority = 0;
        try {

            ByteBuf byteBuf = Unpooled.buffer(20 + 4 + path.length());
            byteBuf.writeInt(MAGIC_CODE);
            byteBuf.writeLong(SESSIONID);
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeBytes(new byte[1]);//status
            byteBuf.writeBytes(new byte[5]);
            byteBuf.writeInt(path.length());
            byteBuf.writeBytes(path.getBytes());
            return byteBuf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static ByteBuf createDeleteFileMessage(String path) {
        byte type = (byte) LessMessageType.DELETE_FILE_IN.getType();
        byte priority = 0;
        try {

            ByteBuf byteBuf = Unpooled.buffer(20 + 4 + path.length());
            byteBuf.writeInt(MAGIC_CODE);
            byteBuf.writeLong(SESSIONID);
            byteBuf.writeByte(type);
            byteBuf.writeByte(priority);
            byteBuf.writeBytes(new byte[1]);//status
            byteBuf.writeBytes(new byte[5]);
            byteBuf.writeInt(path.length());
            byteBuf.writeBytes(path.getBytes());
            return byteBuf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
