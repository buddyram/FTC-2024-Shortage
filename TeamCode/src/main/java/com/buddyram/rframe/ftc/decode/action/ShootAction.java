package com.buddyram.rframe.ftc.decode.action;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.StopDrivingAction;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.launcher.Feeder;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;

public class ShootAction implements RobotAction<DecodeBot> {
    public static final ConditionalWrapperAction<DecodeBot> WAIT_FOR_CORRECT_SPEED = new ConditionalWrapperAction<>(
            (nothing) -> true, (drive) -> drive.getLauncher().wheel.isReady()
    );
    public static final MultiAction<DecodeBot> FEED = new MultiAction<>(
            Feeder.moveTo(1),
            BotUtils.wait(700),
            Feeder.moveTo(0)
    );

    public boolean run(DecodeBot drive) throws RobotException {
        new MultiAction<>(
                new StopDrivingAction<>(),
                WAIT_FOR_CORRECT_SPEED,
                FEED
        ).run(drive);
        return true;
    }
}
