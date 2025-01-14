package com.buddyram.rframe.ftc;
import com.buddyram.rframe.DriveCondition;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public class ConditionalWrapperAction extends BaseConditionalAction {
    private final RobotAction action;

    public ConditionalWrapperAction(RobotAction action, DriveCondition condition) {
        super(condition);
        this.action = action;
    }

    @Override
    public void execute(AutonomousDrive drive) throws RobotException {
        action.run(drive);
    }
}
