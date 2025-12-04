package com.buddyram.rframe.ftc.decode.action;

import com.buddyram.rframe.RobotException;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.MultiAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.drive.StopDrivingAction;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.BotUtils;
import com.buddyram.rframe.ftc.decode.indexer.Indexer;
import com.buddyram.rframe.ftc.decode.launcher.Feeder;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;

public class ShootAction implements RobotAction<DecodeBot> {
    public static final ConditionalWrapperAction<DecodeBot> WAIT_FOR_CORRECT_SPEED = new ConditionalWrapperAction<>(
            (nothing) -> true, (drive) -> drive.indexer.isReady() && drive.getLauncher().wheel.isReady()
    );

    public static final RobotAction<DecodeBot> spindexer = drive1 -> {
        try {
            drive1.indexer.setCurrentMode(Indexer.Mode.OUTTAKING);
            drive1.indexer.goToSlot(drive1.indexer.getNearestFull());
            new ConditionalWrapperAction<>(
                    (m) -> true,
                    (drive2) -> drive1.indexer.isReady()
            ).run(drive1);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    };
    public static final MultiAction<DecodeBot> FEED = new MultiAction<>(
            Feeder.moveTo(Feeder.OPEN),
            BotUtils.wait(700),
            Feeder.moveTo(Feeder.CLOSE),
            BotUtils.wait(1000)
    );
    public static final RobotAction<DecodeBot> spindexer2 = (drive) -> {
        try {
            drive.indexer.goToSlot(drive.indexer.getNearestFull());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    };

    public boolean run(DecodeBot drive) throws RobotException {
        new MultiAction<>(
                new StopDrivingAction<>(),
                spindexer,
                BotUtils.wait(500),
                WAIT_FOR_CORRECT_SPEED,
                FEED
        ).run(drive);
        drive.indexer.emptySlot();
        return true;
    }
}
