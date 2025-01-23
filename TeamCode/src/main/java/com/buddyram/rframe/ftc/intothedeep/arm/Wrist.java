package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.robotcore.hardware.Servo;

public class Wrist extends BaseComponent<Robot> {
    private final Servo angle;

    public Wrist(Servo angle, Robot robot) {
        super(robot);
        this.angle = angle;
    }

    public void incrementTargetPosition(double delta) {
        this.setPosition(this.getPosition() + delta);
    }

    public void setPosition(double tgt) {
        if (tgt > 1 || tgt < 0) {
            return;
        }
        this.angle.setPosition(tgt);
    }

    public double getPosition() {
        return this.angle.getPosition();
    }

    public static RobotAction<ShortageBot> moveTo(double tgt) {
        return (drive) -> {
            drive.getArm().wrist.setPosition(tgt);
            return true;
        };
    }
}

