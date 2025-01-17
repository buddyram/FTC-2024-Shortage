package com.buddyram.rframe.actions;

import com.buddyram.rframe.Robot;
import com.buddyram.rframe.drive.DriveCondition;
import com.buddyram.rframe.RobotException;

public abstract class BaseConditionalAction<R extends Robot> implements RobotAction<R> {
    protected DriveCondition<R> condition;

    public BaseConditionalAction(DriveCondition<R> condition) {
        this.condition = condition;
    }

    @Override
    public boolean run(R drive) throws RobotException {
        this.execute(drive);
        while (!this.isComplete(drive)) {
            this.waitingForCompletion(drive);
        }
        return true;
    }

    public void waitingForCompletion(R drive) throws RobotException {
        Thread.yield();
    }

    public abstract void execute(R drive) throws RobotException;

    public boolean isComplete(R drive) throws RobotException {
        return condition.getState(drive);
    }
}
