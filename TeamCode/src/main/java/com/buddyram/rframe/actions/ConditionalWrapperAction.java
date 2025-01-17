package com.buddyram.rframe.actions;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.drive.DriveCondition;
import com.buddyram.rframe.RobotException;

public class ConditionalWrapperAction<R extends Robot> extends BaseConditionalAction<R> {
    private final RobotAction<R> action;

    public ConditionalWrapperAction(RobotAction<R> action, DriveCondition<R> condition) {
        super(condition);
        this.action = action;
    }

    @Override
    public void execute(R drive) throws RobotException {
        action.run(drive);
    }
}
