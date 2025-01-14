package com.buddyram.rframe.ftc;

import com.buddyram.rframe.DriveCondition;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public abstract class BaseConditionalAction implements RobotAction {
    protected DriveCondition condition;

    public BaseConditionalAction(DriveCondition condition) {
        this.condition = condition;
    }

    @Override
    public boolean run(AutonomousDrive drive) throws RobotException {
        this.execute(drive);
        while (!this.isComplete(drive)) {
            this.waitingForCompletion(drive);
        }
        return true;
    }

    public void waitingForCompletion(AutonomousDrive drive) throws RobotException {
        Thread.yield();
    }

    public abstract void execute(AutonomousDrive drive) throws RobotException;

    public boolean isComplete(AutonomousDrive drive) throws RobotException {
        return condition.getState(drive);
    }
}
