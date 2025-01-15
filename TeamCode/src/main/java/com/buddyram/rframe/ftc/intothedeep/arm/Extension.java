package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.ftc.ConditionalWrapperAction;
import com.buddyram.rframe.ftc.RobotAction;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Extension {
    private final DcMotor winch;
    public static final int POSITION_ERROR_THRESHOLD = 5;

    public Extension(DcMotor winch) {
        this.winch = winch;
    }

    public void setTargetPosition(int tgt) {
        this.winch.setTargetPosition(tgt);
    }

    public int getPosition() {
        return this.winch.getCurrentPosition();
    }

    public static RobotAction moveTo(int tgt) {
        return (drive) -> {
            drive.arm.extension.setTargetPosition(tgt);
            return true;
        };
    }

    public static RobotAction moveToAndWait(int tgt) {
        return new ConditionalWrapperAction(
            Extension.moveTo(tgt),
            (drive) -> Math.abs(drive.arm.extension.getPosition() - tgt) < POSITION_ERROR_THRESHOLD
        );
    }
}
