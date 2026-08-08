package com.buddyram.rframe.ftc;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.drive.StopDrivingAction;

public class DriveToSmoothAction<T extends Navigatable<HolonomicDriveTrain>> implements RobotAction<T> {
    private final Vector3D target;
    private final double accuracy;
    private final boolean prerotation;

    public DriveToSmoothAction(Vector3D target, double accuracy) {
        this(target, accuracy, false);
    }


    public DriveToSmoothAction(Vector3D target, double accuracy, boolean prerotation) {
        this.target = target;
        this.accuracy = accuracy;
        this.prerotation = prerotation;
    }

    @Override
    public boolean run(T drive) throws RobotException {
        Vector3D position = drive.getOdometry().get().position;
        double distance = this.target.distance(position);
        double driveAngle = position.calculateRotation(this.target).z;
        if (this.prerotation) {
            new RotateToAction<T>(driveAngle, 0.5);
        }
        double startDistance = drive.getOdometry().get().position.distance(this.target);
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        while (distance > accuracy) {
            position = drive.getOdometry().get().position;
            distance = position.distance(this.target);
            double fracToGoal = 1 - distance / startDistance;
            driveAngle = position.calculateRotation(this.target).z;
            double speed = Math.min((Math.sin(Math.PI * fracToGoal + 0.5) * 0.7 + 0.53), 1);
            drive.getDrive().drive(new HolonomicDriveInstruction(0, speed, driveAngle));
        }
        new StopDrivingAction<T>().run(drive);
        return true;
    }

}
