package utils;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.concurrent.Executors;

/**
 * Created by jaideepray on 12/6/14.
 */
public class TaskRunner<T> {
    private ListeningExecutorService pool;

    public TaskRunner() {
        pool = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
    }

    public ListenableFuture<T> runTask(ITask<T> task) {
        return pool.submit(task);
    }
}
