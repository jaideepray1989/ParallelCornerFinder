package utils;

import java.util.concurrent.Callable;

/**
 * Created by jaideepray on 12/6/14.
 */
public interface ITask<T> extends Callable<T> {
    public T runTask();
}
