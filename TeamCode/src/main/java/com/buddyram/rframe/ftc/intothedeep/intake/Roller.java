package com.buddyram.rframe.ftc.intothedeep.intake;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;

public class Roller extends BaseComponent<Robot> {
    private final CRServo servo;

    public Roller(Robot robot, CRServo servo) {
        super(robot);
        this.servo = servo;
    }

    public void setPosition(double tgt) {
        if (tgt > 1 || tgt < -1) {
            return;
        }
        this.servo.setPower(tgt);
    }

    public double getPosition() {
        return this.servo.getPower();
    }

    public static RobotAction<ShortageBot> moveTo(double tgt) {
        return (drive) -> {
            drive.getIntake().roller.setPosition(tgt);
            return true;
        };
    }
}
