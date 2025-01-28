package com.buddyram.rframe.drive;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.actions.RobotAction;

public class RotateToAction<T extends Navigatable<HolonomicDriveTrain>> implements RobotAction<T> {
    private final double targetAngle;
    private final double accuracy;
    private final CalculateRotationSpeed speed;

    public RotateToAction(double targetAngle, double accuracy) {
        this(targetAngle, accuracy, angleDifference -> Math.abs(angleDifference) > 60 ? 1 : Math.abs(angleDifference) > 5 ? 0.3 : 0.15);
    }

    public RotateToAction(double targetAngle, double accuracy, CalculateRotationSpeed speed) {
        this.targetAngle = targetAngle;
        this.accuracy = accuracy;
        this.speed = speed;
    }

    @Override
    public boolean run(T drive) throws RobotException {
        double angleDifference = Utils.angleDifference(drive.getOdometry().get().rotation.z, this.targetAngle);
        RotateAction.Direction direction = angleDifference < 0 ? RotateAction.Direction.COUNTER_CLOCKWISE : RotateAction.Direction.CLOCKWISE;
        while (Math.abs(angleDifference) > this.accuracy) {
            new RotateAction(direction, this.speed.calculate(angleDifference)).run(drive);
            Thread.yield();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            angleDifference = Utils.angleDifference(drive.getOdometry().get().rotation.z, this.targetAngle);
            direction = angleDifference < 0 ? RotateAction.Direction.COUNTER_CLOCKWISE : RotateAction.Direction.CLOCKWISE;
        }
        new StopDrivingAction<T>().run(drive);
        return true;
    }

    public interface CalculateRotationSpeed {
        double calculate(double angleDifference);
    }
}
