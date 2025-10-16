package com.buddyram.rframe.ftc.decode.intake;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.decode.launcher.Feeder;
import com.buddyram.rframe.ftc.decode.launcher.Flywheel;
import com.buddyram.rframe.ftc.decode.launcher.Sweeper;

public class Intake extends BaseComponent<Robot> {
    public final Sweeper sweeper;

    public Intake(Robot robot, Sweeper sweeper) {
        super(robot);
        this.sweeper = sweeper;
    }
}
