package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;

public class ArmUtils {
    public static RobotAction<ShortageBot> positionArm(double wrist, double elbow, int extension, int angle) {
        return new MultiAction<ShortageBot>(
            Wrist.moveTo(wrist),
            Elbow.moveTo(elbow),
            Extension.moveTo(extension),
            Shoulder.moveTo(angle)
        );
    }

    public static RobotAction<ShortageBot> positionArmAtRest(double wrist, double elbow, int extension) {
        return ArmUtils.positionArm(wrist, elbow, extension, 0);
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
}
