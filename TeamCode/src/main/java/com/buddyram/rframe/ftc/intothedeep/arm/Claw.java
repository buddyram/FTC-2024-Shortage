package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.ftc.RobotAction;
import com.qualcomm.robotcore.hardware.Servo;

public class Claw {
    private final Servo angle;
    public static final double OPEN = 1;
    public static final double CLOSE = 0;

    public Claw(Servo angle) {
        this.angle = angle;
    }

    public void setPosition(double tgt) {
        this.angle.setPosition(tgt);
    }

    public static RobotAction moveTo(double tgt) {
        return (drive) -> {
            drive.arm.claw.setPosition(tgt);
            return true;
        };
    }
}

