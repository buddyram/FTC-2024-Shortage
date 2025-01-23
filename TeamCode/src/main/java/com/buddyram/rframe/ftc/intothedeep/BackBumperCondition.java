package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.drive.DriveCondition;

public class BackBumperCondition implements DriveCondition<ShortageBot> {

    @Override
    public boolean getState(ShortageBot drive) throws RobotException {
        return !drive.getBackBumper().getState();
    }
}
