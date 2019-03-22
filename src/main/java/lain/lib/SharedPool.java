package lain.lib;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;

public final class SharedPool
{

    private static final ExecutorService thePool = Executors.newWorkStealingPool(ForkJoinPool.getCommonPoolParallelism() + 1);

    public static void execute(Runnable command)
    {
        thePool.execute(command);
    }

    private SharedPool()
    {
    }

}
