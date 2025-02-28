package com.buddyram.rosebot;

import com.buddyram.rframe.Logger;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.Driveable;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.KiwiDriveTrain;
import com.buddyram.rosebot.head.Head;

public class Rosebot implements Driveable<HolonomicDriveTrain> {
    private KiwiDriveTrain drive;
    private Logger logger;
    private Head head;




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
        this(null, null, null);
    }

    public Rosebot(KiwiDriveTrain drive, Head head, Logger logger) {
        this.init(drive, head, logger);
    }

    public void init(KiwiDriveTrain drive, Head head, Logger logger) {
        this.drive = drive;
        this.head = head;
        this.logger = logger;
    }
}
