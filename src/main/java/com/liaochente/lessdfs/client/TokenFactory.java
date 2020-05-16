package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.client.constant.LessClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class TokenFactory {
    private final static Logger LOG = LoggerFactory.getLogger(TokenFactory.class);

    private final static BlockingDeque<Long> TOKEN_QUEUE = new LinkedBlockingDeque<>();

    private final static AtomicLong CURRENT_TOKEN_COUNT = new AtomicLong(Long.MIN_VALUE);

    private final static int STEP = 1024;

    private final static Long MAX_TOKEN_COUNT = Long.MAX_VALUE;

    private static ScheduledFuture future;

    static {
        future = LessClientConfig.GLOBAL_SCHEDULED_THREAD_POOL.scheduleAtFixedRate(() -> {
            try {
                if (isAugment()) {
                    LOG.debug("需要扩充令牌桶");
                    TokenFactory.augment();
                }
            } catch (InterruptedException e) {
                future.cancel(true);
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    private TokenFactory() {
        //emtpy
    }

    public final static long getToken() throws InterruptedException {
        return TOKEN_QUEUE.take();
    }

    public final static void releaseToken(long token) {
        try {
            LOG.debug("释放token start");
            TOKEN_QUEUE.put(token);
            LOG.debug("释放token end");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static boolean isAugment() {
        return TOKEN_QUEUE.size() < 10;
    }

    private static void augment() throws InterruptedException {
        LOG.debug("CURRENT_TOKEN_COUNT={}, MAX_TOKEN_COUNT={}", CURRENT_TOKEN_COUNT.get(), MAX_TOKEN_COUNT);
        if (CURRENT_TOKEN_COUNT.get() == MAX_TOKEN_COUNT) {
            //可用令牌已经全部扩充耗尽
            LOG.debug("可用令牌已经全部扩充耗尽");
            throw new InterruptedException();
        }

        long currentTokenCount = CURRENT_TOKEN_COUNT.get();

        long length = TokenFactory.STEP;
        if (currentTokenCount + TokenFactory.STEP > TokenFactory.MAX_TOKEN_COUNT) {
            length = TokenFactory.MAX_TOKEN_COUNT - currentTokenCount;
        }

        for (long i = 0; i < length; i++) {
            long token = CURRENT_TOKEN_COUNT.incrementAndGet();
            TOKEN_QUEUE.put(token);
        }
        LOG.debug("令牌桶扩充完毕: addTokenNum={}, minToken={}, maxToken={}, currentQueueSize={}", length, currentTokenCount + 1, CURRENT_TOKEN_COUNT.get(), TOKEN_QUEUE.size());
    }

}
