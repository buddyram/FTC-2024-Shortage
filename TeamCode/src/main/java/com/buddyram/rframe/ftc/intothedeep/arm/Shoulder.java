package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Shoulder {
    private final DcMotor angleL;
    private final DcMotor angleR;
    public static final int POSITION_ERROR_THRESHOLD = 5;

    public Shoulder(DcMotor angleL, DcMotor angleR) {
        this.angleL = angleL;
        this.angleR = angleR;
    }

    public void setTargetPosition(int tgt) {
        this.angleL.setTargetPosition(tgt);
        this.angleR.setTargetPosition(tgt / 3);
    }

    public int getPosition() {
        return this.angleL.getCurrentPosition();
    }

    public static ShortageAction moveTo(int tgt) {
        return (drive) -> {
            drive.arm.angle.setTargetPosition(tgt);
            return true;
        };
    }

    public static RobotAction<ShortageBot> moveToAndWait(int tgt) {
        return new ConditionalWrapperAction<>(
                Shoulder.moveTo(tgt),
                (drive) -> Math.abs(drive.arm.angle.getPosition() - tgt) < POSITION_ERROR_THRESHOLD
        );
    }
}
