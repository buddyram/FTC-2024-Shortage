package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.actions.ShortageAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Shoulder extends BaseComponent<Robot> {
    private final DcMotor angleL;
    public static final int POSITION_ERROR_THRESHOLD = 5;
    public static final int MIN = 0;
    public static final int MAX = 467;

    public Shoulder(DcMotor angleL, Robot robot) {
        super(robot);
        this.angleL = angleL;
    }

    public void setTargetPosition(int tgt) {
        if (tgt > MAX || tgt < MIN) {
            return;
        }
        this.angleL.setTargetPosition(tgt);
    }

    public void incrementTargetPosition(int delta) {
        this.setTargetPosition(this.getPosition() + delta);
    }

    public int getPosition() {
        return this.angleL.getCurrentPosition();
    }

    public static ShortageAction moveTo(int tgt) {
        return (drive) -> {
            drive.getArm().angle.setTargetPosition(tgt);
            return true;
        };
    }

    public static RobotAction<ShortageBot> moveToAndWait(int tgt) {
        return new ConditionalWrapperAction<>(
                Shoulder.moveTo(tgt),
                (drive) -> Math.abs(drive.getArm().angle.getPosition() - tgt) < POSITION_ERROR_THRESHOLD
        );
    }
}
