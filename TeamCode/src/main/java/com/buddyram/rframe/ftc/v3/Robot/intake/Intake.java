package com.buddyram.rframe.ftc.v3.Robot.intake;

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
    public final IntakeServoLift intakeServoLift;
    public Intake(Robot robot, Sweeper sweeper, IntakeServoLift intakeServoLift) {
        super(robot);
        this.intakeServoLift = intakeServoLift;
        this.sweeper = sweeper;
    }
    public enum Position {
        UP,
        MIDDLE,
        DOWN
    }
    public void enableHeight(Position mode) {
        if (mode == Position.UP) {
            intakeServoLift.up();
        } else if (mode == Position.MIDDLE) {
            intakeServoLift.middle();
        } else if (mode == Position.DOWN){
            intakeServoLift.down();
        }
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
