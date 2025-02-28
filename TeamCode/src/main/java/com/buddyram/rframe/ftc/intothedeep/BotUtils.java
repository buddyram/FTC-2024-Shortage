package com.buddyram.rframe.ftc.intothedeep;

import com.buddyram.rframe.Vector3D;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.RotateToAction;
import com.buddyram.rframe.ftc.DriveToAction;
import com.buddyram.rframe.ftc.DriveTowardsAction;
import com.buddyram.rframe.ftc.intothedeep.arm.Elbow;
import com.buddyram.rframe.ftc.intothedeep.arm.Extension;
import com.buddyram.rframe.ftc.intothedeep.arm.Shoulder;
import com.buddyram.rframe.ftc.intothedeep.arm.Wrist;
import com.buddyram.rframe.ftc.intothedeep.intake.Roller;
import com.buddyram.rframe.ftc.intothedeep.intake.VirtualFourBar;

public class BotUtils {

    public static RobotAction<ShortageBot> positionArm(double wrist, double elbow, int extension, int angle) {
        return new MultiAction<>(
                Wrist.moveTo(wrist),
                Elbow.moveTo(elbow),
                Extension.moveTo(extension),
                Shoulder.moveTo(angle)
        );
    }

    public static RobotAction<ShortageBot> positionIntake(double virtualFourBar, double rollerSpeed, int extension) {
        return new MultiAction<>(
                VirtualFourBar.moveTo(virtualFourBar),
                Roller.moveTo(rollerSpeed),
                com.buddyram.rframe.ftc.intothedeep.intake.Extension.moveTo(extension)
        );
    }

    public static RobotAction<ShortageBot> positionArmAtRest(double wrist, double elbow, int extension) {
        return BotUtils.positionArm(wrist, elbow, extension, 0);
    }

    public static RobotAction<ShortageBot> wait(int timeMs) {
        return (drive) -> {
            try {
                Thread.sleep(timeMs);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        };
    }

    public static RobotAction<ShortageBot> driveTowardsUntil(int x, int y, PositionalCondition condition) {
        return BotUtils.driveTowardsUntil(x, y, condition, 1);
    }

    public static RobotAction<ShortageBot> driveTowardsUntil(int x, int y, PositionalCondition condition, double speed) {
        return new ConditionalWrapperAction<>(
            new DriveTowardsAction(new Vector3D(x, y, 0), false, speed),
            (drive) -> condition.isComplete(drive.getOdometry().get().position)
        );
    }

    public interface PositionalCondition {
        boolean isComplete(Vector3D position);
    }

    public static RobotAction<ShortageBot> rotateTo(double targetAngle) {
        return new RotateToAction<>(targetAngle, 0.5);
    }

    public static RobotAction<ShortageBot> driveTo(Vector3D target, boolean prerotation) {
        return new DriveToAction<>(target, 0.5, prerotation);
    }

    public static RobotAction<ShortageBot> driveTo(Vector3D target) {
        return new DriveToAction<>(target, 0.5);
    }
}
