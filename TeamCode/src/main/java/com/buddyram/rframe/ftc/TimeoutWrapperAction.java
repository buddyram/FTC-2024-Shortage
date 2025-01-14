package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;
import java.util.concurrent.*;

public class TimeoutWrapperAction implements RobotAction {
    private final RobotAction action;
    private final long timeoutMs;

    public TimeoutWrapperAction(RobotAction action, long timeoutMs) {
        this.action = action;
        this.timeoutMs = timeoutMs;
    }

    @Override
    public boolean run(AutonomousDrive drive) throws RobotException {
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
