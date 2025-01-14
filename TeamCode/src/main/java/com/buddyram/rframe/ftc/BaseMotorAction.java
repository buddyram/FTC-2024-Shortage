package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public abstract class BaseMotorAction implements RobotAction {
    @Override
    public boolean run(AutonomousDrive drive) {
        return false;
    }

    public boolean isComplete() {
        return false;
    }

}
