package com.buddyram.rframe.drive;

import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.RobotException;

public class StopDrivingAction<R extends Driveable<HolonomicDriveTrain>> implements RobotAction<R> {
    @Override
    public boolean run(R drive) throws RobotException {
        drive.getDrive().drive(new HolonomicDriveInstruction(0, 0, 0));
        return true;
    }
}
