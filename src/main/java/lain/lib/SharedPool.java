package lain.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public final class SharedPool {

    private static final ExecutorService thePool = new ThreadPoolExecutor(
            2,
            Math.min(Runtime.getRuntime().availableProcessors() * 4, Short.MAX_VALUE),
            60L,
            TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            SharedPool::newWorker);

    private SharedPool() {
        throw new Error("NoInstance");
    }

    private static Thread newWorker(Runnable target) {
        Thread thread = new Thread(target, "SharedPoolWorker");
        if (!thread.isDaemon())
            thread.setDaemon(true);
        if (thread.getPriority() != Thread.NORM_PRIORITY)
            thread.setPriority(Thread.NORM_PRIORITY);
        return thread;
    }

    public static void execute(Runnable command) {
        thePool.execute(command);
    }

}
