package com.buddyram.rframe.ftc;

import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public interface RobotAction {
    public boolean run(AutonomousDrive drive) throws RobotException;
}
