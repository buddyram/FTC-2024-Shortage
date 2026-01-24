package com.buddyram.rframe.ftc.v2.Robot.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Turret extends BaseComponent<Robot> {
    public static final int MIN_ANGLE = -180;
    public static final int MAX_ANGLE = 180;
    private final DcMotor motor;

    public Turret(Robot robot, DcMotor motor) {
        super(robot);
        this.motor = motor;
    }

    private void setPosition(double angle) {
        motor.setTargetPosition((int) Math.floor(angle / 360.0 * 28 * 363 / 20));
    }

    public boolean isReady() {
        return Math.abs(motor.getTargetPosition() - motor.getCurrentPosition()) < 3;
    }

    public void setAngle (double newAngle) {
        if (newAngle > MIN_ANGLE && newAngle < MAX_ANGLE) {
            setPosition(newAngle);
        }
    }
}
