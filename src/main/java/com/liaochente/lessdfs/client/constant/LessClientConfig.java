package com.liaochente.lessdfs.client.constant;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.stream.Collectors;

public class LessClientConfig {

    public final static int MAGIC_CODE = 0x294;

    @LessValue("less.client.password")
    public static String password = "123456";

    @LessValue("less.client.server.address")
    public static String serverAddress = "127.0.0.1";

    @LessValue("less.client.server.port")
    public static int port = 8888;

    @LessValue("less.client.max_frame_length")
    public static int maxFrameLength = 102400;

    public final static ScheduledThreadPoolExecutor GLOBAL_SCHEDULED_THREAD_POOL = new ScheduledThreadPoolExecutor(8);

    private LessClientConfig() {

    }

    /**
     * 初始化配置文件
     *
     * @throws IOException
     */
    public final static void init() {
        Map<String, String> configMap = loadConfig();
        autowiredConfig(configMap);
    }

    /**
     * 加载配置文件，解析成Key-Value形式返回
     *
     * @return
     * @throws IOException
     */
    private final static Map<String, String> loadConfig() {
        Path path = Paths.get("less-client.conf");
        if (!path.toFile().exists()) {
            throw new RuntimeException("Failed to load configuration file.");
        }
        List<String> configLines = null;
        try {
            configLines = Files.readAllLines(path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Map<String, String> configMap = configLines.stream()
                .filter((configLine) -> configLine.trim().indexOf("#") == -1)
                .map((configLine) -> configLine.split("="))
                .filter((kvs) -> kvs.length > 1)
                .collect(Collectors.toMap((kv) -> kv[0], kv -> kv[1]));
        return configMap;
    }

    /**
     * 自动填充配置属性
     *
     * @param configMap
     * @throws IllegalAccessException
     */
    private final static void autowiredConfig(Map<String, String> configMap) {
        Field[] fields = LessClientConfig.class.getDeclaredFields();
        if (fields != null) {
            for (Field field : fields) {
                LessValue lessValue = field.getAnnotation(LessValue.class);
                if (lessValue != null) {
                    String configValue = configMap.get(lessValue.value());
                    if (configValue != null && !"".equals(configValue)) {
                        field.setAccessible(true);
                        try {
                            if ("int".equals(field.getType().getName())) {
                                field.set(null, Integer.parseInt(configValue));
                            } else {
                                field.set(null, configValue);
                            }
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
    }


}
