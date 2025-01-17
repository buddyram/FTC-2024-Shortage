package com.buddyram.rframe.actions;

import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;

import java.util.concurrent.*;

public class TimeoutWrapperAction<R extends Robot> implements RobotAction<R> {
    private final RobotAction<R> action;
    private final long timeoutMs;

    public TimeoutWrapperAction(RobotAction<R> action, long timeoutMs) {
        this.action = action;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public boolean run(R drive) throws RobotException {
        // inspired by https://stackoverflow.com/questions/19456313/simple-timeout-in-java

        ExecutorService executor = Executors.newSingleThreadExecutor();

        final Future<Boolean> handler = executor.submit(() -> this.action.run(drive));

        try {
            return handler.get(this.timeoutMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | ExecutionException | InterruptedException e) {
            handler.cancel(true);
        } finally {
            executor.shutdownNow();
        }
        return false;
    }
}
