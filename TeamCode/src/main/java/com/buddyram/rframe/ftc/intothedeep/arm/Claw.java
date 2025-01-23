package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.ftc.intothedeep.actions.ShortageAction;
import com.qualcomm.robotcore.hardware.Servo;

public class Claw extends BaseComponent<Robot> {
    private final Servo angle;
    public static final double OPEN = 1;
    public static final double CLOSE = 0;

    public Claw(Servo angle, Robot robot) {
        super(robot);
        this.angle = angle;
    }

    public void setPosition(double tgt) {
        this.angle.setPosition(tgt);
        this.getRobot().getLogger().log("claw", tgt);
    }

    public static ShortageAction moveTo(double tgt) {
        return (drive) -> {
            drive.getArm().claw.setPosition(tgt);
            return true;
        };
    }
}

