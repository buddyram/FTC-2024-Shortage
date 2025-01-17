package com.buddyram.rframe.drive;

import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;

public interface DriveCondition<R extends Robot> {
    public boolean getState(R drive) throws RobotException;
}
