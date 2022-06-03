/**
 * created by tsw on 2021.3.14
 * github: https://github.com/2235854410
 */
package com.tsw.insectdetector.tool;

import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolUtil {
    private static ThreadPoolUtil instance;
    private final long keepAliveTime = 60L;
    private final int corePoolSize = 3;
    private final int maxPoolSize = 3;

    private ThreadPoolExecutor executor;


    private ThreadPoolUtil() {

    }

    public static ThreadPoolUtil getInstance() {
        if(null == instance) {
            synchronized (ThreadPoolUtil.class) {
                instance = new ThreadPoolUtil();
            }
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        if(null == executor) {
            executor = new ThreadPoolExecutor(corePoolSize,
                    maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
                    Executors.defaultThreadFactory(), new ThreadPoolExecutor.AbortPolicy());
        }
        executor.execute(runnable);
    }

    public void cancel(Runnable runnable) {
        if(null != executor) {
            executor.getQueue().remove(runnable);
        }
    }

}
