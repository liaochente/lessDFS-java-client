package com.liaochente.lessdfs.client;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ClientTest {

    private static AtomicLong totalTimeConsuming = new AtomicLong(0);
    private static AtomicLong successRequest = new AtomicLong(0);
    private static AtomicLong failRequest = new AtomicLong(0);

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(100);
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(200, 500, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        DefaultLessDFSClient client = new DefaultLessDFSClient();

        try {
            Thread.sleep(5*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            Future future = poolExecutor.submit(() -> {
                try {
                    countDownLatch.await();
                    long startTime = System.currentTimeMillis();
                    String fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
                    long endTime = System.currentTimeMillis();
                    System.out.println("本次请求耗时 " + (endTime - startTime) + "MS");
                    totalTimeConsuming.addAndGet(endTime - startTime);
                    if (fileName != null && !"".equals(fileName)) {
                        successRequest.incrementAndGet();
                    } else {
                        failRequest.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            futures.add(future);
            countDownLatch.countDown();
        }

        for (Future future : futures) {
            try {
                Object obj = future.get();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        System.out.println("totalTimeConsuming = " + totalTimeConsuming.get() + "MS, avg = " + (totalTimeConsuming.get() / 100) + "MS, successRequest = " + successRequest.get() + ", failRequest = " + failRequest.get());
//
//        try {
//            System.out.println("fileName =" + fileName);
//            fileName = client.upload("/Users/liaochente/cleanMavenRepository.sh", "sh");
//            System.out.println("fileName =" + fileName);

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

//            boolean success = client.delete("L0/73f4b3b398f24ef89aa7ea31ad2dab0b");
//            System.out.println("delete status : " + success);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
    }
}
