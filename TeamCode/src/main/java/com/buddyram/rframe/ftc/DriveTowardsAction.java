package com.buddyram.rframe.ftc;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.ftc.intothedeep.actions.ShortageAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;

public class DriveTowardsAction implements ShortageAction {
    private final Vector3D target;
    private final boolean relative;
    private final double speed;

    public DriveTowardsAction(Vector3D target, boolean relative) {
        this(target, relative, 1);
    }

    public DriveTowardsAction(Vector3D target, boolean relative, double speed) {
        this.target = target;
        this.relative = relative;
        this.speed = speed;
    }


    @Override
    public boolean run(ShortageBot drive) throws RobotException {
        drive.getDrive().drive(this.relative ? drive.calculateRelativeDriveInstruction(this.target, speed) : drive.calculateDriveInstruction(this.target, speed));
        return true;
    }

    public static final Vector3D FORWARD_DIRECTION = new Vector3D(0, Double.POSITIVE_INFINITY, 0);
    public static final Vector3D BACKWARD_DIRECTION = new Vector3D(0, Double.NEGATIVE_INFINITY, 0);
    public static final Vector3D LEFT_DIRECTION = new Vector3D(Double.NEGATIVE_INFINITY, 0, 0);
    public static final Vector3D RIGHT_DIRECTION = new Vector3D(Double.POSITIVE_INFINITY, 0, 0);

}
