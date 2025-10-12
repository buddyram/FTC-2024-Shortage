package com.buddyram.rosebot;

import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Odometry;
import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.Driveable;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.KiwiDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rosebot.head.Head;

public class Rosebot implements Navigatable<HolonomicDriveTrain> {
    private KiwiDriveTrain drive;
    private Logger logger;
    private Head head;
    private Odometry<Pose3D> odometry;



    @Override
    public KiwiDriveTrain getDrive() {
        return this.drive;
    }

    @Override
    public Logger getLogger() {
        return this.logger;
    }

    @Override
    public boolean isActive() {
        return true;
    }

    public Head getHead() {
        return head;
    }

    public void handleAction(RobotAction<Robot> action) throws RobotException {
        action.run(this);
    }

    public Rosebot() {
        this(null, null, null, null);
    }

    public Rosebot(KiwiDriveTrain drive, Head head, Logger logger, Odometry<Pose3D> odometry) {
        this.init(drive, head, logger, odometry);
    }

    public void init(KiwiDriveTrain drive, Head head, Logger logger, Odometry<Pose3D> odometry) {
        this.odometry = odometry;
        this.drive = drive;
        this.head = head;
        this.logger = logger;
    }

    @Override
    public Odometry<Pose3D> getOdometry() {
        return this.odometry;
    }
}
