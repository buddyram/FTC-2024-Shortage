package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

import java.util.Collection;

public class MultiAction implements RobotAction {

    private final Collection<RobotAction> actions;

    public MultiAction(Collection<RobotAction> actions) {
        this.actions = actions;
    }

    @Override
    public boolean run(AutonomousDrive drive) throws RobotException {
        for (RobotAction action : this.actions) {
            action.run(drive);
        }
        return true;
    }
}
