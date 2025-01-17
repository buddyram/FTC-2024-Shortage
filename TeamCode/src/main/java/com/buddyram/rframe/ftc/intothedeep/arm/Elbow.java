package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.ftc.intothedeep.ShortageAction;
import com.qualcomm.robotcore.hardware.Servo;

public class Elbow {
    private final Servo angleL;
    private final Servo angleR;

    public Elbow(Servo angleL, Servo angleR) {
        this.angleL = angleL;
        this.angleR = angleR;
    }

    public void setPosition(double tgt) {
        this.angleL.setPosition(1 - tgt);
        this.angleR.setPosition(tgt);
    }

    public static ShortageAction moveTo(double tgt) {
        return (drive) -> {
            drive.arm.elbow.setPosition(tgt);
            return true;
        };
    }
}

