package com.buddyram.rframe.ftc.decode.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.Servo;

public class Sweeper extends BaseComponent<Robot> {
    private final CRServo servo;
    public static final double INTAKING = 1;
    public static final double OUTTAKING = 0;

    public Sweeper(Robot robot, CRServo servo) {
        super(robot);
        this.servo = servo;
    }

    public void setPower(double newPower) {
        this.servo.setPower(newPower);
    }

    public static RobotAction<DecodeBot> moveTo(double tgt) {
        return (robot) -> {
            robot.getIntake().sweeper.setPower(tgt);
            return true;
        };
    }
}