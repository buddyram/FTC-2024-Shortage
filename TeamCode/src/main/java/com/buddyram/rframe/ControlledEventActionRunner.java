package com.buddyram.rframe;

import com.buddyram.rframe.actions.RobotAction;

public class ControlledEventActionRunner {
    private final RunCheck runCheck;
    private final RobotAction<Robot> action;
    private final Robot drive;
    private final Thread thread;

    public ControlledEventActionRunner(RobotAction<Robot> action, RunCheck runCheck, Robot drive) {
        this.runCheck = runCheck;
        this.drive = drive;
        this.action = action;
        this.thread = new Thread(() -> {
            try {
                this.action.run(this.drive);
            } catch (RobotException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void start() {
        this.thread.start();
    }

    public void interrupt() {
        this.thread.interrupt();
    }

    public interface RunCheck {
        boolean get();
    }
}
