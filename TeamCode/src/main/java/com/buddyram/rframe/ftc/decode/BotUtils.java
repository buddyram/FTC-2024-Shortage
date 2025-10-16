package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.RotateToAction;
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
}
