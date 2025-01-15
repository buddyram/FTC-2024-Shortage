package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.ftc.RobotAction;
import com.qualcomm.robotcore.hardware.Servo;

public class Wrist {
    private final Servo angle;

    public Wrist(Servo angle) {
        this.angle = angle;
    }

    public void setPosition(double tgt) {
        this.angle.setPosition(tgt);
    }

    public static RobotAction moveTo(double tgt) {
        return (drive) -> {
            drive.arm.wrist.setPosition(tgt);
            return true;
        };
    }
}

