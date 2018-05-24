package com.odysseusinc.arachne.portal.service.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author SMaletsky
 */
public class ArachneBackgroundExecutor {
    private static ExecutorService instance;

    public ArachneBackgroundExecutor() {

    }

    public static synchronized ExecutorService getExecutor() {
        if (instance == null) {
            instance = Executors.newCachedThreadPool(runnable -> {
                Thread thread = new Thread(runnable);
                thread.setPriority(Thread.MIN_PRIORITY);
                return thread;
            });
            ((ThreadPoolExecutor) instance).setKeepAliveTime(1, TimeUnit.SECONDS);
        }

        return instance;
    }

    private static synchronized void shutdown() {

        if (instance != null) {
            instance.shutdown();
            instance = null;
        }
    }
}
