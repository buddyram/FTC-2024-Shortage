package com.buddyram.rframe.ftc;

import com.buddyram.rframe.HolonomicDriveInstruction;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.intothedeep.AutonomousDrive;

public class StopDrivingAction implements RobotAction {
    @Override
    public boolean run(AutonomousDrive drive) throws RobotException {
        drive.getDrive().drive(new HolonomicDriveInstruction(0, 0, 0));
        return true;
    }
}
