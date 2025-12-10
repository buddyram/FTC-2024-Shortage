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

public class ShootAction implements RobotAction<DecodeBot> {
    private final int postFeedWaitTime;

    public ShootAction() {
        this(500);
    }
    public ShootAction(int postFeedWaitTimeMS) {
        this.postFeedWaitTime = postFeedWaitTimeMS;
    }

    public static final ConditionalWrapperAction<DecodeBot> WAIT_FOR_CORRECT_SPEED = new ConditionalWrapperAction<>(
            (nothing) -> true, (drive) -> drive.indexer.isReady() && drive.getLauncher().wheel.isReady()
    );

    public static final RobotAction<DecodeBot> PREPARE_SPINDEXER = drive1 -> {
        try {
            drive1.indexer.setCurrentMode(Indexer.Mode.OUTTAKING);
            drive1.indexer.goToSlot(drive1.indexer.getNearestFull());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    };
    public static final MultiAction<DecodeBot> FEED = new MultiAction<>(
            Feeder.moveTo(Feeder.OPEN),
            BotUtils.wait(700),
            Feeder.moveTo(Feeder.CLOSE)
    );

    public boolean run(DecodeBot drive) throws RobotException {
        new MultiAction<>(
                new StopDrivingAction<>(),
                PREPARE_SPINDEXER,
                BotUtils.wait(500),
                WAIT_FOR_CORRECT_SPEED,
                FEED,
                BotUtils.wait(postFeedWaitTime)
        ).run(drive);
        drive.indexer.emptySlot();
        return true;
    }
}
