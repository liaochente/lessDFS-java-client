package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.client.util.StopWatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class ClientTest {

    private final static Logger LOG = LoggerFactory.getLogger(ClientTest.class);
    private final static int REQUEST_NUMBER = 1;
    private static AtomicLong total = new AtomicLong(0);
    private static AtomicLong successRequest = new AtomicLong(0);
    private static AtomicLong failRequest = new AtomicLong(0);

    private final static void printStopWatchs() {
        if (StopWatch.OPEN) {
            List<StopWatch> stopWatches = StopWatch.STOP_WATCHES;
            LOG.debug("*************** 执行时间统计数据 ***************");
            Set<String> set = new HashSet<>();
            stopWatches.forEach(e -> set.add(e.getThreadName()));

            set.forEach(e -> {
                stopWatches.forEach(sw -> {
                    if (sw.getThreadName().equals(e)) {
                        List<Map<String, Object>> tasks = sw.getTasks();
                        tasks.forEach(task -> {
                            //计算时间差
                            String taskName = (String) task.get("taskName");
                            Instant start = (Instant) task.get("start");
                            Instant end = (Instant) task.get("end");
                            long millis = Duration.between(start, end).toMillis();
                            LOG.debug(" Thread-[{}] TaskName-[{}] millis-[{}] ms ", sw.getThreadName(), taskName, millis);
                        });
                    }
                });
            });
        }
        LOG.debug("*************** 外部调用统计 ***************");
        LOG.debug("总耗时: {}ms, 平均耗时: {}ms", total.get(), total.get() / (successRequest.get() - failRequest.get()));
        LOG.debug("***************            ***************");
    }

    public static void main(String[] args) {
        CountDownLatch countDownLatch = new CountDownLatch(REQUEST_NUMBER);
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(REQUEST_NUMBER, 500, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));
        DefaultLessDFSClient client = DefaultLessDFSClient.newInstance();

        try {
            Thread.sleep(5 * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        List<Future> futures = new ArrayList<>();
        for (int i = 0; i < REQUEST_NUMBER; i++) {
            Future future = poolExecutor.submit(() -> {
                try {
                    countDownLatch.await();
                    Instant start = Instant.now();
                    String fileName = client.upload("/Users/liaochente/CSS禅意花园.pdf", "pdf");
                    Instant end = Instant.now();

                    LOG.debug(">>>> 单次请求耗时 {} ms", Duration.between(start, end).toMillis());
                    total.addAndGet(Duration.between(start, end).toMillis());

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

        //打印时间统计
        printStopWatchs();
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
