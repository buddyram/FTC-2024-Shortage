package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.ftc.MultiAction;
import com.buddyram.rframe.ftc.RobotAction;

public class ArmUtils {
    public static RobotAction positionArm(double wrist, double elbow, int extension, int angle) {
        return new MultiAction(
            Wrist.moveTo(wrist),
            Elbow.moveTo(elbow),
            Extension.moveTo(extension),
            Shoulder.moveTo(angle)
        );
    }

    public static RobotAction positionArmAtRest(double wrist, double elbow, int extension) {
        return ArmUtils.positionArm(wrist, elbow, extension, 0);
    }

    public static RobotAction wait(int timeMs) {
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
