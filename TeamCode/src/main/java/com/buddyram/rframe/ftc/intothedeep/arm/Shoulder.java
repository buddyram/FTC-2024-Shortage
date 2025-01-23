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
    private final DcMotor angleR;
    public static final int POSITION_ERROR_THRESHOLD = 5;

    public Shoulder(DcMotor angleL, DcMotor angleR, Robot robot) {
        super(robot);
        this.angleL = angleL;
        this.angleR = angleR;
    }

    public void setTargetPosition(int tgt) {
        this.angleL.setTargetPosition(tgt);
        this.angleR.setTargetPosition(-tgt);
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
