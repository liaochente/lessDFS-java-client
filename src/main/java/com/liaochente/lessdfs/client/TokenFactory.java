package com.liaochente.lessdfs.client;

import com.liaochente.lessdfs.client.constant.LessClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 令牌生产工厂
 * 生产64位长度的唯一令牌
 */
public class TokenFactory {
    private final static Logger LOG = LoggerFactory.getLogger(TokenFactory.class);

    /**
     * 令牌存放桶
     */
    private final static BlockingDeque<Long> TOKEN_QUEUE = new LinkedBlockingDeque<>();

    /**
     * 当前已产生令牌数量
     */
    private final static AtomicLong CURRENT_TOKEN_COUNT = new AtomicLong(Long.MIN_VALUE);

    /**
     * 令牌桶扩容步长
     */
    private final static int STEP = 1024;

    /**
     * 令牌桶可存放最大令牌数
     */
    private final static Long MAX_TOKEN_COUNT = Long.MAX_VALUE;

    /**
     * 令牌桶扩容周期任务
     */
    private static ScheduledFuture future;

    private TokenFactory() {
        //emtpy
    }

    public final static void startTokenTask() {
        //创建扩容周期任务
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

    /**
     * 获取令牌
     *
     * @return
     * @throws InterruptedException
     */
    public final static long getToken() throws InterruptedException {
        LOG.debug("take token start.");
        long token = TOKEN_QUEUE.take();
        LOG.debug("take token end. token={}", token);
        return token;
    }

    /**
     * 释放令牌
     *
     * @param token
     */
    public final static void releaseToken(long token) {
        try {
            LOG.debug("release token start. token={}", token);
            TOKEN_QUEUE.put(token);
            LOG.debug("release token end.");
        } catch (InterruptedException e) {
            LOG.error("release token error", e);
        }
    }

    /**
     * 判断是否需要扩容
     * 默认令牌桶中令牌数量少于10就开始扩容
     *
     * @return
     */
    private static boolean isAugment() {
        return TOKEN_QUEUE.size() < 10;
    }

    /**
     * 扩容方法
     *
     * @throws InterruptedException
     */
    private static void augment() throws InterruptedException {
        LOG.debug("token queue has {} tokens.", CURRENT_TOKEN_COUNT.get());
        if (CURRENT_TOKEN_COUNT.get() == MAX_TOKEN_COUNT) {
            LOG.debug("The maximum number of tokens has been reached.");
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
        LOG.debug("add {} tokens to the token bucket, token queue has {} tokens.", length, currentTokenCount + 1, CURRENT_TOKEN_COUNT.get(), TOKEN_QUEUE.size());
    }

}
