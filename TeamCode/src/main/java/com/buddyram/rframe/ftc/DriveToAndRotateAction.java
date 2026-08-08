package com.buddyram.rframe.ftc;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.HolonomicDriveInstruction;
import com.buddyram.rframe.drive.HolonomicDriveTrain;
import com.buddyram.rframe.drive.Navigatable;
import com.buddyram.rframe.drive.RotateAction;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.drive.StopDrivingAction;

public class DriveToAndRotateAction<T extends Navigatable<HolonomicDriveTrain>> implements RobotAction<T> {
    private final Vector3D target;
    private final double accuracy;
    private final CalculateDriveSpeed speed;
    private final double rotation;


    // public DriveToAndRotateAction(Vector3D target, double accuracy, int rotation) {}

    public DriveToAndRotateAction(Vector3D target, double accuracy, CalculateDriveSpeed speed, double rotation) {
        this.target = target;
        this.accuracy = accuracy;
        this.speed = speed;
        this.rotation = rotation;
    }

    @Override
    public boolean run(T drive) throws RobotException {
        Vector3D position = drive.getOdometry().get().position;
        double distance = this.target.distance(position);
        double driveAngle;
        double angleDifference = Utils.angleDifference(drive.getOdometry().get().rotation.z, this.rotation);
        RotateAction.Direction direction = angleDifference < 0 ? RotateAction.Direction.COUNTER_CLOCKWISE : RotateAction.Direction.CLOCKWISE;

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        boolean angleReached = false;
        boolean positionReached = false;
        while (!(angleReached && positionReached) && drive.isActive()) {
            if (Thread.currentThread().isInterrupted()) {
                new StopDrivingAction<T>().run(drive);
                return false;
            }
            position = drive.getOdometry().get().position;
            distance = position.distance(this.target);
            driveAngle = position.calculateRotation(this.target).z;
            double speed = distance > accuracy ? this.speed.calculate(distance) : 0;
            angleReached = Math.abs(angleDifference) <= accuracy;
            positionReached =  distance <= accuracy;
            angleDifference = Utils.angleDifference(drive.getOdometry().get().rotation.z, this.rotation);
            direction = angleDifference < 0 ? RotateAction.Direction.COUNTER_CLOCKWISE : RotateAction.Direction.CLOCKWISE;
            double rotationSpeed = (Math.abs(angleDifference) > 40 ? 1: Math.abs(angleDifference) > this.accuracy ? 0.2 : 0);
            rotationSpeed = direction == RotateAction.Direction.CLOCKWISE ? rotationSpeed : -rotationSpeed;
            drive.getDrive().drive(new HolonomicDriveInstruction(rotationSpeed, speed, driveAngle));
        }
        new StopDrivingAction<T>().run(drive);
        return true;
    }

    public interface CalculateDriveSpeed {
        double calculate(double distance);
    }
}
