package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.ftc.DriveToAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;

public class BotUtils {
    public static RobotAction<DecodeBot> wait(int timeMs) {
        return (drive) -> {
            try {
                Thread.sleep(timeMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        };
    }
    public static RobotAction<DecodeBot> rotateTo(double targetAngle) {
        return new RotateToAction<>(targetAngle, 0.5);
    }

    public static RobotAction<DecodeBot> driveTowardsUntil(int x, int y, PositionalCondition condition) {
        return BotUtils.driveTowardsUntil(x, y, condition, 1);
    }

    public interface PositionalCondition {
        boolean isComplete(Vector3D position);
    }

    public static RobotAction<DecodeBot> driveTo(Vector3D target, boolean prerotation) {
        return new DriveToAction<>(target, 0.5, prerotation);
    }

    public static RobotAction<DecodeBot> driveTowardsUntil(int x, int y, PositionalCondition condition, double speed) {
        return new ConditionalWrapperAction<>(
                new DriveTowardsAction(new Vector3D(x, y, 0), false, speed),
                (drive) -> condition.isComplete(drive.getOdometry().get().position)
        );
    }
}
