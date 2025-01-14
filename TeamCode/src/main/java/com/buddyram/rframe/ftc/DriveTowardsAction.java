package com.buddyram.rframe.ftc;

import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public class DriveTowardsAction implements RobotAction {
    private final Vector3D target;
    private final boolean relative;

    public DriveTowardsAction(Vector3D target, boolean relative) {
        this.target = target;
        this.relative = relative;
    }


    @Override
    public boolean run(AutonomousDrive drive) throws RobotException {
        drive.getDrive().drive(this.relative ? drive.calculateRelativeDriveInstruction(this.target) : drive.calculateDriveInstruction(this.target));
        return true;
    }
}
