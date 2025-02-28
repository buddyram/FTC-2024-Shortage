package com.buddyram.rosebot.head;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Motor;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.buddyram.rframe.ftc.intothedeep.actions.ShortageAction;
import com.buddyram.rosebot.RosebotAction;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Extension extends BaseComponent<Robot> {
    private final DcMotor motor;
    public static final int POSITION_ERROR_THRESHOLD = 5;
    public static final int MIN = 0;
    public static final int MAX = 1948;

    public Extension(Robot robot, DcMotor motor) {
        super(robot);
        this.motor = motor;
    }
    public void setTargetPosition(int tgt) {
        if (tgt > MAX || tgt < MIN) {
            return;
        }
        this.motor.setTargetPosition(tgt);
    }

    public int getPosition() {
        return this.motor.getCurrentPosition();
    }

    public void incrementTargetPosition(int delta) {
        this.setTargetPosition(this.getPosition() + delta);
    }

    public static RosebotAction moveTo(int tgt) {
        return (drive) -> {
            drive.getHead().extension.setTargetPosition(tgt);
            return true;
        };
    }

    public static RobotAction<ShortageBot> moveToAndWait(int tgt) {
        return new ConditionalWrapperAction<>(
                com.buddyram.rframe.ftc.intothedeep.arm.Extension.moveTo(tgt),
                (drive) -> Math.abs(drive.getArm().extension.getPosition() - tgt) < POSITION_ERROR_THRESHOLD
        );
    }
}
