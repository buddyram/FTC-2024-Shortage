package com.buddyram.rframe.ftc.v2.Robot.intake;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Sweeper extends BaseComponent<Robot> {
    private final DcMotor motor;
    public static final double INTAKING = 1;
    public static final double OUTTAKING = 0;

    public Sweeper(Robot robot, DcMotor motor) {
        super(robot);
        this.motor = motor;
    }

    public void setPower(double newPower) {
        this.motor.setPower(newPower);
    }

    public void idle() {
        motor.setPower(0);
    }

    public void intaking() {
        motor.setPower(0.8);
    }

    public void outtaking() {
        motor.setPower(-0.1);
    }
}