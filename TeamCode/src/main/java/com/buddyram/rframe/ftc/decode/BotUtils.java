package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.Pose3D;
import com.buddyram.rframe.Utils;
import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.ftc.DriveToAction;
import com.buddyram.rframe.ftc.DriveToAndRotateAction;
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

//    public static RobotAction<DecodeBot> DriveAndRotateTo(Vector3D target, double targetAngle) {
//        return new DriveToAndRotateAction<>(target, 2, targetAngle);
//    }

    public static RobotAction<DecodeBot> driveTowardsUntil(int x, int y, PositionalCondition condition) {
        return BotUtils.driveTowardsUntil(x, y, condition, 1);
    }

    public interface PositionalCondition {
        boolean isComplete(Vector3D position);
    }

    public static RobotAction<DecodeBot> driveTo(Vector3D target, boolean prerotation) {
        return new DriveToAction<>(target, 0.5, (dist) -> dist > 20 ? 1 : 0.3, prerotation);
    }

    public static RobotAction<DecodeBot> driveToSlow(Vector3D target, boolean prerotation) {
        return new DriveToAction<>(target, 0.5, (dist) -> 0.25,  prerotation);
    }

    public static RobotAction<DecodeBot> driveTowardsUntil(int x, int y, PositionalCondition condition, double speed) {
        return new ConditionalWrapperAction<>(
                new DriveTowardsAction(new Vector3D(x, y, 0), false, speed),
                (drive) -> condition.isComplete(drive.getOdometry().get().position)
        );
    }

    public static Vector3D mirrorIfRed(Vector3D pos, boolean isRed) {
        if (isRed) {
            return new Vector3D(144 - pos.x, pos.y, pos.z);
        } else {
            return pos;
        }
    }
    public static double mirrorIfRed(double angle, boolean isRed) {
        if (isRed) {
            return Utils.normalizeAngle(0 - angle);
        } else {
            return angle;
        }
    }
    public static Pose3D mirrorIfRed(Pose3D pose, boolean isRed) {
        if (isRed) {
            return new Pose3D(
                    BotUtils.mirrorIfRed(pose.position, isRed),
                    new Vector3D(pose.rotation.x, pose.rotation.y, BotUtils.mirrorIfRed(pose.rotation.z, isRed)),
                    pose.positionVelocity,
                    pose.rotationVelocity
            );
        } else {
            return pose;
        }
    }
}
