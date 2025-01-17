package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.drive.DriveCondition;
import com.buddyram.rframe.RobotException;

public class FrontBumperCondition implements DriveCondition<ShortageBot> {

    @Override
    public boolean getState(ShortageBot drive) throws RobotException {
        return !drive.getFrontBumper().getState();
    }
}
