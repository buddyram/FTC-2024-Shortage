package com.buddyram.rframe.ftc.intothedeep.arm;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.ConditionalWrapperAction;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.intothedeep.actions.ShortageAction;
import com.buddyram.rframe.ftc.intothedeep.ShortageBot;
import com.qualcomm.robotcore.hardware.DcMotor;

public class Extension extends BaseComponent<Robot> {
    private final DcMotor winch;
    public static final int POSITION_ERROR_THRESHOLD = 5;

    public Extension(DcMotor winch, Robot robot) {
        super(robot);
        this.winch = winch;
    }

    public void setTargetPosition(int tgt) {
        this.winch.setTargetPosition(tgt);
    }

    public int getPosition() {
        return this.winch.getCurrentPosition();
    }

    public static ShortageAction moveTo(int tgt) {
        return (drive) -> {
            drive.getArm().extension.setTargetPosition(tgt);
            return true;
        };
    }

    public static RobotAction<ShortageBot> moveToAndWait(int tgt) {
        return new ConditionalWrapperAction<>(
                Extension.moveTo(tgt),
                (drive) -> Math.abs(drive.getArm().extension.getPosition() - tgt) < POSITION_ERROR_THRESHOLD
        );
    }
}
