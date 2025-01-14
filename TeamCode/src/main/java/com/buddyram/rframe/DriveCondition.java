package com.buddyram.rframe;

import com.buddyram.rframe.ftc.RobotException;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public interface DriveCondition {
    public boolean getState(AutonomousDrive drive) throws RobotException;
}
