package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.intothedeep.actions.ShortageAction;
import com.qualcomm.robotcore.hardware.Servo;

public class Elbow extends BaseComponent<Robot> {
    private final Servo angleL;
    private final Servo angleR;

    public Elbow(Servo angleL, Servo angleR, Robot robot) {
        super(robot);
        this.angleL = angleL;
        this.angleR = angleR;
    }

    public void setPosition(double tgt) {
        this.angleL.setPosition(1 - tgt);
        this.angleR.setPosition(tgt);
    }

    public static ShortageAction moveTo(double tgt) {
        return (drive) -> {
            drive.getArm().elbow.setPosition(tgt);
            return true;
        };
    }
}

