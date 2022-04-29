package lain.lib;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class SharedPool {

    private static final Executor thePool = Executors.newWorkStealingPool(ForkJoinPool.getCommonPoolParallelism());

    private SharedPool() {
        throw new Error("NoInstance");
    }

    public static void execute(Runnable command) {
        thePool.execute(command);
    }

}
