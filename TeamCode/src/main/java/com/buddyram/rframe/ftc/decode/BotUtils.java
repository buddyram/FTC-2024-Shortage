package com.buddyram.rframe.ftc.decode;

import com.buddyram.rframe.actions.RobotAction;

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
}
