package com.liaochente.lessdfs.client;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class ClientTest {

    public static void main(String[] args) {
        DefaultLessDFSClient client = new DefaultLessDFSClient();
        try {
            String fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
            System.out.println("fileName =" + fileName);
            fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
            System.out.println("fileName =" + fileName);

//            byte[] b = client.download("L0/3ab47e94a223443bb345afa590f21835", inputStream -> {
//                byte[] results = null;
//                try {
//                    results = new byte[inputStream.available()];
//                    inputStream.read(results);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return results;
//            });
//
//            try {
//                ByteBuffer byteBuffer = ByteBuffer.allocate(b.length);
//                byteBuffer.put(b);
//                byteBuffer.flip();
//                FileChannel fileChannel = new FileOutputStream(new File("/Users/liaochente/less_down/down2.sh"), false).getChannel();
//                fileChannel.write(byteBuffer);
//                byteBuffer.clear();
//                fileChannel.close();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            boolean success = client.delete("L0/73f4b3b398f24ef89aa7ea31ad2dab0b");
            System.out.println("delete status : " + success);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
