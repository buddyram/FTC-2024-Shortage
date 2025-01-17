package com.buddyram.rframe.actions;

import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;

public interface RobotAction<R extends Robot> {
    public boolean run(R drive) throws RobotException;
}
