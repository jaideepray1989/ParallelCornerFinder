package utils;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by jaideepray on 12/6/14.
 */
public class TaskRunner<T> {
    private ExecutorService pool;

    public TaskRunner(int numFixedThreads) {
        pool = Executors.newFixedThreadPool(numFixedThreads);
    }

    public List<Future<T>> invokeAll(List<ITask<T>> taskList) throws Exception {
        return pool.invokeAll(taskList);
    }
}
