package com.buddyram.rframe.ftc.decode.launcher;

import com.buddyram.rframe.BaseComponent;
import com.buddyram.rframe.Robot;
import com.buddyram.rframe.actions.RobotAction;
import com.buddyram.rframe.ftc.decode.DecodeBot;
import com.buddyram.rframe.ftc.intothedeep.BotUtils;
import com.qualcomm.robotcore.hardware.Servo;

public class Feeder extends BaseComponent<Robot> {
    private final Servo servo;
    public static final double OPEN = 1;
    public static final double CLOSE = 0.4;

    public Feeder(Robot robot, Servo servo) {
        super(robot);
        this.servo = servo;
    }

    public void setPosition(double newPosition) {
        if (newPosition > OPEN || newPosition < CLOSE) {
            return;
        }
        this.servo.setPosition(newPosition);
    }

    public static RobotAction<DecodeBot> moveTo(double tgt) {
        return (robot) -> {
            robot.getLauncher().feeder.setPosition(tgt);
            return true;
        };
    }
}