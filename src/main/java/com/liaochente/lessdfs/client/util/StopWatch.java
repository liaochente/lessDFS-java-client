package com.liaochente.lessdfs.client.util;

import java.time.Instant;
import java.util.*;

public class StopWatch {

    public final static List<StopWatch> STOP_WATCHES = Collections.synchronizedList(new ArrayList<>());

    String threadName;

    List<Map<String, Object>> tasks = new ArrayList<>();

    Map<String, Object> taskMap;

    public StopWatch() {
        this.threadName = Thread.currentThread().getName();
        STOP_WATCHES.add(this);
    }

    public void start(String taskName) {
        taskMap = new HashMap<>();
        taskMap.put("taskName", taskName);
        taskMap.put("start", Instant.now());
    }

    public void stop() {
        taskMap.put("end", Instant.now());
        tasks.add(taskMap);
    }

    public List<Map<String, Object>> getTasks() {
        return tasks;
    }

    public String getThreadName() {
        return threadName;
    }
}
