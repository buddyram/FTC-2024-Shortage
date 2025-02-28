package com.buddyram.rframe.ftc;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.drive.StopDrivingAction;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;

public class DriveToAction<T extends Navigatable<HolonomicDriveTrain>> implements RobotAction<T> {
    private final Vector3D target;
    private final double accuracy;
    private final CalculateDriveSpeed speed;
    private final boolean prerotation;

    public DriveToAction(Vector3D target, double accuracy) {
        this(target, accuracy, false);
    }

    public DriveToAction(Vector3D target, double accuracy, boolean prerotation) {
        this(target, accuracy, distance -> Math.abs(distance) > 20 ? 1 : Math.abs(distance) > 5 ? 0.6 : 0.3, prerotation);
    }

    public DriveToAction(Vector3D target, double accuracy, CalculateDriveSpeed speed, boolean prerotation) {
        this.target = target;
        this.accuracy = accuracy;
        this.speed = speed;
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

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        while (distance > accuracy) {
            position = drive.getOdometry().get().position;
            distance = position.distance(this.target);
            driveAngle = position.calculateRotation(this.target).z;
            double speed = this.speed.calculate(distance);
            drive.getDrive().drive(new HolonomicDriveInstruction(0, speed, driveAngle));
        }
        new StopDrivingAction<T>().run(drive);
        return true;
    }

    public interface CalculateDriveSpeed {
        double calculate(double distance);
    }
}
