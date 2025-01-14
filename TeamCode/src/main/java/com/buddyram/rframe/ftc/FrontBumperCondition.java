package com.buddyram.rframe.ftc;

import com.buddyram.rframe.DriveCondition;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public class FrontBumperCondition implements DriveCondition {

    @Override
    public boolean getState(AutonomousDrive drive) throws RobotException {
        return !drive.getFrontBumper().getState();
    }
}
