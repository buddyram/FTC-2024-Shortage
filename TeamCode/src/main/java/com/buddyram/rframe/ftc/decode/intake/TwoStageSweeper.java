package com.buddyram.rframe.ftc.decode.intake;

import com.buddyram.rframe.Robot;
import com.qualcomm.robotcore.hardware.CRServo;

public class TwoStageSweeper extends Sweeper {
    private final CRServo stage2Servo;
    public TwoStageSweeper(Robot robot, CRServo stage1Servo, CRServo stage2Servo) {
        super(robot, stage1Servo);
        this.stage2Servo = stage2Servo;
    }

    @Override
    public void setPower(double newPower) {
        super.setPower(newPower);
        stage2Servo.setPower(newPower);
    }
    public void idle() {
        super.idle();
        stage2Servo.setPower(0.5);
    }

    public void intaking() {
        super.intaking();
        stage2Servo.setPower(1);
    }

    public void waiting() {
        super.waiting();
        stage2Servo.setPower(-0.1);
    }
}
