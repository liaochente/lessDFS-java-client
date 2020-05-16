package com.liaochente.lessdfs.client.constant;

import com.liaochente.lessdfs.client.ClientBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class LessClientConfig {

    public final static int MAGIC_CODE = 0x294;

    public final static String PASSWORD = "123456";

    public final static ScheduledThreadPoolExecutor GLOBAL_SCHEDULED_THREAD_POOL = new ScheduledThreadPoolExecutor(8);

    private LessClientConfig() {

    }

    public final static void init() {

    }

}
