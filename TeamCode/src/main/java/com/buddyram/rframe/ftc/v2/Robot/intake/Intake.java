package com.buddyram.rframe.ftc.v2.Robot.intake;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;

public class Intake extends BaseComponent<Robot> {
    public Modes getMode() {
        return mode;
    }

    public enum Modes {
        INTAKING,
        IDLE,
        OUTTAKING
    }
    private Modes mode;
    public final Sweeper sweeper;

    public Intake(Robot robot, Sweeper sweeper) {
        super(robot);
        this.sweeper = sweeper;
    }

    public void enableMode(Modes mode) {
        if (mode == Modes.IDLE) {
            sweeper.idle();
        } else if (mode == Modes.OUTTAKING) {
            sweeper.outtaking();
        } else if (mode == Modes.INTAKING) {
            sweeper.intaking();
        }
        this.mode = mode;
    }
}
