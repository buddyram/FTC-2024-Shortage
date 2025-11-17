package com.buddyram.rframe.ftc.decode.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Turret extends BaseComponent<DecodeBot> {
    public static final int MIN_ANGLE = -60;
    public static final int MAX_ANGLE = 65;
    private final DcMotor motor;

    public Turret(DecodeBot robot, DcMotor motor) {
        super(robot);
        this.motor = motor;
    }

    private void setPosition(double angle) {
        motor.setTargetPosition((int) Math.floor(angle/((21 / 56.0 * 360)/(-459.0))));
    }

    public void setAngle (double newAngle) {
        if (newAngle > MIN_ANGLE && newAngle < MAX_ANGLE) {
            setPosition(newAngle);
        }
    }
}
