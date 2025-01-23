package com.buddyram.rframe.drive;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.RobotAction;

public class RotateAction implements RobotAction<Driveable<HolonomicDriveTrain>> {

    public enum Direction {
        CLOCKWISE,
        COUNTER_CLOCKWISE
    }

    private final Direction direction;
    private final double speed;

    public RotateAction(Direction direction) {
        this(direction, 1);
    }

    public RotateAction(Direction direction, double speed) {
        this.direction = direction;
        this.speed = speed;
    }


    @Override
    public boolean run(Driveable<HolonomicDriveTrain> drive) throws RobotException {
        drive.getDrive().drive(new HolonomicDriveInstruction(this.direction == Direction.CLOCKWISE ? this.speed : -this.speed, 0, 0));
        return true;
    }
}
