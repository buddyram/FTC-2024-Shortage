package com.buddyram.rframe.ftc.intothedeep.intake;
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
    public static final int MIN = 0;
    public static final int MAX = 2000;

    public Extension(Robot robot, DcMotor winch) {
        super(robot);
        this.winch = winch;
    }

    public void setTargetPosition(int tgt) {
        if (tgt > MAX || tgt < MIN) {
            return;
        }
        this.winch.setTargetPosition(tgt);
    }

    public int getPosition() {
        return this.winch.getCurrentPosition();
    }

    public void incrementTargetPosition(int delta) {
        this.setTargetPosition(this.getPosition() + delta);
    }

    public static ShortageAction moveTo(int tgt) {
        return (drive) -> {
            drive.getIntake().extension.setTargetPosition(tgt);
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
