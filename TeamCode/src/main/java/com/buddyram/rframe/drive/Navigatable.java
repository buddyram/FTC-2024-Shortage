package com.buddyram.rframe.drive;

import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;

public interface Navigatable<DriveTrain> extends Driveable<DriveTrain> {
    public Odometry<Pose3D> getOdometry();
}
