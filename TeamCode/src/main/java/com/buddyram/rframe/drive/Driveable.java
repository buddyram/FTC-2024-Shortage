package com.buddyram.rframe.drive;

import com.buddyram.rframe.Robot;

public interface Driveable<DriveTrain> extends Robot {
    public DriveTrain getDrive();
}
